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
