# La familia: contrato común

Lo que comparten todas las apps (Stone, Luma, Calculadora, Notas, Filem, Brújula, Nivel, Reloj, Calendario, Inicio…),
para que cualquier IA las use igual y cualquier agente pueda crear una nueva sin adivinar. Inicio es la casa de la
familia: las reconoce por este contrato y las muestra juntas.

## 1. Identidad
- Paquete `com.zirakusa.<app>`; nombre corto en español (`Reloj`, `Calendario`).
- Firma: la de depuración del entorno (Gateway Hub reconoce al mismo desarrollador). Huella, cómo usarla desde otro
  entorno (GPT/Codex) y la lista de repos: [`GUIA-AGENTES.md`](GUIA-AGENTES.md).
- Kotlin + Compose, misma cadena de herramientas (AGP, Kotlin, BOM de Compose, OkHttp) en todas; se actualizan juntas.

## 2. Ser encontrada (Gateway Hub e Inicio)
En la actividad principal:
```xml
<intent-filter>
    <action android:name="com.zirakusa.gatewayhub.action.APP" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
<meta-data android:name="com.zirakusa.gatewayhub.module" android:value="<modulo>_" />
<meta-data android:name="com.zirakusa.gatewayhub.link" android:value="${applicationId}.hublink" />
```
Ser encontrada no enciende nada. El módulo es el prefijo de sus herramientas (`clock_`, `calendar_`, `launcher_`…).

## 3. Modo MCP
- Apagado de fábrica. «Activar MCP» o «Redireccionar a Hub», en **Ajustes › Modo MCP** (siempre en Ajustes).
- Mismo relay público de Stone (WebSocket saliente, solo https/wss); un id de dispositivo por app.
- Tokens: prefijo propio de 3 letras + 256 bits, cifrados con el Keystore (`SecretBox`),
  de tipo LLM, Hub o Redirect, lectura o lectura y escritura, con caducidad, pausa y revocar.
- `McpHandler`: JSON-RPC (Streamable HTTP), token comparado en tiempo constante, 120 peticiones por minuto, 1 MiB máx.
- Herramientas: nombres `<modulo>_<verbo>`, argumentos con esquema estricto (`additionalProperties: false`), errores
  con código en `structuredContent.error.code`: `BAD_ARGUMENT`, `NOT_FOUND`, `READ_ONLY`, `UNKNOWN_TOOL` y los propios.
  Las que cambian algo no se listan a un token de solo lectura.
- Cada herramienta lleva prueba en `McpTest`: lista, llamada correcta, argumentos malos y «no encontrado».

## 4. Código
- `data/`: reglas puras, sin Android, con pruebas. `ui/`: un único estado (`…State`) y pantallas que solo pintan.
- Ajustes tipados sobre `KeyValues` (mapa en pruebas, preferencias en la app).
- Capturas con Roborazzi en `ScreenshotsTest` (fecha y hora fijas).
- Texto visible en `values` (español) y `values-en`. Colores desde el objeto de colores del tema.
- Pantallas de unas 150 líneas como mucho.

## 5. Privacidad
- Sin anuncios ni estadísticas. `allowBackup=false` y reglas que excluyen todo de las copias.
- Internet solo para el Modo MCP. Nada sale del teléfono si el usuario no lo activa.

## 6. Versiones
- `versionCode` y `versionName` suben juntos; el APK firmado se copia a `android/<App>-<versión>.apk` (se borra el
  anterior) y se anota en `CHANGELOG.md`. Pruebas, lint sin avisos y `assembleRelease` antes de cada versión.

## 7. Resumen de familia (la pantalla «Hoy» de Inicio)
Una app puede contar en una tarjeta qué tiene ahora (Reloj: próxima alarma; Calendario: eventos de hoy):
```xml
<permission android:name="com.zirakusa.family.permission.SUMMARY" android:protectionLevel="signature"
    android:label="@string/summary_permission" />
<!-- en la actividad principal, junto al módulo de Gateway Hub -->
<meta-data android:name="com.zirakusa.family.summary" android:value="${applicationId}.summary" />
<provider android:name=".SummaryProvider" android:authorities="${applicationId}.summary"
    android:exported="true" android:permission="com.zirakusa.family.permission.SUMMARY" />
```
- `call("summary")` devuelve un `Bundle`: `title` (texto), `lines` (`ArrayList<String>`, hasta 5 líneas cortas) y
  `empty` (lo que se muestra si no hay líneas). Nada más: ni datos crudos ni identificadores.
