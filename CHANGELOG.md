# Inicio 0.7.0: «Editar inicio» al pellizcar

- **Pellizca la pantalla de inicio con dos dedos** (en cualquier diseño) y entras en **«Editar inicio»**, como en el
  lanzador de tu teléfono: tu inicio se ve en pequeño y abajo aparecen cuatro botones. También desde el menú al mantener
  pulsado el fondo. Tocar el inicio en pequeño o «Atrás» sale.
  - **Fondo de pantalla**: el selector de fondos de Android.
  - **Widgets**: el diseño (Widgets, Clásico o Lista), reloj y fecha, vista previa del calendario, apps sugeridas y
    «Editar esta vista» (las tarjetas de «Hoy»). Los cambios se ven al momento en el inicio en pequeño.
  - **Estilo de iconos**: la forma de todos los iconos (Original, Círculo, Redondeado o Cuadrado) en el inicio, el dock y
    el cajón, y cuántas apps por fila (4 o 5).
  - **Ajustes**: los Ajustes de Inicio.
- 22 pruebas.

# Inicio 0.6.0

- **Nuevo diseño «Widgets»** (el de fábrica): reloj de agujas con segundero rojo, cuatro apps de la familia en círculos
  (la última en el color de acento), tarjeta con la fecha, la próxima alarma en un círculo, el dock en círculos y
  «Buscar» abajo. Tocar el reloj o la alarma abre Reloj; la fecha, Calendario.
- «Clásico» y «Lista» siguen en Ajustes › Pantalla de inicio › Diseño. Si ya habías elegido uno, se mantiene.

# Inicio 0.5.2

- **Sin puntos rojos**: las apps de la familia ya no llevan un punto en su icono.

# Inicio 0.5.1

- **Conectar todas** (Gateway Hub 0.0.21): con un toque en el Hub se activa aquí «Redireccionar a Hub» y la app queda
  vinculada, sin entrar en ella. Solo lo puede pedir una app firmada por el mismo desarrollador.
- **Lucía** (la voz del Hub) usa las herramientas de esta app en el teléfono, **sin internet**, con las mismas reglas que
  por MCP. Solo mientras «Redireccionar a Hub» esté activado.
- **Icono de la app**: 5 iconos para elegir (Original, Claro, Dorado, Azul y Verde) en Ajustes › Temas › Icono de la app. Tras esta
  actualización quizá tengas que volver a poner el icono en tu pantalla de inicio (una sola vez).

# Inicio 0.5.0

- **Notas** y **Documentos** en vivo en «Hoy» con Notas 0.5.0 (tareas pendientes y últimas notas) y Filem 0.4.0
  (documentos recientes). Si una app no tiene nada que contar: «No tienes pendientes».
- **Skeleton loader**: mientras «Hoy» pregunta a las apps, tarjetas grises con un brillo que las recorre.
- 17 pruebas.

# Inicio 0.4.0

- **Diseño «Lista»** (Ajustes › Pantalla de inicio › Diseño: Clásico / Lista): reloj grande, fecha y el primer evento
  de hoy; tus favoritas en lista con su nombre (el dock primero y, si quieres, completada con tus apps más abiertas
  hasta 8); el **alfabeto a la derecha**: desliza el dedo y las letras se apartan como una onda; al soltar (o tocar) se
  abre la lista de apps en esa letra, con las demás atenuadas. Botón de búsqueda abajo; mantener pulsado abre Ajustes.
- **Cajón en lista** con letras de sección (en el diseño «Lista»).
- **Ordenar según su uso** dentro de cada letra (Ajustes › Cajón de apps).
- **Vista previa del calendario** bajo el reloj y **Sugerir apps frecuentes** (Ajustes › Pantalla de inicio).
- 16 pruebas.

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
