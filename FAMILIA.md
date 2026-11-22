# La familia: contrato común

Lo que comparten todas las apps (Stone, Luma, Calculadora, Notas, Filem, Brújula, Nivel, Reloj, Calendario, Inicio…),
para que cualquier IA las use igual y cualquier agente pueda crear una nueva sin adivinar. Inicio es la casa de la
familia: las reconoce por este contrato y las muestra juntas.

## 1. Identidad
- Paquete `com.zirakusa.<app>`; nombre corto en español (`Reloj`, `Calendario`).
- Firma: la de depuración del entorno (Gateway Hub reconoce al mismo desarrollador).
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
- Tokens: prefijo propio de 3 letras + 256 bits (`rel_`, `cal_`, `ini_`…), cifrados con el Keystore (`SecretBox`),
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
