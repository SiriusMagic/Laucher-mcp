package com.zirakusa.launcher.data

/** The «Widgets» home's rules: the analog clock's hands and which apps get the round buttons. Pure, with tests. */
object Widgets {
    /** Round buttons next to the clock. */
    const val BUTTONS = 4

    /** Angles in degrees, clockwise from 12, for the hour, minute and second hands. The hands move smoothly. */
    data class Hands(val hour: Float, val minute: Float, val second: Float)

    fun hands(hour: Int, minute: Int, second: Int): Hands =
        Hands((hour % 12) * 30f + minute * 0.5f + second / 120f, minute * 6f + second * 0.1f, second * 6f)

    /** The family first; if there are fewer than [BUTTONS], the most opened apps fill in (never one already in the dock). */
    fun buttons(family: List<AppEntry>, frequent: List<AppEntry>, dock: List<AppEntry>): List<AppEntry> {
        val dockKeys = dock.map { it.key }.toSet()
        return (family + frequent.filter { it.key !in dockKeys }).distinctBy { it.key }.take(BUTTONS)
    }
}
