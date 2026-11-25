package com.zirakusa.launcher.data

import kotlin.math.abs
import kotlin.math.exp

/**
 * How the home screen looks: «Widgets» (analog clock, round buttons, date and alarm, round dock; the default since
 * 0.6.0), «Clásico» (clock, family row, dock) or «Lista» (clock, favourites as a list, alphabet). Stored by name.
 */
enum class HomeStyle { WIDGETS, CLASSIC, LIST }

/** The «Lista» design's rules. Pure, with tests. */
object ListStyle {
    /** Favourites shown as a list: the most people use 4 to 8. */
    const val FAVOURITES = 8

    /**
     * The home list: the dock's apps first, then (when [suggest] is on) the most opened apps not already there, up to
     * [FAVOURITES]. Suggested ones are marked, so the screen can show them a little dimmer.
     */
    fun favourites(dock: List<AppEntry>, frequent: List<AppEntry>, suggest: Boolean): List<Pair<AppEntry, Boolean>> {
        val mine = dock.take(FAVOURITES).map { it to false }
        if (!suggest) return mine
        val keys = dock.map { it.key }.toSet()
        return mine + frequent.filter { it.key !in keys }.take(FAVOURITES - mine.size).map { it to true }
    }

    /** «Ordenar según su uso»: A to Z by letter, and inside each letter the most opened first. */
    fun byUsage(apps: List<AppEntry>, counts: Map<String, Int>): List<AppEntry> =
        Apps.groups(apps).flatMap { (_, group) -> group.sortedWith(compareBy({ -(counts[it.key] ?: 0) }, { Apps.fold(it.label) })) }

    /** The letters on the alphabet bar, in order, from the apps there are. */
    fun letters(apps: List<AppEntry>): List<Char> = Apps.groups(apps).map { it.first }

    /**
     * How far a letter of the alphabet bar moves left while a finger is near it: a smooth wave that is [max] at the
     * letter under the finger and fades over about three letters. [touch] is null when no finger is there.
     */
    fun wave(index: Int, touch: Float?, max: Float): Float {
        if (touch == null) return 0f
        val d = abs(index - touch)
        return (max * exp(-(d * d) / 4.5)).toFloat()
    }

    /** The letter under the finger at [y] on a bar of [height] with [count] letters. */
    fun letterAt(y: Float, height: Float, count: Int): Int =
        if (count <= 0 || height <= 0f) 0 else (y / height * count).toInt().coerceIn(0, count - 1)
}
