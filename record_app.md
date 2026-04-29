# Historial de Cambios y Ajustes (RentApp)

Este archivo sirve como bitácora para registrar las modificaciones, correcciones, nuevas implementaciones y ajustes realizados en el proyecto a lo largo del tiempo.

---

## [2026-04-21] Corrección de Persistencia de Sesión
**Componente:** Navegación / Autenticación (`RentAppNavGraph.kt`)
* **Problema:** La aplicación no mantenía la sesión activa al cerrarla y volverla a abrir; siempre solicitaba iniciar sesión al iniciar debido a que el destino inicial de la navegación (`startDestination`) estaba quemado en el código hacia `Screen.Login.route`.
* **Solución:** Se integró la comprobación del usuario autenticado de Firebase (`FirebaseAuth.getInstance().currentUser`) al momento de construir el grafo de navegación.
* **Resultado:** Si el usuario ya está autenticado, la app ahora inicia directamente en `Screen.Dashboard.route`, omitiendo la pantalla de Login y ofreciendo una experiencia fluida.

## [2026-04-24] Ampliación de Internacionalización y Fix de Mapas
**Componente:** Perfil (`UserProfileScreen`), Localización (`AddPropertyScreen`)
* **Mejora i18n:** Se agregaron los idiomas Portugués (`pt`), Francés (`fr`) y Alemán (`de`) a la aplicación, actualizando también el selector de idiomas en la pantalla de perfil del usuario.
* **Problema:** Al buscar o seleccionar una ubicación en el mapa de agregar propiedad, la interfaz sufría un congelamiento ("glitch").
* **Solución:** Se movieron las llamadas a la clase `Geocoder` al hilo de background (`Dispatchers.IO`) ya que son operaciones de red/disco que bloqueaban el hilo principal de la UI.

## [2026-04-24] Correcciones Múltiples (RFC, Theme, Glitch, Crash)
**Componente:** Perfil (`UserProfileScreen`), Localización (`AddPropertyScreen`), Tema (`Theme.kt`)
* **Mejora UI:** Se eliminó el campo RFC del formulario del perfil de usuario ya que no era estrictamente necesario para la vista principal.
* **Corrección de Tema:** El interruptor (Toggle) de "Tema Oscuro/Claro" en la configuración del perfil no funcionaba. Se implementó el esquema de colores claro (`NeonGridLightColorScheme`), se agregó la bandera `DARK_MODE_ENABLED` a `PreferencesManager` y ahora `MainActivity` reacciona dinámicamente al cambio en tiempo real.
* **Fix Glitch de Mapa:** Se arregló un problema donde al buscar la ubicación el mapa no se actualizaba correctamente de forma inmediata, llamando a `mapView.invalidate()` tras mover el marcador desde las corrutinas asíncronas.
* **Fix Crash:** Se solucionó el cierre repentino de la app al guardar una nueva propiedad. Esto sucedía porque la conversión del ID pasaba un entero `0` a la entidad de base de datos que espera un tipo `Long` estricto en el compilador de Kotlin, lo que provocaba un error de inicialización en Room. Se arregló pasando un literal `0L`.

## [2026-04-27] Eliminación de Inquilinos y Propiedades con Deslizamiento
**Componente:** `TenantListScreen.kt`, `PropertyListScreen.kt`, ViewModels correspondientes
* **Nueva función:** Se implementó la eliminación de inquilinos y propiedades no deseados directamente desde sus listas respectivas.
* **Swipe-to-delete:** El usuario desliza cualquier tarjeta hacia la izquierda; aparece un fondo rojo con la etiqueta "Eliminar" y un ícono de papelera que se intensifica al alcanzar el umbral.
* **Diálogo de confirmación:** Se muestra un `AlertDialog` con el nombre del elemento antes de borrar definitivamente, evitando eliminaciones accidentales.
* **Deshacer (Undo):** Tras confirmar, un `Snackbar` con botón "Deshacer" permite restaurar el registro eliminado en segundos.
* **Fluidez:** La tarjeta siempre vuelve a su posición original tras deslizarla — el borrado real solo ocurre al pulsar "Eliminar" en el diálogo, protegiendo contra gestos accidentales.
* **[Fix] Eliminación permanente en Firestore:** Se detectó que al reiniciar la app los registros eliminados volvían a aparecer porque solo se borraban de la base de datos local (Room) pero **no de Firestore**. El listener de sincronización en tiempo real los re-insertaba al arrancar. Se añadieron los métodos `deleteProperty` y `deleteTenant` en `FirestoreSyncManager` que eliminan el documento de la colección remota, y se conectaron desde `PropertyRepository` y `TenantRepository`. Ahora la eliminación es permanente en local y en la nube.

