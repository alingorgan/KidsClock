package com.kidsclock.core.model.routine

import com.kidsclock.core.model.sound.SoundSettings

/** Limits from decision 35 / prompt 05. */
const val MAX_ACTIVITIES = 8
const val MIN_MINUTES = 1
const val MAX_MINUTES = 60
const val MAX_NAME_LENGTH = 30
const val MAX_DOING_LENGTH = 40
const val DEFAULT_NEW_ACTIVITY_MINUTES = 5

/** Why a routine cannot be saved (decision 34). */
sealed interface RoutineIssue {
    data object BlankName : RoutineIssue

    data object NoActivities : RoutineIssue

    data object TooManyActivities : RoutineIssue

    data class BlankActivityName(
        val index: Int,
    ) : RoutineIssue

    data class MinutesOutOfRange(
        val index: Int,
    ) : RoutineIssue
}

fun RoutineSpec.validate(): List<RoutineIssue> =
    buildList {
        if (name.isBlank()) add(RoutineIssue.BlankName)
        if (activities.isEmpty()) add(RoutineIssue.NoActivities)
        if (activities.size > MAX_ACTIVITIES) add(RoutineIssue.TooManyActivities)
        activities.forEachIndexed { i, a ->
            if (a.name.isBlank()) add(RoutineIssue.BlankActivityName(i))
            if (a.minutes !in MIN_MINUTES..MAX_MINUTES) add(RoutineIssue.MinutesOutOfRange(i))
        }
    }

val RoutineSpec.isValid: Boolean get() = validate().isEmpty()

val RoutineSpec.canAddActivity: Boolean get() = activities.size < MAX_ACTIVITIES

/** An empty draft for "+ New routine": no name, no activities (Save stays refused until both exist). */
fun newRoutineDraft(id: String): RoutineSpec = RoutineSpec(id = id, name = "", activities = emptyList())

fun RoutineSpec.rename(name: String): RoutineSpec = copy(name = name.take(MAX_NAME_LENGTH))

/** Appends a blank activity (no name yet), cycling the palette. A no-op at [MAX_ACTIVITIES]. */
fun RoutineSpec.addActivity(): RoutineSpec {
    if (!canAddActivity) return this
    val colour = ActivityColor.palette[activities.size % ActivityColor.palette.size]
    return copy(
        activities =
            activities +
                ActivitySpec(
                    name = "",
                    pictogram = Pictogram.Star,
                    color = colour,
                    minutes = DEFAULT_NEW_ACTIVITY_MINUTES,
                ),
    )
}

/** A no-op for an index that is not in the list. */
fun RoutineSpec.removeActivity(index: Int): RoutineSpec =
    if (index !in activities.indices) this else copy(activities = activities.filterIndexed { i, _ -> i != index })

/** Moves one place earlier ([delta] -1) or later (+1). A no-op at either end or out of range. */
fun RoutineSpec.moveActivity(
    index: Int,
    delta: Int,
): RoutineSpec {
    val target = index + delta
    if (index !in activities.indices || target !in activities.indices) return this
    val list = activities.toMutableList()
    list[index] = activities[target]
    list[target] = activities[index]
    return copy(activities = list)
}

private fun RoutineSpec.updateActivity(
    index: Int,
    change: (ActivitySpec) -> ActivitySpec,
): RoutineSpec =
    if (index !in activities.indices) {
        this
    } else {
        copy(activities = activities.mapIndexed { i, a -> if (i == index) change(a) else a })
    }

fun RoutineSpec.renameActivity(
    index: Int,
    name: String,
) = updateActivity(index) { it.copy(name = name.take(MAX_NAME_LENGTH)) }

/** Clamped to [MIN_MINUTES]..[MAX_MINUTES]. */
fun RoutineSpec.setMinutes(
    index: Int,
    minutes: Int,
) = updateActivity(index) { it.copy(minutes = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)) }

fun RoutineSpec.setStartPolicy(
    index: Int,
    policy: StartPolicy,
) = updateActivity(index) { it.copy(startPolicy = policy) }

fun RoutineSpec.setColor(
    index: Int,
    color: ActivityColor,
) = updateActivity(index) { it.copy(color = color) }

fun RoutineSpec.setPictogram(
    index: Int,
    pictogram: Pictogram,
) = updateActivity(index) { it.copy(pictogram = pictogram) }

fun RoutineSpec.setDoing(
    index: Int,
    doing: String,
) = updateActivity(index) { it.copy(doing = doing.take(MAX_DOING_LENGTH)) }

fun RoutineSpec.setFinish(finish: Finish) = copy(finish = finish)

fun RoutineSpec.setSound(sound: SoundSettings) = copy(sound = sound)

/** The routine as it is stored: names trimmed. Callers check [isValid] first. */
fun RoutineSpec.tidied(): RoutineSpec =
    copy(
        name = name.trim(),
        activities = activities.map { it.copy(name = it.name.trim(), doing = it.doing.trim()) },
    )

/** Library operations: the list of saved routines, in the order they were made. */
fun List<RoutineSpec>.upserted(spec: RoutineSpec): List<RoutineSpec> =
    if (any { it.id == spec.id }) map { if (it.id == spec.id) spec else it } else this + spec

fun List<RoutineSpec>.removed(id: String): List<RoutineSpec> = filterNot { it.id == id }

/** A copy under [newId], named "<name> copy" (cut to the name limit). Null if [id] is not in the list. */
fun List<RoutineSpec>.duplicated(
    id: String,
    newId: String,
): List<RoutineSpec>? {
    val original = firstOrNull { it.id == id } ?: return null
    return this + original.copy(id = newId, name = "${original.name} copy".take(MAX_NAME_LENGTH))
}
