# Informe de Análisis del Proyecto: RentApp

Este documento proporciona una visión detallada de la arquitectura, diseño y propósito de la aplicación móvil **RentApp**. Está estructurado para servir tanto a desarrolladores que entran al proyecto como a stakeholders, gerentes de producto o inversores.

---

## 📖 Descripción No Técnica (Para Usuarios y Negocios)
**RentApp** es una herramienta integral, innovadora y moderna diseñada exclusivamente para **arrendadores y propietarios**. Permite administrar de manera fácil y rápida propiedades de alquiler directamente desde un dispositivo móvil. 

Con esta aplicación, los propietarios pueden registrar inmuebles, gestionar inquilinos, crear contratos digitales y emitir recibos de pago. Su característica más impresionante es que **permite trabajar sin conexión a internet** (Offline-First); además, permite generar documentos profesionales en formato PDF y Excel (CSV) para reportes anuales, contratos y comprobantes de pago. Todo esto bajo un estricto blindaje de seguridad biométrica.

---

## ⚙️ Descripción Técnica (Para Desarrolladores)
Se trata de una aplicación Android nativa, modular y escalable, desarrollada íntegramente en **Kotlin** siguiendo una estricta arquitectura **MVVM (Model-View-ViewModel)** y **Unidirectional Data Flow (UDF)**. 

La interfaz de usuario está desarrollada con **Jetpack Compose**, en un entorno completamente declarativo, y emplea la biblioteca Material 3 para los últimos estándares visuales. Utiliza **Coroutines y Flow** extensamente para reaccionar asíncronamente a los cambios de estado sin bloquear el hilo principal. 

A nivel de datos e infraestructura, el proyecto implementa un patrón robusto tipo híbrido (*"Local Source of Truth"*). Se gestionan bases de datos relacionales en el dispositivo por medio de **Room** (SQLite) apoyándose en **Firebase Firestore** para replicación remota. La sincronización local-nube es bidireccional, llevada a las sombras (background) con **Android WorkManager** para persistir cuando no hay acceso a la red, apoyada por una directiva de prevención de conflictos *"Last Write Wins"*. Adicionalmente, el proyecto evita costos y ataduras a licenciamientos de Google Maps, implementando integraciones cartográficas descentralizadas con **OSmdroid**.

---

## 🎯 Funcionalidades Principales de la App

* **Enfoque Landlord-Only**: UX optimizada exclusivamente para las necesidades del arrendador, eliminando distracciones y simplificando la gestión de cobros.
* **Generación de Documentos Digitales**: Creación instantánea de reportes anuales, contratos de alquiler y recibos de pago en formato PDF profesional, guardados directamente en la carpeta de descargas.
* **Exportación Analítica**: Capacidad de exportar datos financieros a formatos CSV (Excel) para un control contable externo detallado.
* **Autenticación Moderna Integral**: Inicio de sesión ágil con Google Sign-In mediante la nueva API Android Credential Manager y Firebase Auth.
* **Seguridad Biométrica Inteligente**: Control de acceso seguro requiriendo lectura de huella dactilar o reconocimiento facial.
* **Gestión de Propiedades Eficiente**: Capacidad para agregar, listar y editar unidades de bienes raíces con seguimiento de estado (Disponible/Rentada).
* **Geolocalización en Mapas Libre**: Incorporación de un visor de mapa dinámico basado en OSmdroid para ubicar propiedades con precisión.
* **Administración Financiera y Auditoría**: Seguimiento de pagos mensuales con recordatorios automáticos gestionados por WorkManager para alertar sobre morosidad o próximos vencimientos.
* **Módulo de Reportes Visuales**: Gráficos dinámicos y estadísticas de rendimiento financiero anual para una toma de decisiones informada.
* **Mecanismos Offline Resilientes**: Trabajo ininterrumpido sin internet con sincronización bidireccional automática hacia la nube.
* **Multi-idioma (i18n)**: Soporte completo para Español, Inglés, Portugués, Francés y Alemán.

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
