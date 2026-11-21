# Arquitectura de Inicio

Carpeta = package (`android/app/src/main/java/com/zirakusa/launcher/`):

| Carpeta | Qué hay |
|---|---|
| `data/` | Lógica pura, con pruebas. `AppEntry` (paquete, actividad, nombre y módulo MCP si es de la familia) y `Apps` (orden A-Z sin acentos, letra, búsqueda con prefijos primero, familia, visibles, por paquete); `Dock` (hasta 5, añadir, quitar, mover, limpiar desinstaladas, primer relleno); `Usage` (frecuentes y recientes), `FamilyCard` (tarjeta de «Hoy») y `Greeting`; `Settings` (dock, ocultas, columnas, familia primero, fondo, reloj, temas sobre `KeyValues`). |
| `ui/` | `HomeState` (único dueño del estado: apps, dock, ocultas, búsqueda, menú de una app, reloj, próxima alarma y ajustes) y `LauncherApp` (inicio, cajón o ajustes, con el menú encima; `AppActions`). |
| `ui/screens/` | `HomeScreen` (reloj, fecha y próxima alarma; familia; «Buscar apps»; dock; deslizar arriba; mantener pulsado: fondo y Ajustes), `DrawerScreen` (buscar, Recientes, Familia, todas e índice A-Z), `FeedScreen` («Hoy»), `WelcomeScreen` (bienvenida), `AppIcon` (`AppImage` con icono o letra y punto de familia, `AppCell`, `AppMenu`), `SettingsScreen` (y Apps ocultas), `McpScreen` (igual que en las apps hermanas), `SettingsParts` y `Chrome`. Solo pintan y avisan. |
| `ui/theme/` | `Theme`: `ThemeMode`, `Accent` (rojo de fábrica…) y `HomeColors`. |
| `mcp/` | Modo MCP como la familia: `Tokens` (`ini_…`), `SecretBox`, `LauncherTools` (`launcher_apps`, `launcher_dock`; con escritura `launcher_dock_add`, `launcher_dock_remove`; a través de `LauncherData`), `McpHandler`, `RelayLink`, `McpMode` (`LiveData`) y `HubLink` (módulo `launcher_`). |
| `Platform.kt` | `PhoneApps`: apps con entrada de launcher y su módulo de familia (meta-data de Gateway Hub), iconos en caché, intents (abrir, información, desinstalar), primer dock (teléfono, mensajes, cámara, navegador), resúmenes de familia (`summaries`), próxima alarma del sistema y abrir Reloj o Calendario. |
| `MainActivity.kt` | La pantalla de inicio (`HOME`): lee las apps (y otra vez al instalar o quitar una), botón de inicio vuelve al inicio, pantalla de inicio predeterminada (`RoleManager`), fondo e «Ir al Hub». |

Reglas:
- El tema de la ventana muestra el fondo del teléfono; si el usuario lo apaga, se pinta el color del tema.
- Texto visible en `res/values/strings.xml` (español) y `values-en`. Colores desde `HomeColors`.
- `ScreenshotsTest` usa una lista de apps falsa (sin iconos: salen las letras) y el sábado 21 de noviembre de 2026, 9:41.
