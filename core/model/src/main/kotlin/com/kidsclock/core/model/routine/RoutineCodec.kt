package com.kidsclock.core.model.routine

import com.kidsclock.core.model.sound.ChimeMode
import com.kidsclock.core.model.sound.SoundSettings

/**
 * The stored form of the routine library (decision 36): a small versioned text record, so no
 * serialisation library is needed. Whole minutes are stored, never milliseconds, so the debug fast
 * mode can still scale them.
 *
 * ```
 * kidsclock-routines 1
 * R <TAB> id <TAB> name <TAB> finish <TAB> chime <TAB> nearlyDoneNote(0/1)
 * A <TAB> pictogram <TAB> colour <TAB> minutes <TAB> startPolicy <TAB> name <TAB> doing
 * ```
 * Text fields escape backslash, tab, newline and carriage return.
 */
object RoutineCodec {
    const val VERSION = 1
    private const val HEADER = "kidsclock-routines"

    fun encode(routines: List<RoutineSpec>): String =
        buildString {
            appendLine("$HEADER $VERSION")
            routines.forEach { r ->
                appendLine(
                    listOf(
                        "R",
                        escape(r.id),
                        escape(r.name),
                        r.finish.name,
                        r.sound.chime.name,
                        bit(r.sound.nearlyDoneNote),
                    ).joinToString("\t"),
                )
                r.activities.forEach { a ->
                    appendLine(
                        listOf(
                            "A",
                            a.pictogram.name,
                            a.color.name,
                            a.minutes.toString(),
                            a.startPolicy.name,
                            escape(a.name),
                            escape(a.doing),
                        ).joinToString("\t"),
                    )
                }
            }
        }

    /**
     * The routines in [text], or null when there is nothing usable: a different [VERSION] (older or newer
     * app), a malformed line, or a record whose routines are all invalid. A routine that is invalid on
     * its own (see [validate]) is dropped and the rest kept.
     */
    fun decode(text: String): List<RoutineSpec>? {
        val lines = text.lines().filter { it.isNotBlank() } // stray blank lines carry no data
        if (lines.firstOrNull() != "$HEADER $VERSION") return null
        val drafts = mutableListOf<Draft>()
        for (line in lines.drop(1)) {
            val f = line.split('\t')
            when (f[0]) {
                "R" -> drafts += parseRoutine(f) ?: return null
                "A" -> (drafts.lastOrNull() ?: return null).activities += parseActivity(f) ?: return null
                else -> return null
            }
        }
        val specs = drafts.map { it.toSpec() }
        if (specs.isEmpty()) return emptyList()
        val valid = specs.filter { it.isValid }.distinctBy { it.id }
        return valid.ifEmpty { null }
    }

    private class Draft(
        val id: String,
        val name: String,
        val finish: Finish,
        val sound: SoundSettings,
    ) {
        val activities = mutableListOf<ActivitySpec>()

        fun toSpec() = RoutineSpec(id, name, activities.toList(), finish, sound)
    }

    private fun parseRoutine(f: List<String>): Draft? {
        if (f.size != 6) return null
        val finish = enumOrNull<Finish>(f[3]) ?: return null
        val chime = enumOrNull<ChimeMode>(f[4]) ?: return null
        val note = f[5].toBit() ?: return null
        return Draft(unescape(f[1]) ?: return null, unescape(f[2]) ?: return null, finish, SoundSettings(chime, note))
    }

    private fun parseActivity(f: List<String>): ActivitySpec? {
        if (f.size != 7) return null
        return ActivitySpec(
            pictogram = enumOrNull<Pictogram>(f[1]) ?: return null,
            color = enumOrNull<ActivityColor>(f[2]) ?: return null,
            minutes = f[3].toIntOrNull() ?: return null,
            startPolicy = enumOrNull<StartPolicy>(f[4]) ?: return null,
            name = unescape(f[5]) ?: return null,
            doing = unescape(f[6]) ?: return null,
        )
    }

    private inline fun <reified E : Enum<E>> enumOrNull(name: String): E? =
        enumValues<E>().firstOrNull {
            it.name ==
                name
        }

    private fun bit(b: Boolean) = if (b) "1" else "0"

    private fun String.toBit(): Boolean? =
        when (this) {
            "1" -> true
            "0" -> false
            else -> null
        }

    private fun escape(s: String): String =
        buildString {
            s.forEach {
                when (it) {
                    '\\' -> append("\\\\")
                    '\t' -> append("\\t")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    else -> append(it)
                }
            }
        }

    /** Null on a dangling or unknown escape, which means the record is corrupt. */
    private fun unescape(s: String): String? {
        val out = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c != '\\') {
                out.append(c)
                i++
                continue
            }
            out.append(
                when (s.getOrNull(i + 1)) {
                    '\\' -> '\\'
                    't' -> '\t'
                    'n' -> '\n'
                    'r' -> '\r'
                    else -> return null
                },
            )
            i += 2
        }
        return out.toString()
    }
}
