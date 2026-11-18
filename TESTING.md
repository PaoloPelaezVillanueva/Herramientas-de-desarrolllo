# Pruebas unitarias

El proyecto utiliza JUnit 5 y Mockito a través de las dependencias de pruebas de Spring Boot.

Las pruebas no utilizan MySQL ni levantan el contexto completo de la aplicación.

```bash
./mvnw test
```

Para generar el reporte de cobertura:

```bash
./mvnw verify
```
