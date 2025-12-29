# mobyapp-ms-news

Este documento proporciona una visión general y una guía operativa para el microservicio `mobyapp-ms-news`. Está destinado a ser una referencia para el desarrollo, despliegue y mantenimiento.

## Resumen del Proyecto

El microservicio `mobyapp-ms-news` es un microservicio Spring Boot diseñado para gestionar y entregar contenido de noticias para la plataforma MobyApp. Se integra con Contentful como un CMS headless para obtener, filtrar, ordenar y almacenar en caché artículos de noticias. El servicio expone APIs RESTful, distinguiendo entre contenido adaptado para la aplicación móvil y la plataforma web. También incluye tareas programadas para limpiar automáticamente las entradas de noticias caducadas de Contentful.

**Tecnologías Clave:**

*   **Lenguaje:** Java 17
*   **Framework:** Spring Boot 3.2.0
*   **Herramienta de Construcción:** Apache Maven
*   **Gestión de API:** Springdoc OpenAPI (Swagger UI)
*   **Integración con CMS:** Contentful Java SDK (CDA para lectura, CMA para escritura/eliminación)
*   **Mensajería:** Spring Kafka (configurado, pero los detalles de uso específicos requieren más investigación)
*   **Descubrimiento de Servicios:** Spring Cloud Eureka (`@EnableDiscoveryClient`, `spring-cloud-starter-openfeign`)
*   **Caché:** Spring Cache (para contenido de noticias)
*   **Contenerización:** Docker
*   **CI/CD:** Bitbucket Pipelines, Google Cloud Build

## Construcción y Ejecución

### Prerrequisitos

*   Java Development Kit (JDK) 17 o superior
*   Apache Maven 3.x
*   Docker (para despliegue contenerizado)

### Desarrollo Local

1.  **Clonar el repositorio:**
    ```bash
    git clone <url-del-repositorio>
    cd mobyapp-ms-news
    ```
2.  **Configurar Variables de Entorno:**
    La aplicación depende en gran medida de las variables de entorno para configuraciones sensibles como las claves API de Contentful, las credenciales de Kafka y las URLs del servicio Eureka. Estas se configuran típicamente en su entorno local o a través de un archivo `dev.properties` (referenciado por `application.properties`).
    
    *   `NEWS_PORT`: Puerto para la aplicación (ej. `8084`)
    *   `URL_EUREKA`: URL del servicio de descubrimiento Eureka
    *   `SERVICE_HOSTNAME`: Nombre de host para el registro en Eureka (ej. `localhost`)
    *   `CONTENTFUL_IDSPACE`: ID del espacio de Contentful
    *   `CONTENTFUL_CDA_ACCESSTOKEN`: Token de acceso a la API de entrega de contenido (CDA) de Contentful
    *   `CONTENTFUL_CMA_ACCESSTOKEN`: Token de acceso a la API de gestión de contenido (CMA) de Contentful
    *   `BOOTSTRAP_SERVER`: Servidores de arranque de Kafka
    *   `API_KEY`: Clave API de Kafka
    *   `API_SECRET`: Secreto API de Kafka

3.  **Construir el proyecto:**
    ```bash
    mvn clean install -DskipTests
    ```
    Esto compilará el código, ejecutará las pruebas (si se omite `-DskipTests`) y empaquetará la aplicación en un archivo JAR en el directorio `target/`.

4.  **Ejecutar la aplicación:**
    ```bash
    java -jar target/mobyapp-ms-news-0.0.1-SNAPSHOT.jar
    ```
    Asegúrese de que todas las variables de entorno requeridas estén configuradas antes de ejecutar.

### Docker

Para construir y ejecutar la aplicación usando Docker:

1.  **Construir la imagen de Docker:**
    ```bash
    docker build -t mobyapp-ms-news .
    ```
