package com.kidsclock.core.data

/** One named piece of text kept on the device. The only thing the Android storage has to provide. */
interface TextSlot {
    fun read(): String?

    fun write(text: String)
}
