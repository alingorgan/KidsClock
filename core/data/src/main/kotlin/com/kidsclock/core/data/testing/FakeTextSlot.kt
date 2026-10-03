package com.kidsclock.core.data.testing

import com.kidsclock.core.data.TextSlot

/** In-memory [TextSlot] for tests. */
class FakeTextSlot(
    var text: String? = null,
) : TextSlot {
    override fun read(): String? = text

    override fun write(text: String) {
        this.text = text
    }
}
