package com.kidsclock.feature.run

import com.kidsclock.core.designsystem.components.KcPicture
import com.kidsclock.core.model.routine.Pictogram
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PictogramMappingTest {
    @Test
    fun spec12_everyPictogramHasAPictureOfTheSameName() {
        Pictogram.entries.forEach { assertEquals(it.name, it.toPicture().name) }
    }

    @Test
    fun spec12_everyPictureIsUsedByAPictogram() {
        assertEquals(KcPicture.entries.map { it.name }.toSet(), Pictogram.entries.map { it.toPicture().name }.toSet())
    }
}