## [2026-04-27] Pulido de Interfaz y Estadísticas Reales
**Componente:** Dashboard (`DashboardScreen.kt`), Listas (`PropertyListScreen`, `TenantListScreen`), ViewModels
* **[UI] Dashboard Premium:** Se finalizó el rediseño del Dashboard con efectos de cristal (glassmorphism), gradientes suaves y saludos dinámicos basados en la hora del día. Se incluyó un indicador "Live" pulsante para los ingresos mensuales.
* **[Mejora] Conversión de Moneda:** Se implementó la conversión en tiempo real de USD a BOB (Bolivianos) en el Dashboard del Casero, utilizando tasas de cambio dinámicas.
* **[UI] Limpieza de Listas:** Se eliminaron los botones flotantes (FAB) de las listas de Propiedades e Inquilinos para mejorar la visibilidad y evitar que obstruyan el contenido, devolviendo la sensación de "visualización libre".
* **[UI] Acciones Rápidas Mejoradas:** Se agregaron botones dedicados para "Nuevo Inmueble" y "Nuevo Inquilino" en el Dashboard, manteniendo la accesibilidad a las funciones de creación sin saturar las listas.
* **[Dashboard] Estadísticas Dinámicas:** Se integró el `ContractViewModel` para mostrar cifras reales de contratos activos y pagos realizados en el Dashboard del Inquilino, sustituyendo los valores estáticos.
* **[Refactor] Arquitectura:** Se actualizó el grafo de navegación para inyectar correctamente las dependencias de ViewModels necesarias para las nuevas funcionalidades.

## [2026-04-24] Correcciones Visuales y de Formulario (Fotos, Inquilinos, Días)
**Componente:** Listas (`PropertyListScreen`, `TenantListScreen`), Formulario Inquilino (`AddTenantScreen`)
* **Mejora UI:** Ahora se muestran las fotos de las propiedades (`PropertyListScreen`) y las fotos de los inquilinos (`TenantListScreen`) utilizando `AsyncImage` de Coil en sus respectivas tarjetas.
* **Ajuste Formulario:** Se retiró el campo "Tipo de identificación" de la pantalla de agregar inquilino.
* **Fix Campo Numérico:** Se arregló el comportamiento del campo "Día de Vencimiento de Pago Mensual" para permitir borrar el número e introducir un nuevo valor libremente, sin forzar un "1" por defecto de manera incorrecta.

## [2026-04-28] Flujo de Renta Unificado y Contrato Digital

**Componente:** `AddContractScreen.kt`, `ContractDetailScreen.kt`, `PropertyDetailScreen.kt`, `TenantDetailScreen.kt`, `RentAppNavGraph.kt`

### Flujo de Renta Unificado (Eliminación del paso intermedio)
* **Problema:** El flujo de renta requería pasar por una pantalla intermedia ("Agregar Inquilino") antes de crear el contrato, lo que resultaba en una experiencia fragementada e innecesariamente compleja.
* **Solución:** Se eliminó el paso intermedio. El botón **"Rentar Propiedad"** en `PropertyDetailScreen` ahora navega directamente a `AddContractScreen`, omitiendo `AddTenantScreen`.
* **Selección dual de inquilino:** Dentro de `AddContractScreen` se añadió un toggle de modo (`Inquilino Existente` / `Nuevo Inquilino`) que permite al usuario elegir un inquilino de la lista o registrar uno nuevo — todo en la misma pantalla.
* **Creación atómica:** Al guardar, si el modo es "Nuevo Inquilino", la pantalla primero inserta al inquilino en la BD (obteniendo su ID), y luego crea el contrato vinculado. Ambas operaciones ocurren en secuencia dentro de una misma corrutina.
* **Actualización de estado de propiedad:** Tras crear el contrato exitosamente, `PropertyViewModel.updatePropertyStatus` cambia el estado de la propiedad a `"RENTED"` de forma automática.

### Pantalla de Detalle de Contrato Rediseñada
* **Nuevo diseño "Documento Digital":** Se reescribió `ContractDetailScreen` con una jerarquía visual profesional:
  * **Cabecera de documento:** Muestra el número de contrato (`№ {id}`), fecha de emisión y un badge de estado coloreado (`ACTIVO` / `VENCIDO`).
  * **Tarjetas de Partes:** Dos tarjetas lado a lado — "Propietario / Inmueble" e "Inquilino" — con icono, nombre y subtext.
  * **Términos Financieros:** Sección con renta mensual destacada, depósito de garantía y día de pago, cada uno con icono y colores diferenciados.
  * **Vigencia del Contrato:** Visualización de fecha inicio → fin con el total de días calculado automáticamente.
  * **Historial de Pagos:** Lista dinámica de pagos registrados con estado color-coded (`Pagado`, `Pendiente`, `Atrasado`).
  * **FAB contextual:** Botón flotante "Registrar Pago" disponible solo cuando el contrato está `ACTIVE`.
* **Firma de función corregida:** Se añadieron los parámetros `paymentRepo` y `onViewPayments` que el NavGraph requería.

### Correcciones de Compilación
* **`PropertyDetailScreen`:** `remember { }` estaba siendo invocado directamente dentro de `LazyListScope`, lo cual no es un contexto `@Composable` válido. Se sustituyó por llamadas `items(contracts.filter { ... })` inline.
* **`TenantDetailScreen`:** Mismo patrón `remember` inválido corregido. Además se eliminó la declaración duplicada de `var tenant` (conflicto con `val tenant` derivado del Flow) y se reemplazó el color inexistente `Warning` por `Tertiary`.
* **`ContractDetailScreen`:** Campo `Payment.paymentDate` inexistente corregido a `Payment.dueDate`. Llamadas `.isNullOrBlank()` en campos `String` no-nulos ajustadas a `.isNotBlank()`.

