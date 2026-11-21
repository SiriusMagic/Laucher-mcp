# Inicio (launcher MCP): reglas para agentes

- Lee `FAMILIA.md` (el contrato común de todas las apps), luego `ARQUITECTURA.md`, y abre solo los archivos que necesites.
- Pruebas: `cd android && ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleRelease` (debe pasar todo).
  Nunca desactives ni saltes una prueba. Lint sin avisos nuevos.
- Cada versión: sube `versionCode` y `versionName`, copia el APK a `android/Inicio-<versión>.apk` (borra el anterior)
  y anótalo en `CHANGELOG.md`.
- Reglas puras (orden, letras, búsqueda, familia, dock) en `data/`, sin Android y con pruebas. Las pantallas solo pintan
  y avisan. Todo lo que toca el sistema (apps, iconos, intents) vive en `Platform.kt`.
- Inicio nunca abre, instala ni desinstala apps por su cuenta ni por MCP: solo el usuario, con un toque. Las
  herramientas MCP solo leen la lista de apps y ordenan el dock.
- Sin `QUERY_ALL_PACKAGES`: solo se ven las apps con entrada de launcher (`<queries>`).
- Privacidad: dock, apps ocultas y ajustes viven solo en el teléfono (fuera de copias). Nada sale salvo por el Modo MCP
  que el usuario active; internet es solo para eso.
- Cada herramienta MCP nueva lleva su prueba en `McpTest`: lista de herramientas, llamada correcta, argumentos malos y
  «no encontrado». Las que cambian algo exigen token de lectura y escritura (`READ_ONLY` si no).
- Prohibido: `eval`, motores de scripts, SQL concatenado.
- Firma: la de depuración del entorno, igual que el resto de la familia y Gateway Hub.
