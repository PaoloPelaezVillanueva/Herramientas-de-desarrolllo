package com.equipo.tambo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:tambo_test;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.defer-datasource-initialization=true",
        "spring.jpa.show-sql=false",
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=file:docs/postman/e2e-data.sql",
        "logging.level.org.springframework.security=WARN",
        "security.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
class TamboBackendApplicationTests {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;

    private final HttpClient http = HttpClient.newHttpClient();
    private String admin;
    private String cashier;

    @BeforeEach
    void logInAsEachRole() throws Exception {
        admin = logIn("admin_test", "AdminTest123!");
        cashier = logIn("cajero_test", "AdminTest123!");
    }

    @Test
    void categoriesAndProductsRespectLinksAndRejectDuplicates() throws Exception {
        Map<String, String> category = Map.of("category", "Protección de catálogo");
        long categoryId = id(request("POST", "/categories", category, admin, 201));
        request("POST", "/categories", category, admin, 409);
        request("PUT", "/categories/" + categoryId, Map.of("category", "Bebidas"), admin, 409);
        request("PUT", "/categories/" + categoryId, category, admin, 200);
        request("DELETE", "/categories/99999", null, admin, 404);

        JsonNode product = request("POST", "/products",
                Map.of("name", "Producto vinculado", "cost", 2.5, "stock", 18, "category", categoryId), admin, 201);
        long productId = id(product);
        assertEquals("", product.get("description").asText());
        request("DELETE", "/categories/" + categoryId, null, admin, 409);
        long saleId = id(request("POST", "/sales", sale(2, productId, 1), cashier, 201));
        request("DELETE", "/products/" + productId, null, admin, 409);
        request("DELETE", "/sales/" + saleId, null, admin, 204);
        request("DELETE", "/products/" + productId, null, admin, 204);
        request("DELETE", "/categories/" + categoryId, null, admin, 204);
    }

    @Test
    void clientsValidateContactAndKeepSalesLinks() throws Exception {
        Map<String, String> client = Map.of("name", "Cliente", "lastname", "Pruebas",
                "dni", "83344551", "email", "cliente-crud@example.test");
        JsonNode response = request("POST", "/clients", client, cashier, 201);
        long clientId = id(response);
        assertEquals("", response.get("address").asText());
        request("POST", "/clients", client, cashier, 409);
        request("POST", "/clients", Map.of("name", "Cliente", "lastname", "Pruebas",
                "dni", "83344552", "email", "CLIENTE-CRUD@example.test"), cashier, 409);
        request("POST", "/clients", Map.of("name", "Cliente", "lastname", "Pruebas",
                "dni", "83344553"), cashier, 400);
        request("PUT", "/clients/" + clientId, Map.of("name", "Cliente", "lastname", "Pruebas",
                "dni", "12345678", "email", "cliente-crud@example.test"), cashier, 409);
        request("PUT", "/clients/" + clientId, Map.of("name", "Cliente", "lastname", "Pruebas",
                "dni", "83344551", "email", "cliente@example.test"), cashier, 409);
        JsonNode edited = request("PUT", "/clients/" + clientId, Map.of("name", "Cliente",
                "lastname", "Editado", "dni", "83344551", "email", "cliente-crud@example.test",
                "address", "Lima"), cashier, 200);
        assertEquals("Lima", edited.get("address").asText());

        long productId = product("Compra con cliente", 2.5, 18);
        long saleId = id(request("POST", "/sales",
                Map.of("user", 2, "client", clientId, "details", List.of(line(productId, 1))), cashier, 201));
        request("DELETE", "/clients/" + clientId, null, admin, 409);
        request("DELETE", "/sales/" + saleId, null, admin, 204);
        request("DELETE", "/clients/" + clientId, null, admin, 204);
    }

    @Test
    void administratorCanUpdateAndDeleteAUserWithoutBreakingSales() throws Exception {
        Map<String, String> registration = Map.of("name", "Cajero", "lastname", "Temporal",
                "user", "user_temporal", "password", "DemoPassword1!");
        long userId = id(request("POST", "/auth/register", registration, admin, 201));
        String path = "/users/" + userId;
        request("PUT", path, user("cajero_test", "DemoPassword2!", 2), admin, 409);
        request("PUT", path, user("user_temporal", "á".repeat(37), 2), admin, 400);
        request("PUT", path, user("user_temporal", "DemoPassword2!", 99999), admin, 404);
        request("PUT", path, user("user_temporal", "DemoPassword2!", 2), admin, 200);
        String changedUser = logIn("user_temporal", "DemoPassword2!");
        long productId = product("Compra de usuario temporal", 1, 18);
        long saleId = id(request("POST", "/sales", sale(userId, productId, 1), changedUser, 201));
        request("DELETE", path, null, admin, 409);
        request("DELETE", "/sales/" + saleId, null, admin, 204);
        request("DELETE", path, null, admin, 204);
    }

    @Test
    void rejectedSalesLeaveTheStockAndTotalUnchanged() throws Exception {
        long productId = product("Stock limitado", 2.5, 18);
        long expensive = product("Importe máximo", 99999999.99, 18);
        request("POST", "/sales", sale(2, productId, 20), cashier, 400);
        request("POST", "/sales", sale(2, productId, 1.9), cashier, 400);
        request("POST", "/sales", sale(2, 99999, 1), cashier, 404);
        request("POST", "/sales", sale(2, expensive, 2), cashier, 400);
        request("POST", "/sales",
                Map.of("user", 2, "details", List.of(line(productId, 2), line(99999, 1))), cashier, 404);
        assertStock(productId, 18);
        assertStock(expensive, 18);

        long saleId = id(request("POST", "/sales", sale(2, productId, 2), cashier, 201));
        request("PUT", "/sales/" + saleId, sale(2, productId, 20), cashier, 400);
        assertEquals(5.0, request("GET", "/sales/" + saleId, null, cashier, 200).get("total").asDouble());
        request("PUT", "/products/" + productId, catalogProduct("Stock limitado", 2.5, Integer.MAX_VALUE, true), admin, 200);
        request("DELETE", "/sales/" + saleId, null, admin, 409);
        assertStock(productId, Integer.MAX_VALUE);
    }