2.  **Ejecutar el contenedor Docker:**
    Deberá pasar las variables de entorno al contenedor.
    ```bash
    docker run -p 8084:8084 \
    -e NEWS_PORT=8084 \
    -e URL_EUREKA=<url_eureka> \
    -e SERVICE_HOSTNAME=<nombre_host_servicio> \
    -e CONTENTFUL_IDSPACE=<id_espacio_contentful> \
    -e CONTENTFUL_CDA_ACCESSTOKEN=<token_cda_contentful> \
    -e CONTENTFUL_CMA_ACCESSTOKEN=<token_cma_contentful> \
    -e BOOTSTRAP_SERVER=<servidor_arranque_kafka> \
    -e API_KEY=<clave_api_kafka> \
    -e API_SECRET=<secreto_api_kafka> \
    mobyapp-ms-news
    ```

### Documentación de la API

Una vez que la aplicación esté en funcionamiento, la documentación de OpenAPI (Swagger) estará disponible en:
`http://localhost:<PUERTO_NOTICIAS>/swagger-ui.html`

## Endpoints de la API

El `ContentfulController` proporciona los siguientes endpoints principales:

*   **`GET /api/contentful/news/app`**: Recupera una lista de artículos de noticias activos específicamente adaptados para la aplicación móvil MobyApp. Las noticias se filtran por fecha de caducidad, se ordenan por prioridad y se almacenan en caché.
*   **`GET /api/contentful/news/web`**: Recupera una lista de artículos de noticias activos específicamente adaptados para la plataforma web de Moby. Las noticias se filtran por fecha de caducidad, se ordenan por prioridad y se almacenan en caché.

El contenido de las noticias se obtiene de Contentful, se procesa y, potencialmente, se limita a un número específico de entradas (ej. 8).

## Convenciones de Desarrollo

*   **Estructura Estándar de Spring Boot:** El proyecto sigue una estructura típica de aplicación Spring Boot con paquetes para `controller`, `service`, `config`, `dto` y `exception`.
*   **Principios RESTful:** Los controladores se adhieren al diseño de API RESTful para la gestión de recursos.
*   **Integración con Contentful:** Utiliza tanto la API de entrega de Contentful (CDA) para la recuperación de contenido público como la API de gestión de Contentful (CMA) para tareas administrativas como la eliminación de entradas caducadas.
*   **Caché:** Los datos de noticias se almacenan en caché para mejorar el rendimiento y reducir las llamadas directas a Contentful. La caché se vacía periódicamente y ante modificaciones relevantes de los datos.
*   **Tareas Programadas:** Incluye trabajos programados para el procesamiento en segundo plano, como la limpieza de entradas de noticias caducadas.
*   **Documentación OpenAPI:** Los endpoints están autodocumentados utilizando anotaciones de Springdoc OpenAPI, lo que facilita el descubrimiento y consumo de la API.

## CI/CD y Despliegue

*   **Bitbucket Pipelines (`bitbucket-pipelines.yml`):** Configurado para la integración continua, específicamente para sincronizar la rama `develop` con la rama `main` en un repositorio de GitHub. Esta configuración sugiere que GitHub (y potencialmente Railway, como se menciona en los comentarios) se utiliza para despliegues posteriores.
*   **Google Cloud Build (`cloudbuild.yaml`):** Proporciona una pipeline completa para construir imágenes de Docker, subirlas a Google Artifact Registry y desplegar el servicio en Google Cloud Run. Las variables de entorno para el despliegue en Cloud Run se gestionan mediante sustituciones.
*   **Dockerfile:** Define el proceso de construcción de varias etapas para crear una imagen de Docker ligera, comenzando con Maven para la construcción y luego utilizando una imagen solo con JRE para el tiempo de ejecución. Incluye comprobaciones de salud y gestión de usuarios para mayor seguridad.

## Información Adicional

*   **Configuración de Kafka:** Aunque Kafka está configurado, una inmersión más profunda en su uso (productores/consumidores, temas, estructura de mensajes) requeriría examinar las implementaciones de servicio relevantes.
*   **Manejo de Errores:** Se implementa un manejo global de excepciones (`GlobalExceptionHandler.java`) para proporcionar respuestas de error consistentes.
*   **Seguridad:** Los detalles de autenticación y autorización no están cubiertos explícitamente en los archivos analizados, pero serían críticos para un entorno de producción.