# Inicio (launcher MCP)

La pantalla de inicio de la familia (Stone, Luma, Calculadora, Notas, Filem, Brújula, Nivel, Reloj, Calendario…): las
reúne, sabe cuáles hablan MCP, y tu IA puede ver tus apps y ordenar el dock.

## Estado (0.1.0)

- **Inicio:** reloj grande, fecha y próxima alarma (tocar abre Reloj o Calendario), fila **Familia** con las apps que
  hablan MCP (punto de color), «Buscar apps» y el **dock** (hasta 5). Deslizar hacia arriba abre todas las apps;
  mantener pulsado el fondo: cambiar fondo de pantalla y Ajustes. Usa el fondo del teléfono.
- **Cajón:** buscar sin acentos (Ir abre la primera), Familia primero y todas de la A a la Z en 4 o 5 columnas.
- **Mantener pulsada una app:** añadir o quitar del dock, ocultar, información y desinstalar (con la pregunta de
  Android).
- **Ajustes:** pantalla de inicio predeterminada, reloj y fecha, fondo del teléfono, apps por fila, familia primero,
  apps ocultas, **Modo MCP**, Temas, privacidad y versión.
- **Modo MCP:** `launcher_apps` y `launcher_dock`; con token de lectura y escritura, `launcher_dock_add` y
  `launcher_dock_remove`. Nunca abre ni desinstala apps. Módulo `launcher_` en Gateway Hub.
- Sin anuncios ni estadísticas; sin `QUERY_ALL_PACKAGES`; internet solo para el Modo MCP (apagado de fábrica).

APK: [`android/Inicio-0.1.0.apk`](android/Inicio-0.1.0.apk) (`com.zirakusa.launcher`). Tras instalar, pulsa el botón
de inicio y elige **Inicio**, o ve a Ajustes › Pantalla de inicio predeterminada.

El contrato común de la familia está en [`FAMILIA.md`](FAMILIA.md).

## Compilar y probar

```bash
cd android
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

## Siguiente

Ver [`ROADMAP.md`](ROADMAP.md).
