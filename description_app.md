# Informe de Análisis del Proyecto: RentApp

Este documento proporciona una visión detallada de la arquitectura, diseño y propósito de la aplicación móvil **RentApp**. Está estructurado para servir tanto a desarrolladores que entran al proyecto como a stakeholders, gerentes de producto o inversores.

---

## 📖 Descripción No Técnica (Para Usuarios y Negocios)
**RentApp** es una herramienta integral, innovadora y moderna diseñada para ayudar a los propietarios y gestores a administrar de manera fácil y rápida sus propiedades de alquiler directamente desde su teléfono móvil. 

Con esta aplicación, los usuarios pueden registrar nuevas propiedades, ubicarlas de forma gráfica usando un mapa libre, gestionar los cobros y pagos de los inquilinos y visualizar eficaces reportes anuales de sus ganancias en tiempo real. Su característica más impresionante es que **permite trabajar sin conexión a internet** (Offline-First); todo lo que el usuario añade o modifica queda guardado en la memoria del teléfono y la app se encarga por su cuenta de actualizar y salvaguardar la información en la nube en el preciso momento en que detecta señal nuevamente. Además, RentApp acompaña al usuario en su propio idioma y mantiene la privacidad de sus finanzas bajo un estricto blindaje de seguridad biométrica (reconocimiento facial o huella digital).

---

## ⚙️ Descripción Técnica (Para Desarrolladores)
Se trata de una aplicación Android nativa, modular y escalable, desarrollada íntegramente en **Kotlin** siguiendo una estricta arquitectura **MVVM (Model-View-ViewModel)** y **Unidirectional Data Flow (UDF)**. 

La interfaz de usuario está desarrollada con **Jetpack Compose**, en un entorno completamente declarativo, y emplea la biblioteca Material 3 para los últimos estándares visuales. Utiliza **Coroutines y Flow** extensamente para reaccionar asíncronamente a los cambios de estado sin bloquear el hilo principal. 

A nivel de datos e infraestructura, el proyecto implementa un patrón robusto tipo híbrido (*"Local Source of Truth"*). Se gestionan bases de datos relacionales en el dispositivo por medio de **Room** (SQLite) apoyándose en **Firebase Firestore** para replicación remota. La sincronización local-nube es bidireccional, llevada a las sombras (background) con **Android WorkManager** para persistir cuando no hay acceso a la red, apoyada por una directiva de prevención de conflictos *"Last Write Wins"*. Adicionalmente, el proyecto evita costos y ataduras a licenciamientos de Google Maps, implementando integraciones cartográficas descentralizadas con **OSmdroid**.

---

## 🎯 Funcionalidades Principales de la App

* **Autenticación Moderna Integral**: Inicio de sesión ágil con Google Sign-In mediante la nueva API Android Credential Manager y Firebase Auth.
* **Seguridad Biométrica Inteligente**: Control de acceso y cierre de sesión seguro requiriendo lectura de huella dactilar o reconocimiento facial de la API Jetpack Biometrics, con configuraciones del usuario almacenadas en DataStore Preferences.
* **Gestión de Propiedades Eficiente**: Capacidad para agregar, listar, visualizar detalles y editar unidades de bienes raíces a comercializar.
* **Geolocalización en Mapas Libre**: Incorporación de un visor de mapa dinámico dentro de la creación/visualización de propiedades que permite al usuario marcar su dirección (sin usar API keys cerradas de Google).
* **Administración Financiera de Pagos rentables**: Registro de pagos, seguimiento de montos y uso del consumo de APIs REST vía la red para conversiones de divisas automáticas y manejo de países (*REST Countries* y *ExchangeRate*).
* **Módulo de Reportes Anuales**: Pantallas para analizar y visualizar rendimientos financieros por periodos con métricas fiables.
* **Mecanismos Offline Resilientes**: Trabajo ininterrumpido sin internet, donde se insertan propiedades y pagos en el caché local a ser insertados a Google Cloud silenciados para sincronizaciones en back-ground.
* **Notificaciones Push Contextuales:** Envío y gestión de estado mediante Firebase Cloud Messaging para alertas globales del sistema al usuario.
* **Multi-idioma (i18n)**: La interfaz responde de manera automática reaccionando al entorno entre variables localizadas como Español e Inglés nativo.

---

## 🛠️ Stack Tecnológico Utilizado

El proyecto utiliza las últimas y mejores librerías de modernización Android dictadas por Google:

**Lenguajes y Herramientas Base**:
* **Lenguaje:** Kotlin (versión 1.9+).
* **Versión de SDK de compilación:** Android API 35 (Para Android 15), compatibilidad mínima API 28.
* **Compatibilidad de Máquina Virtual:** Java SDK 17.

**Arquitectura y Diseño**:
* **Arquitectura de Software:** MVVM (Model-View-ViewModel).
* **Gestión de Interfaz de Usuario:** Jetpack Compose, Material 3, Navigation Compose.
* **Cargado Asíncrono de Imágenes:** Coil Compose.

**Manejo de Datos y Red (Backend & DB):**
* **Base de Datos Local (SQL):** AndroidX Room Database.
* **Programación Asíncrona:** Kotlin Coroutines.
* **Manejo de estados Persistentes Config:** Jetpack DataStore Preferences.
* **Redes y Peticiones API:** Retrofit v2, JSON Gson Converter.
* **Plataforma Backend as a Service (BaaS):** Ecosistema Firebase.
  * Firebase Authentication (Control Usuario).
  * Firebase Cloud Firestore (NoSQL, persistencia).
  * Firebase Cloud Messaging (Notificaciones push).
  * Firebase Analytics (Telemetría).

**APIs Nativas y de 3eros:**
* **Mapas:** OSmdroid v6.1 (Alternativa a Google Maps SDK).
* **Planificador de Tareas:** AndroidX WorkManager (para Sync data background).
* **Biometría:** AndroidX Biometrics.
* **Inicio de Sesión:** Google Credential Manager (sin obsolescencias de Sign-In viejo).

**Herramientas de Test Avanzadas (QA)**:
* Integración a testing modular con **JUnit 4** y **MockK**.
* **Turbine** y **Coroutines Test** para análisis de flujos secuenciales y asíncronos.
* **Espresso** y **Compose UI Tests** para pruebas de interfaz instrumentadas.
* Automatización de pruebas a Room Database.
