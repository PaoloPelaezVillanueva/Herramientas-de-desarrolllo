# Pruebas unitarias

El proyecto utiliza JUnit 5 y Mockito a través de las dependencias de pruebas de Spring Boot.

Las pruebas unitarias no utilizan MySQL. La prueba de arranque levanta el contexto completo de la aplicación con H2 en memoria.

```bash
./mvnw test
```

Para generar el reporte de cobertura:

```bash
./mvnw verify
```

## Pruebas de la API en Postman

1. Desde la raíz del repositorio, inicia el backend temporal con PowerShell en Windows.

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\postman\iniciar-api-pruebas.ps1
   ```

2. Importa `docs/postman/Tambo-Postman-pruebas.json` en Postman y ejecuta la colección completa en orden. Las peticiones de inicio de sesión guardan los tokens de administrador y cajero.
3. Para repetir la colección, detén el backend con Ctrl+C y vuelve a iniciar el script. La colección usa los datos de prueba de una base H2 en memoria.

También puedes ejecutar la colección con Newman mientras la API de prueba está activa.

```powershell
npx --yes newman run docs/postman/Tambo-Postman-pruebas.json
```

### Reintentos al registrar ventas

En `POST /api/sales` se puede enviar la cabecera `Idempotency-Key` con un UUID nuevo por compra. Para reintentar la misma compra, vuelve a enviar el mismo cuerpo y la misma clave. La API devuelve el mismo ID de venta y no descuenta stock otra vez. Reutilizar la clave con datos distintos devuelve `409`. Si la venta ya fue eliminada, el reintento devuelve `410`.

H2 crea la tabla de claves durante el arranque local. Para usar la cabecera con una base MySQL existente, aplica primero el SQL de [sale-submissions.sql](docs/sale-submissions.sql).

Con Node.js 22 y la API H2 recién iniciada, ejecuta las pruebas de compras simultáneas, reintentos, cantidades y precios con `node docs/postman/verificar-ventas.mjs`.