- Solo responde a apps firmadas por el mismo desarrollador (permiso de firma y `checkSignatures` en el proveedor).
- Las líneas salen de una función pura con prueba (`SummaryLines`). La autoridad debe empezar por el paquete de la app.
- Lo usan Reloj 0.2.0, Calendario 0.2.0, Notas 0.5.0 y Filem 0.4.0. Si no hay líneas, el texto `empty` (por
  defecto «No tienes pendientes»). Inicio también lo ofrece a la IA con `launcher_today`.

## 8. Registro de prefijos (únicos)
Gateway Hub reconoce de qué app es un token por su prefijo, así que **nunca se repite**. Antes de crear una app, añade
aquí el suyo:

| Prefijo | App | Módulo del hub |
|---|---|---|
| `stn_` | Stone | `music_` |
| `lum_` | Luma | `gallery_` |
| `cal_` | Calculadora | `calc_` |
| `not_` | Notas | `notes_` |
| `fil_` | Filem | `files_` |
| `bru_` | Brújula | `compass_` |
| `niv_` | Nivel | `level_` |
| `rel_` | Reloj | `clock_` |
| `cld_` | Calendario (desde 0.3.0; antes `cal_`, repetido) | `calendar_` |
| `ini_` | Inicio | `launcher_` |
| `grb_` | Grabadora | `rec_` |
| `stv_` | Stone Video (desde 0.4.0; antes `stn_`, repetido) | — |
| `qrc_` | QR | `qr_` |
| `cli_` | Clima | `weather_` |
| `red_` | Redes (Analizar redes) | `network_` |
| `con_` | Contactos | `contacts_` |
| `cam_` | Cámara | `camera_` |
| `tel_` | Teléfono | `phone_` |
| `sms_` | SMS | `sms_` |


## 9. Seguridad del token del propietario del hub
`HUB_TOKEN` es el único secreto que elige una persona y abre el hub y, a través de él, los teléfonos. Desde Gateway Hub
0.0.3 el hub no abre con un token débil (menos de 32 caracteres, poca variedad o secuencias). Créalo con
`openssl rand -base64 32 | npx wrangler secret put HUB_TOKEN`. Los tokens de las apps son siempre aleatorios (256 bits).

## 10. Órdenes entre apps (Asistente)
El Asistente (en la app de Gateway Hub) habla con las apps como lo haría cualquier asistente de Android. Cada app acepta
las **órdenes estándar** que le tocan y, cuando no hay una estándar, la **orden de la familia**:

| Orden | Quién la acepta | Qué hace |
|---|---|---|
| `android.media.action.MEDIA_PLAY_FROM_SEARCH` (`query` vacío = aleatorio) | Stone | Reproduce: todo en aleatorio, o lo que coincida con `query` |
| `com.zirakusa.family.action.PLAY_RECOMMENDED` | Stone | Reproduce las recomendaciones del día |
| `android.intent.action.SET_ALARM` / `SET_TIMER` (`EXTRA_SKIP_UI`) | Reloj | Crea la alarma o el temporizador |
| `android.intent.action.DIAL` (`tel:`) | Teléfono | Abre el número en el teclado; la persona pulsa «Llamar» |
| `android.intent.action.SENDTO` (`smsto:`, `sms_body`) | SMS | Abre la conversación con el texto; la persona pulsa «Enviar» |
| `android.media.action.STILL_IMAGE_CAMERA` | Cámara | Abre la cámara (nunca dispara sola) |

Reglas:
- Las órdenes de la familia (`com.zirakusa.family.action.*`) se declaran en una actividad con
  `android:permission="com.zirakusa.family.permission.ACT"` (firma): solo las apps firmadas por el mismo desarrollador
  pueden enviarlas. La app que las envía también usa ese permiso; cualquiera de las dos lo declara.
- Llamar, enviar y fotografiar **siempre** terminan en un toque de la persona.
- El Asistente lee los resúmenes de §7 para responder («¿qué tiempo hace?», «¿qué tengo hoy?»).
- Si una app aún no acepta una orden, el Asistente la abre y sigue: nada se rompe con versiones viejas.
