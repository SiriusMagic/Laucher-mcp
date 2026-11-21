# Inicio 0.3.0

- **Editar esta vista** («Hoy»): activar, ocultar y ordenar las tarjetas (Reloj, Calendario, Notas, Documentos, Apps
  frecuentes, Recientes y cualquier app de la familia con resumen). Desde el botón al final de «Hoy», el «⋯» de cada
  tarjeta (Ocultar tarjeta / Editar esta vista), mantener pulsado el inicio, o Ajustes › Hoy.
- **Vista previa**: las tarjetas cuya app falta o aún no da resumen se muestran como vista previa de lo que vendrá
  (Notas: tus últimas notas; Documentos: tus documentos recientes, con Filem).
- **Ajustes de Inicio** por categorías con icono: Configuración del sistema, Fondo, Temas, Pantalla de inicio, Cajón,
  Hoy, Modo MCP y Acerca de.
- **MCP**: `launcher_cards` (las tarjetas de «Hoy», en vivo o vista previa) y `launcher_card_set` (mostrar, ocultar y
  mover; con token de lectura y escritura). Tu IA puede organizar «Hoy» y el dock.
- 14 pruebas.

# Inicio 0.2.0

- **Bienvenida** la primera vez (4 pasos, «Omitir» siempre): qué es Inicio y que no pide ubicación, fotos, archivos ni
  cuentas; elegir hasta 5 apps favoritas para el dock; el fondo de pantalla; hacerlo pantalla de inicio predeterminada.
  Se puede ver otra vez desde Ajustes.
- **«Hoy»** (deslizar a la derecha en el inicio, o mantener pulsado › Hoy): saludo y fecha, una tarjeta por cada app de
  la familia con resumen (Reloj: próxima alarma, temporizador y cronómetro; Calendario: feriado y eventos de hoy), apps
  frecuentes y recientes. Tocar una tarjeta abre su app.
- **Resumen de familia**: el contrato nuevo en `FAMILIA.md` (sección 7), con permiso de firma.
- **Cajón:** fila «Recientes» (se puede apagar) e índice A-Z a la derecha.
- Inicio cuenta qué apps abres (solo en el teléfono) para frecuentes y recientes.
- MCP: `launcher_today` (las tarjetas de «Hoy» y las apps frecuentes).
- 12 pruebas.

# Inicio 0.1.0

Primera versión: la base del launcher de la familia.
- Pantalla de inicio (`HOME`) sobre el fondo del teléfono: reloj, fecha y próxima alarma (abren Reloj y Calendario),
  fila Familia, «Buscar apps» y dock de 5 (se llena la primera vez con teléfono, mensajes, cámara y navegador).
- Cajón con búsqueda sin acentos, Familia primero y todas A-Z en 4 o 5 columnas; menú de cada app (dock, ocultar,
  información, desinstalar).
- Reconoce las apps de la familia por su meta-data de Gateway Hub y muestra su módulo MCP.
- Ajustes: predeterminada, reloj, fondo, columnas, familia primero, apps ocultas, Modo MCP y Temas.
- Modo MCP como la familia (tokens `ini_…`, Redireccionar a Hub, módulo `launcher_`), con 4 herramientas.
- `FAMILIA.md`: el contrato común de todas las apps.
- 10 pruebas (lógica, MCP y capturas).
