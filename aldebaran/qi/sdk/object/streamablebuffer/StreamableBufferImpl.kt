package com.aldebaran.qi.sdk.`object`.streamablebuffer

import com.aldebaran.qi.Property
import com.aldebaran.qi.QiService
import java.nio.ByteBuffer
import kotlin.math.min

internal class StreamableBufferImpl(
        private val totalSize: Long,
        private val readFunction: (offset: Long, size: Long) -> ByteBuffer
) : QiService() {

    val size = Property(Long::class.java)

    init {
        size.setValue(totalSize)
    }

    fun read(offset: Long, nofBytes: Long): ByteBuffer {
        val toRead = min(nofBytes, totalSize - offset)
        return readFunction(offset, toRead)
    }
}
