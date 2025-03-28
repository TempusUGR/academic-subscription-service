# Academic Subscription Service - CalendarUgr

## Descripción
El **Academic Subscription Service** es un microservicio dentro del sistema **CalendarUgr** encargado de la gestión de suscripciones académicas de los estudiantes. Su función principal es permitir a los usuarios suscribirse a asignaturas, obtener un horario personalizado basado en sus suscripciones y generar un archivo **.ics** compatible con aplicaciones de calendario externas.

## Características
- Gestión de suscripciones a asignaturas.
- Generación automática de horarios personalizados.
- Exportación del horario en formato `.ics` para integración con calendarios externos.
- Integración con otros microservicios de CalendarUgr.

## Requisitos previos
Para ejecutar este servicio, es necesario configurar las siguientes variables de entorno:

- `DB_URL`: URL de conexión a la base de datos MongoDB.
- `DB_NAME`: Nombre de la base de datos MongoDB.

## Instalación y ejecución

1. Clonar el repositorio:

    ```sh
    git clone <repository-url>
    cd academic-subscription-service
    ```

2. Configurar variables de entorno:

    ```sh
    export DB_URL=<your_mongodb_url>
    export DB_NAME=<your_mongodb_database_name>
    ```

3. Construir y ejecutar el servicio:

    ```sh
    ./mvnw spring-boot:run