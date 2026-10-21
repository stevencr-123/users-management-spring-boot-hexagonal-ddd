# Users Management — Spring Boot, Arquitectura Hexagonal y DDD

Aplicación de gestión de usuarios construida con Java 17 y Spring Boot 3.3.5. La API REST es el
punto de entrada activo. La antigua CLI se conserva como adaptador inactivo.

La persistencia usa PostgreSQL mediante JDBC. Spring es el único punto de composición y la API
documentada con OpenAPI está disponible en `/swagger-ui.html`; el documento JSON está en
`/v3/api-docs`.

## Verificación

```bash
./mvnw clean verify
```

En Windows se puede utilizar `mvnw.cmd clean verify`. JaCoCo genera el informe en
`target/site/jacoco/index.html`.

## Notificaciones por correo

Las notificaciones se envían mediante la API REST de Brevo (`POST /v3/smtp/email`) y se ejecutan
asíncronamente en el executor `notificationExecutor`. Render bloquea los puertos SMTP habituales
(25, 465 y 587), por lo que la aplicación no utiliza SMTP ni JavaMail.

El adaptador reintenta errores transitorios con backoff exponencial y usa un circuit breaker para
evitar insistir cuando Brevo no está disponible. Los fallos de envío se registran sin incluir datos
personales. Configura `BREVO_API_KEY` con una clave REST (prefijo `xkeysib-`) y utiliza como
`BREVO_FROM_ADDRESS` un remitente verificado en Brevo.

## Secretos para desarrollo local

No guardes credenciales reales en archivos versionados. Copia
`application-secrets.properties.example` a `.environments/application-secrets.properties` y
completa los valores de PostgreSQL, JWT y Brevo localmente. Genera un `JWT_SECRET` aleatorio,
codificado en Base64, de al menos 32 bytes; por ejemplo, con `openssl rand -base64 32`.

También puedes definir las variables de entorno correspondientes. La contraseña de PostgreSQL y
las credenciales de Brevo no tienen valores por defecto en la configuración de la aplicación.

## Docker Compose local

1. Copia `compose.env.example` a `.env`.
2. Sustituye todos los valores `REEMPLAZAR` por valores locales válidos. Brevo requiere una API key
   REST y un remitente verificado; el JWT debe ser Base64 de al menos 32 bytes.
3. Arranca PostgreSQL y la API:

   ```bash
   docker compose up --build
   ```

La base de datos se publica solo en `127.0.0.1:5433`, y la API en `127.0.0.1:8080`. PostgreSQL
ejecuta `schema.sql` automáticamente al inicializar por primera vez el volumen.

## Despliegue en Render

`render.yaml` define el servicio Docker y su health check. En el panel de Render configura
`DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`, `BREVO_API_KEY`, `BREVO_FROM_ADDRESS` y `JWT_SECRET`.
La configuración también utiliza `DB_PORT`, `DB_NAME`, `DB_SSLMODE`, `BREVO_FROM_NAME` y
`JWT_EXPIRATION_SECONDS`.

Render no genera `JWT_SECRET` automáticamente: proporciona manualmente un valor Base64 generado a
partir de al menos 32 bytes aleatorios. Ejecuta `src/main/resources/schema.sql` una vez contra la
instancia PostgreSQL de Render, por ejemplo mediante `psql`, antes de usar la API.

Verifica el remitente en Brevo antes de configurar `BREVO_FROM_ADDRESS`. No incluyas la API key,
la contraseña de la base de datos ni el secreto JWT en el repositorio.
