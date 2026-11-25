# Guía para agentes (Claude, GPT/Codex u otro)

Cómo trabajamos en la familia, dónde está cada repo, qué leer y **cómo firmar** para que cada APK se instale
encima de la que ya está en el teléfono (sin apps repetidas ni datos perdidos). El contrato técnico común está en
[`FAMILIA.md`](FAMILIA.md); esta guía es el punto de entrada.

## 1. Qué leer y en qué orden
1. Esta guía.
2. `FAMILIA.md` (este repo): el contrato común de todas las apps.
3. En el repo donde vas a trabajar: `CLAUDE.md` (reglas cortas; valen igual para GPT), luego `ARQUITECTURA.md`.
   Stone y Stone Video además tienen `android-native/AGENTS.md` y `android-native/MAP.md` (un renglón por archivo).
   Gateway Hub tiene `MODULES.md` para crear módulos.
4. Abre solo los archivos que necesites; si uno pasa de ~200 líneas, busca la función y lee ese tramo.

> GPT/Codex lee `AGENTS.md`, no `CLAUDE.md`. Las reglas son las mismas: si el repo no tiene `AGENTS.md`, lee
> `CLAUDE.md` como si lo fuera.

## 2. Dónde están los repos
Todos en GitHub, cuenta `SiriusMagic` (`https://github.com/SiriusMagic/<repo>`), rama `main`. Estos son los nombres
actuales en GitHub; los viejos siguen funcionando porque GitHub redirige. La copia local puede llamarse en minúsculas
(`galer-a-mcp`, `contactos-mcp`…). La lista que usa Tienda está en `tienda-de-app/tools/update_catalog.py`.

| Repo | App | Paquete | APK |
|---|---|---|---|
| `Stone` | Stone (música) | `com.zirakusa.stone.kotlin.preview` | `android-native/`, habilidad `stone-apk-release` |
| `Stone-v-deo-mcp` | Stone Video | `com.zirakusa.stone.video` | `android-native/StoneVideo-<v>.apk` |
| `Gateway-Hub-MCP` | Gateway Hub (relay + app, Lucía) | `com.zirakusa.gatewayhub` | `npm test`; app en `android/` |
| `Laucher-mcp` | Inicio (launcher) | `com.zirakusa.launcher` | `android/Inicio-<v>.apk` |
| `Galer-a-mcp` | Galería (antes Luma) | `app.luma.gallery` | `android/Luma-<v>.apk` |
| `Calculadora.mcp` | Calculadora | `com.zirakusa.calculadora` | `android/Calculadora-<v>.apk` |
| `Notas.mcp` | Notas | `com.zirakusa.notas` | `android/Notas-<v>.apk` |
| `Filem.mcp` | Filem (archivos) | `com.zirakusa.filem` | `android/Filem-<v>.apk` |
| `Br-jula-mcp...` | Brújula | `com.zirakusa.brujula` | `android/Brujula-<v>.apk` |
| `Nivel-burbuja-mcp` | Nivel | `com.zirakusa.nivel` | `android/Nivel-<v>.apk` |
| `Reloj-mcp` | Reloj | `com.zirakusa.reloj` | `android/Reloj-<v>.apk` |
| `Calendario-mcp` | Calendario | `com.zirakusa.calendario` | `android/Calendario-<v>.apk` |
| `Qr-mcp` | QR | `com.zirakusa.qr` | `android/QR-<v>.apk` |
| `Clima-mcp` | Clima | `com.zirakusa.clima` | `android/Clima-<v>.apk` |
| `Analizador-de-redes-mcp` | Redes | `com.zirakusa.redes` | `android/Redes-<v>.apk` |
| `C-mara-mcp` | Cámara | `com.zirakusa.camara` | `android/Camara-<v>.apk` |
| `Tel-fono.mcp` | Teléfono (+ Contactos) | `com.zirakusa.telefono` | `android/Telefono-<v>.apk` |
| `SMS` | SMS | `com.zirakusa.sms` | `android/SMS-<v>.apk` |
| `Grabadora-mcp` | Grabadora | `com.zirakusa.grabadora` | `android/Grabadora-<v>.apk` |
| `tienda-de-app` | **Tienda** (antes `Contactos-mcp`) | `com.zirakusa.tienda` | `android/Tienda-<v>.apk` |

## 3. Cómo trabajamos
- Pruebas antes de entregar: `cd android && ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleRelease`
  (Stone y Stone Video: el comando de su `CLAUDE.md`). Todo debe pasar; nunca se desactiva ni se salta una prueba.
- Cada versión: sube `versionCode` **y** `versionName`, copia el APK a `android/<App>-<versión>.apk` (borra el
  anterior), anótalo en `CHANGELOG.md`. Después, en `tienda-de-app`: `python3 tools/update_catalog.py` y sube
  `catalog.json` para que Tienda la anuncie.
- Texto y documentos en español. Commits cortos y claros.
- Nada de secretos en Git (`*.keystore`, `*.jks`, `.env`, `local.properties` están ignorados).

## 4. Firma: para que se instale encima
Android solo actualiza una app si la nueva APK tiene **el mismo paquete**, un **`versionCode` mayor o igual** y
**exactamente la misma firma**. Con otra firma el teléfono dice «La app no está instalada» / «conflicto con un paquete
existente», y la única salida sería desinstalar (se pierden los datos). Además, Gateway Hub, Tienda, los resúmenes de
Inicio y las órdenes de la familia (`protectionLevel="signature"`) solo funcionan entre apps con la misma firma.

Todas las apps se firman con **la clave de depuración del entorno** (`~/.android/debug.keystore`; en el
`build.gradle.kts`, `signingConfig = signingConfigs.getByName("debug")`, también en `release`). La huella que deben
tener todas:

```
Certificado: C=US, O=Android, CN=Android Debug   (alias androiddebugkey, contraseña android)
SHA-256: 34:B8:87:16:BA:31:A8:CA:17:06:24:C1:C0:09:C1:1D:D2:3E:18:21:48:73:1F:08:89:C9:84:04:B3:41:82:D4
```

**Importante:** cada entorno nuevo (el de GPT/Codex también) genera *su propia* `debug.keystore` al primer build, con
otra huella. Hay que poner allí **el mismo archivo** antes de compilar:

1. El dueño guarda una copia del archivo `debug.keystore` (nunca en Git). En el entorno de Codex lo añade como
   *secret* `DEBUG_KEYSTORE_B64` (el archivo en base64).
2. En el *setup script* del entorno de Codex:
   ```bash
   mkdir -p ~/.android
   echo "$DEBUG_KEYSTORE_B64" | base64 -d > ~/.android/debug.keystore
   ```
3. Antes de entregar una APK, comprueba la huella:
   ```bash
   apksigner verify --print-certs android/<App>-<versión>.apk | grep SHA-256
   # debe decir 34b88716ba31a8ca170624c1c009c11dd23e182148731f0889c98404b34182d4
   ```
   Si no coincide: **no la entregues**. No cambies el `signingConfig`, no crees otra clave, no cambies el paquete.

Reglas para no duplicar apps:
- Nunca cambies `applicationId` ni añadas un sufijo nuevo (`applicationIdSuffix`). Los sufijos que ya existen
  (`.lucia` en Stone, `.lucia.preview` en Gateway Hub) son versiones de prueba a propósito.
- Nunca bajes el `versionCode`.
- Si la clave se pierde, ninguna app se podrá actualizar: guarda la copia en un lugar seguro.
