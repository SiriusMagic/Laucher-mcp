# Arquitectura de Inicio

Carpeta = package (`android/app/src/main/java/com/zirakusa/launcher/`):

| Carpeta | Qué hay |
|---|---|
| `data/` | Lógica pura, con pruebas. `AppEntry` (paquete, actividad, nombre y módulo MCP si es de la familia) y `Apps` (orden A-Z sin acentos, letra, búsqueda con prefijos primero, familia, visibles, por paquete); `Dock` (hasta 5, añadir, quitar, mover, limpiar desinstaladas, primer relleno); `Usage` (frecuentes y recientes), `Widgets` (agujas del reloj y los cuatro botones redondos), `FamilyCard` (tarjeta de «Hoy»), `Greeting`, `ListStyle` (`HomeStyle`, favoritas, orden por uso, letras, onda del alfabeto) y `Cards` (`CardKind`, `Slot`: orden, visibilidad y vivo/vista previa de las tarjetas); `Settings` (dock, ocultas, columnas, familia primero, fondo, reloj, temas sobre `KeyValues`). |
| `ui/` | `HomeState` (único dueño del estado: apps, dock, ocultas, búsqueda, menú de una app, reloj, próxima alarma y ajustes) y `LauncherApp` (inicio, cajón o ajustes, con el menú encima; `AppActions`). |
| `ui/screens/` | `WidgetsHome` (diseño «Widgets», el de fábrica: reloj de agujas, botones redondos, fecha, próxima alarma, dock redondo y «Buscar»), `HomeScreen` (gestos y menú; diseño «Clásico»: reloj, fecha y próxima alarma; familia; «Buscar apps»; dock; deslizar arriba; mantener pulsado: fondo y Ajustes), `DrawerScreen` (buscar, Recientes, Familia, todas e índice A-Z), `ListHome` (diseño «Lista» y `AlphabetBar`), `FeedScreen` («Hoy» con ⋯ y vista previa), `CardsScreen` (Editar esta vista), `WelcomeScreen` (bienvenida), `AppIcon` (`AppImage` con icono o letra, `AppCell`, `AppMenu`), `SettingsScreen` (categorías con icono y sus páginas; Apps ocultas), `McpScreen` (igual que en las apps hermanas), `SettingsParts` y `Chrome`. Solo pintan y avisan. |
| `ui/theme/` | `Theme`: `ThemeMode`, `Accent` (rojo de fábrica…) y `HomeColors`. |
| `mcp/` | Modo MCP como la familia: `Tokens` (`ini_…`), `SecretBox`, `LauncherTools` (`launcher_apps`, `launcher_dock`; con escritura `launcher_dock_add`, `launcher_dock_remove`; a través de `LauncherData`), `McpHandler`, `RelayLink`, `McpMode` (`LiveData`) y `HubLink` (módulo `launcher_`). |
| `Platform.kt` | `PhoneApps`: apps con entrada de launcher y su módulo de familia (meta-data de Gateway Hub), iconos en caché, intents (abrir, información, desinstalar), primer dock (teléfono, mensajes, cámara, navegador), resúmenes de familia (`summaries`), próxima alarma del sistema y abrir Reloj o Calendario. |
| `MainActivity.kt` | La pantalla de inicio (`HOME`): lee las apps (y otra vez al instalar o quitar una), botón de inicio vuelve al inicio, pantalla de inicio predeterminada (`RoleManager`), fondo e «Ir al Hub». |

Reglas:
- El tema de la ventana muestra el fondo del teléfono; si el usuario lo apaga, se pinta el color del tema.
- Texto visible en `res/values/strings.xml` (español) y `values-en`. Colores desde `HomeColors`.
- `ScreenshotsTest` usa una lista de apps falsa (sin iconos: salen las letras) y el sábado 21 de noviembre de 2026, 9:41.


«Editar inicio» (0.7.0): `data/EditMode.kt` (pura: `IconShape` y `Pinch`, el pellizco con dos dedos), `ui/screens/EditHome.kt` (`pinchIn`, el inicio en pequeño y los cuatro botones) y `EditSheets.kt` (Widgets y Estilo de iconos). La forma de los iconos la aplica `Platform.icon` recortando las capas del icono adaptable; llega a todas las pantallas por `LocalIconShape`.