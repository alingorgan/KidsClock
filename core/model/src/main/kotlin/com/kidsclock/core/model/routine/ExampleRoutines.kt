package com.kidsclock.core.model.routine

/** SPEC §12: the two routines a first launch starts with (decision 36). Ids are fixed so they are stable. */
fun exampleRoutines(): List<RoutineSpec> = listOf(eveningExample(), morningExample())

private fun eveningExample() =
    RoutineSpec(
        id = "example-evening",
        name = "Evening",
        finish = Finish.SleepTime,
        activities =
            listOf(
                ActivitySpec("Playtime", Pictogram.Play, ActivityColor.Amber, 8, StartPolicy.ChildTaps, "playing"),
                ActivitySpec("Tidy up", Pictogram.Tidy, ActivityColor.Green, 3, StartPolicy.ChildTaps, "tidying up"),
                ActivitySpec(
                    "Bath time",
                    Pictogram.Bath,
                    ActivityColor.Blue,
                    10,
                    StartPolicy.GrownUpUnlocksThenChildTaps,
                    "having a bath",
                ),
                ActivitySpec(
                    "Brush teeth",
                    Pictogram.Teeth,
                    ActivityColor.Teal,
                    3,
                    StartPolicy.ChildTaps,
                    "brushing teeth",
                ),
                ActivitySpec(
                    "Story time",
                    Pictogram.Story,
                    ActivityColor.Purple,
                    8,
                    StartPolicy.GrownUpOnly,
                    "reading a story",
                ),
            ),
    )

private fun morningExample() =
    RoutineSpec(
        id = "example-morning",
        name = "Morning",
        finish = Finish.AllDone,
        activities =
            listOf(
                ActivitySpec(
                    "Cuddle in bed",
                    Pictogram.Cuddle,
                    ActivityColor.Purple,
                    5,
                    StartPolicy.ChildTaps,
                    "having a cuddle",
                ),
                ActivitySpec(
                    "Go to the toilet",
                    Pictogram.Toilet,
                    ActivityColor.Blue,
                    3,
                    StartPolicy.ChildTaps,
                    "going to the toilet",
                ),
                ActivitySpec(
                    "Brush teeth",
                    Pictogram.Teeth,
                    ActivityColor.Teal,
                    3,
                    StartPolicy.ChildTaps,
                    "brushing teeth",
                ),
                ActivitySpec(
                    "Get dressed",
                    Pictogram.Dress,
                    ActivityColor.Amber,
                    8,
                    StartPolicy.ChildTaps,
                    "getting dressed",
                ),
                ActivitySpec(
                    "Brush hair",
                    Pictogram.Hair,
                    ActivityColor.Indigo,
                    3,
                    StartPolicy.ChildTaps,
                    "brushing hair",
                ),
                ActivitySpec(
                    "Get ready to leave",
                    Pictogram.Leave,
                    ActivityColor.Green,
                    8,
                    StartPolicy.GrownUpOnly,
                    "getting ready to leave the house",
                ),
            ),
    )