    @Test
    void editingADetailRespectsItsOwnerAndRestoresStock() throws Exception {
        long original = product("Precio al vender", 2.5, 18);
        long other = product("Producto de reemplazo", 4, 18);
        request("POST", "/sales", sale(1, original, 1), cashier, 403);
        JsonNode sale = request("POST", "/sales", sale(2, original, 2), cashier, 201);
        long saleId = id(sale);
        String salePath = "/sales/" + saleId;
        request("PUT", salePath, sale(1, original, 2), cashier, 403);

        long adminSale = id(request("POST", "/sales", sale(1, other, 1), admin, 201));
        request("PUT", "/sales/" + adminSale, sale(2, other, 1), cashier, 403);
        request("DELETE", "/sales/" + adminSale, null, admin, 204);

        request("PUT", "/products/" + original, catalogProduct("Precio al vender", 3.5, 16, false), admin, 200);
        request("POST", "/sales", sale(2, original, 1), cashier, 409);
        JsonNode edited = request("PUT", salePath,
                Map.of("user", 2, "client", 1, "details", List.of(line(original, 2))), cashier, 200);
        assertEquals(5.0, edited.get("total").asDouble());
        assertStock(original, 16);

        String detailPath = salePath + "/details/" + id(edited.get("details").get(0));
        request("PUT", salePath + "/details/99999", line(original, 1), cashier, 404);
        JsonNode replacement = request("PUT", detailPath, line(other, 3), cashier, 200);
        assertEquals(12.0, replacement.get("total").asDouble());
        assertStock(original, 18);
        assertStock(other, 15);
        request("DELETE", detailPath, null, cashier, 204);
        assertStock(other, 18);
        request("DELETE", salePath, null, admin, 204);
    }

    @Test
    void retryingTheSamePurchaseReturnsTheSameSale() throws Exception {
        long product = product("Reintento de compra", 2.5, 18);
        String key = UUID.randomUUID().toString();
        var body = sale(2, product, 3);
        var first = http.sendAsync(buildRequest("POST", "/sales", body, cashier, key),
                HttpResponse.BodyHandlers.ofString());
        var retry = http.sendAsync(buildRequest("POST", "/sales", body, cashier, key),
                HttpResponse.BodyHandlers.ofString());
        long saleId = id(expect(first.get(), 201));
        assertEquals(saleId, id(expect(retry.get(), 201)));
        assertStock(product, 15);

        expect(http.send(buildRequest("POST", "/sales", sale(2, product, 4), cashier, key),
                HttpResponse.BodyHandlers.ofString()), 409);
        long secondSale = id(expect(http.send(buildRequest("POST", "/sales", body,
                cashier, UUID.randomUUID().toString()), HttpResponse.BodyHandlers.ofString()), 201));
        assertNotEquals(saleId, secondSale);
        request("DELETE", "/sales/" + saleId, null, admin, 204);
        expect(http.send(buildRequest("POST", "/sales", body, cashier, key),
                HttpResponse.BodyHandlers.ofString()), 410);
        assertStock(product, 15);
    }

    private String logIn(String user, String password) throws Exception {
        return request("POST", "/auth/login", Map.of("user", user, "password", password), null, 200)
                .get("accessToken").asText();
    }

    private long product(String name, double cost, int stock) throws Exception {
        return id(request("POST", "/products", catalogProduct(name, cost, stock, true), admin, 201));
    }

    private void assertStock(long productId, int expected) throws Exception {
        assertEquals(expected, request("GET", "/products/" + productId, null, admin, 200).get("stock").asInt());
    }

    private Map<String, Object> catalogProduct(String name, double price, int stock, boolean active) {
        return Map.of("name", name, "cost", price, "stock", stock, "category", 1,
                "active", active, "description", "Producto de prueba");
    }

    private Map<String, Object> user(String name, String password, int role) {
        return Map.of("name", "Cajero", "lastname", "Temporal", "user", name, "password", password, "role", role);
    }

    private Map<String, Object> sale(long user, long product, Number quantity) {
        return Map.of("user", user, "details", List.of(line(product, quantity)));
    }

    private Map<String, Object> line(long product, Number quantity) {
        return Map.of("product", product, "quantity", quantity);
    }

    private long id(JsonNode response) {
        return response.get("id").asLong();
    }

    private JsonNode request(String method, String path, Object body, String token, int status) throws Exception {
        return expect(http.send(buildRequest(method, path, body, token, null),
                HttpResponse.BodyHandlers.ofString()), status);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String token, String key) {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api" + path))
                .timeout(Duration.ofSeconds(10));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        if (key != null) builder.header("Idempotency-Key", key);
        if (body == null) {
            return builder.method(method, HttpRequest.BodyPublishers.noBody()).build();
        }
        return builder.header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
    }

    private JsonNode expect(HttpResponse<String> response, int status) {
        assertEquals(status, response.statusCode(), () -> response.uri() + " " + response.body());
        return response.body().isBlank() ? null : json.readTree(response.body());
    }

}
