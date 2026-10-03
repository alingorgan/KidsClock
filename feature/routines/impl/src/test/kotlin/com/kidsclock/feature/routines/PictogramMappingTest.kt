package com.kidsclock.feature.routines

import com.kidsclock.core.designsystem.components.KcPicture
import com.kidsclock.core.model.routine.Pictogram
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PictogramMappingTest {
    @Test
    fun spec12_everyPictogramHasAPictureOfTheSameName() {
        Pictogram.entries.forEach { assertEquals(it.name, KcPicture.valueOf(it.name).name) }
        assertEquals(KcPicture.entries.size, Pictogram.entries.size)
    }
}
