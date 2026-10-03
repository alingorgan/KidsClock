package com.kidsclock.core.designsystem.components

/**
 * The pictures [KcPictogram] can draw (SPEC §12). The design system depends on nothing, so features map
 * their own types onto this by name (`KcPicture.valueOf(pictogram.name)`) and test that every one maps.
 */
enum class KcPicture {
    Cuddle,
    Toilet,
    Teeth,
    Dress,
    Hair,
    Leave,
    Play,
    Tidy,
    Bath,
    Story,
    Snack,
    Playground,
    Outside,
    Star,
    Sleep,
    Done,
}
