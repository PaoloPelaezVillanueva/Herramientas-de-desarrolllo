# Pruebas unitarias

El proyecto utiliza JUnit 5 y Mockito a través de las dependencias de pruebas de Spring Boot.

Las pruebas de servicios utilizan Mockito y no requieren MySQL ni secretos. La prueba
de arranque levanta el contexto de Spring con el perfil `test`, una base H2 en memoria
y una clave JWT ficticia. Este perfil solo se activa en las pruebas.

```bash
./mvnw test
```

Para generar el reporte de cobertura:

```bash
./mvnw verify
```

En Windows utiliza `.\mvnw.cmd verify`. El reporte de JaCoCo
se genera en `target/site/jacoco/index.html` y `target/site/jacoco/jacoco.xml`.

Las pruebas de regresión del PR #55 cubren las restricciones de eliminación de
categorías, productos, clientes y usuarios; la suma de cantidades de productos
repetidos; los límites de stock; los productos inexistentes; y la validación de
stock antes de crear los detalles y persistir una venta.
