import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';

// Run only against the local H2 server started by iniciar-api-pruebas.ps1.
const baseUrl = 'http://127.0.0.1:18080/api';
let adminToken;

async function api(method, path, { body, token = adminToken, key, status } = {}) {
    const headers = {};
    if (token) headers.Authorization = 'Bearer ' + token;
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (key !== undefined) headers['Idempotency-Key'] = key;
    const response = await fetch(baseUrl + path, {
        method, headers, body: body === undefined ? undefined : JSON.stringify(body)
    });
    const raw = await response.text();
    if (status !== undefined) {
        assert.equal(response.status, status, method + ' ' + path + ' respondió ' + raw);
    }
    return { status: response.status, data: raw ? JSON.parse(raw) : null };
}

async function login(user, password) {
    const { data } = await api('POST', '/auth/login', { body: { user, password }, status: 200 });
    return data.accessToken;
}

async function product(cost = 2.5) {
    const body = { name: 'Prueba ' + randomUUID(), stock: 18, cost, category: 1 };
    return (await api('POST', '/products', { body, status: 201 })).data;
}

async function checkStock(id, expected) {
    const { data } = await api('GET', '/products/' + id, { status: 200 });
    assert.equal(data.stock, expected);
}

function sale(productId, quantity, user = 1) {
    return { user, client: 1, details: [{ product: productId, quantity }] };
}

adminToken = await login('admin_test', 'AdminTest123!');
const cashierToken = await login('cajero_test', 'AdminTest123!');

{
    const { id } = await product();
    await api('POST', '/sales', { body: sale(id, 1.9), status: 400 });
    await api('POST', '/sales', { body: sale(id, 20), status: 400 });
    const body = sale(id, 10);
    body.details.push({ product: id, quantity: 10 });
    await api('POST', '/sales', { body, status: 400 });
    await checkStock(id, 18);
    console.log('OK: Cantidades decimales y exceso de stock se rechazan sin descontar.');
}

for (const [quantity, expected, stock] of [[9, [201, 201], 0], [10, [201, 400], 8]]) {
    const { id } = await product();
    const responses = await Promise.all([
        api('POST', '/sales', { body: sale(id, quantity) }),
        api('POST', '/sales', { body: sale(id, quantity, 2), token: cashierToken })
    ]);
    assert.deepEqual(responses.map(result => result.status).sort(), expected);
    await checkStock(id, stock);
    console.log('OK: Dos cajeros piden ' + quantity + ' unidades a la vez. Stock final ' + stock + '.');
}

{
    const { id, name } = await product();
    const body = sale(id, 2);
    const { data: created } = await api('POST', '/sales', { body, status: 201 });
    assert.equal(created.total, 5);
    await api('PUT', '/products/' + id, {
        body: { name, stock: 16, cost: 3.5, category: 1, active: false }, status: 200
    });
    const updated = await api('PUT', '/sales/' + created.id, {
        body: { ...body, client: null }, status: 200
    });
    assert.equal(updated.data.total, 5);
    assert.equal(updated.data.client, null);
    await checkStock(id, 16);
    await api('POST', '/sales', { body, status: 409 });
    console.log('OK: Editar el cliente conserva el precio original, aunque el producto esté inactivo.');
}

{
    const { id } = await product(99999999.99);
    await api('POST', '/sales', { body: sale(id, 2), status: 400 });
    await checkStock(id, 18);
    console.log('OK: La venta que supera el importe máximo se rechaza y restaura stock.');
}

{
    const { id } = await product();
    const body = sale(id, 3);
    const key = randomUUID();
    const [first, retry] = await Promise.all([
        api('POST', '/sales', { body, key, status: 201 }),
        api('POST', '/sales', { body, key, status: 201 })
    ]);
    assert.equal(retry.data.id, first.data.id);
    await checkStock(id, 15);
    await api('POST', '/sales', { body: sale(id, 4), key, status: 409 });
    const next = await api('POST', '/sales', { body, key: randomUUID(), status: 201 });
    assert.notEqual(next.data.id, first.data.id);
    await checkStock(id, 12);
    await api('DELETE', '/sales/' + first.data.id, { status: 204 });
    await api('POST', '/sales', { body, key, status: 410 });
    await checkStock(id, 15);
    console.log('OK: Reintentar la misma compra conserva el ID y descuenta una sola vez.');
}
