# TempusUGR - Academic Subscription Service

Este repositorio contiene el código fuente del `academic-subscription-service`, un microservicio clave dentro de la arquitectura del proyecto **TempusUGR**.

Su principal responsabilidad es gestionar toda la información académica personalizada del usuario. Esto incluye las suscripciones a asignaturas y grupos, la creación de eventos personalizados (como tutorías o charlas) y la generación del calendario final que el usuario visualiza y sincroniza.

---

## ✨ Funcionalidades Principales

* **Gestión de Suscripciones:** Permite a los usuarios suscribirse y darse de baja de grupos específicos de asignaturas. También ofrece la posibilidad de realizar suscripciones masivas (por ejemplo, todas las asignaturas que imparte un profesor).
* **Generación de Calendario Personalizado:** Agrega la información de las clases oficiales (obtenidas del `schedule-consumer-service`) con los eventos personalizados creados por el usuario para generar una vista de calendario completa.
* **Creación de Eventos:** Ofrece endpoints para que los profesores y administradores puedan crear eventos adicionales, ya sea a nivel de grupo de asignatura o a nivel de facultad.
* **Exportación y Sincronización:** Genera archivos en formato **iCalendar (.ics)** y proporciona una **URL de sincronización** segura y única para que los usuarios puedan integrar su horario con calendarios externos como Google Calendar, Outlook, etc.
* **Notificaciones:** Publica mensajes en **RabbitMQ** cuando se crean nuevos eventos, permitiendo que otros servicios (como el `mail-service`) notifiquen a los usuarios afectados.

---

## 🛠️ Pila Tecnológica

* **Lenguaje/Framework:** Java 21, Spring Boot 3.4.4
* **Base de Datos:** **MongoDB**, elegido por su flexibilidad para almacenar documentos con estructuras variables como las suscripciones y los eventos.
* **Comunicación Asíncrona:** **RabbitMQ** para la publicación de eventos de notificación.
* **Comunicación Síncrona:** API REST para interactuar con otros servicios.
* **Descubrimiento de Servicios:** Cliente de **Eureka** para el registro y descubrimiento en la red de microservicios.

---

## 🏗️ Arquitectura e Interacciones

Este servicio es un componente central y se comunica con varios otros microservicios:

* **`schedule-consumer-service` (REST):** Le solicita la información de las clases oficiales (horarios, aulas, etc.) correspondientes a las suscripciones de un usuario.
* **`user-service` (REST):** Consulta información de los usuarios cuando es necesario para validar permisos o gestionar eventos.
* **`mail-service` (Vía RabbitMQ):** Publica eventos (ej. "evento creado") en una cola de RabbitMQ para que el servicio de correo envíe notificaciones de forma asíncrona.
* **`eureka-service`:** Se registra en el servidor Eureka para ser descubierto por el `api-gateway` y otros servicios.

---

## 🔌 API Endpoints Principales

El servicio expone una serie de endpoints bajo el prefijo `/academic-subscription`.

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/subscription` | Suscribe al usuario a un grupo de una asignatura. |
| `POST` | `/subscription-batching` | Realiza una suscripción masiva a varios grupos. |
| `GET`| `/subscriptions` | Devuelve las suscripciones activas del usuario. |
| `DELETE` | `/subscription` | Elimina una suscripción específica. |
| `GET` | `/entire-calendar`| Obtiene el calendario completo del usuario (clases + eventos). |
| `POST`| `/group-event` | Crea un evento a nivel de grupo de asignatura. |
| `POST`| `/faculty-event` | Crea un evento a nivel de facultad. |
| `DELETE` | `/group-event` | Elimina un evento de grupo por su ID. |
| `GET`| `/ics` | Genera y devuelve el calendario del usuario en formato `.ics`. |
| `GET`| `/sync-url` | Devuelve la URL pública para la sincronización del calendario. |

---

## 🚀 Puesta en Marcha Local

### **Prerrequisitos**

* Java 21 o superior.
* Maven 3.x.
* Una instancia de **MongoDB** en ejecución.
* Una instancia de **RabbitMQ** en ejecución.
* Un servidor **Eureka** (`eureka-service`) en ejecución.

### **Configuración**

Configura los siguientes parámetros en el archivo `src/main/resources/application.properties`:

```properties
# MongoDB Configuration
spring.data.mongodb.uri=mongodb://<user>:<password>@<host>:<port>/<database_name>

# RabbitMQ Configuration
spring.rabbitmq.host=<rabbitmq_host>
spring.rabbitmq.port=5672
spring.rabbitmq.username=<user>
spring.rabbitmq.password=<password>

# Eureka Client Configuration
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka

# Server Port
server.port=8083 # O el puerto deseado
