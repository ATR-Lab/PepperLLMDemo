package com.aldebaran.qi.sdk.`object`.streamablebuffer

import com.aldebaran.qi.DynamicObjectBuilder
import com.aldebaran.qi.sdk.QiSDK
import java.nio.ByteBuffer

/**
 * Factory of [StreamableBuffer].
 * @since 7
 */
object StreamableBufferFactory {

    /**
     * Create a [StreamableBuffer] from a function.
     * @param [totalSize] the total size of the [StreamableBuffer].
     * @param [readFunction] the function providing a data chunk of the [StreamableBuffer].
     * @since 7
     */
    @JvmStatic
    fun fromFunction(totalSize: Long, readFunction: (offset: Long, size: Long) -> ByteBuffer): StreamableBuffer {
        val streamableBufferImpl = StreamableBufferImpl(totalSize, readFunction)

        val streamableBufferAny = with(DynamicObjectBuilder()) {
            advertiseProperty("size", streamableBufferImpl.size)
            advertiseMethod("read::r(ii)", streamableBufferImpl, "Reads a chunk of the underlying buffer.")
            `object`()
        }

        streamableBufferImpl.init(streamableBufferAny)

        val deserialized = QiSDK.getSerializer().deserialize(streamableBufferAny, StreamableBuffer::class.java)

        return deserialized as StreamableBuffer
    }
}
