package com.zirakusa.launcher.data

/** «Estilo de iconos»: the shape every app icon is cut to (the system's own, or Inicio's), as a share of its side. */
enum class IconShape(val key: String, val corner: Float) {
    SYSTEM("system", -1f), CIRCLE("circle", 0.5f), ROUNDED("rounded", 0.26f), SQUARE("square", 0.08f);

    companion object { fun from(key: String?) = entries.firstOrNull { it.key == key } ?: SYSTEM }
}

/**
 * The pinch that opens «Editar inicio» (pure, with tests): two fingers that start at least [MIN_START] px apart and come
 * together to [RATIO] of that distance. One finger (scroll, swipe, tap, hold) never counts.
 */
object Pinch {
    const val RATIO = 0.72f
    const val MIN_START = 120f

    fun closed(start: Float, now: Float, minStart: Float = MIN_START): Boolean = start >= minStart && now <= start * RATIO

    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float { val dx = x1 - x2; val dy = y1 - y2; return kotlin.math.sqrt(dx * dx + dy * dy) }
}
