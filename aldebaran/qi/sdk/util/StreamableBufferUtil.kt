@file:JvmName("StreamableBufferUtil")

package com.aldebaran.qi.sdk.util

import com.aldebaran.qi.Consumer
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.`object`.streamablebuffer.StreamableBuffer
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.math.min

private const val DEFAULT_CHUNK_SIZE = 1024L * 1024L * 4L

/**
 * Read a [StreamableBuffer] entirely, chunk by chunk.
 * @receiver the [StreamableBuffer] to be read.
 * @param [chunkSize] the chunk size.
 * @return A [ByteBuffer] containing the entire [StreamableBuffer] data.
 * @since 7
 */
@JvmOverloads
fun StreamableBuffer.readAll(chunkSize: Long = DEFAULT_CHUNK_SIZE): ByteBuffer {
    return FutureUtils.get(async().readAll(chunkSize))
}

/**
 * Read a [StreamableBuffer] entirely, chunk by chunk, asynchronously.
 * @receiver the [StreamableBuffer.Async] to be read.
 * @param [chunkSize] the chunk size.
 * @return A [Future] that is completed when the [StreamableBuffer] is read entirely.
 * @since 7
 */
@JvmOverloads
fun StreamableBuffer.Async.readAll(chunkSize: Long = DEFAULT_CHUNK_SIZE): Future<ByteBuffer> {
    var byteArray = byteArrayOf()

    return size.andThenCompose { totalSize: Long ->
        readAllFrom(0L, chunkSize, totalSize) { byteBuffer: ByteBuffer ->
            byteArray += byteBuffer.array()
        }
    }.andThenApply {
        return@andThenApply ByteBuffer.wrap(byteArray)
    }
}

/**
 * Read a [StreamableBuffer] entirely, chunk by chunk.
 * @receiver the [StreamableBuffer] to be read.
 * @param [chunkSize] the chunk size.
 * @param [onReadChunkFunction] the function called for each chunk read, providing the chunk data.
 * @since 7
 */
@JvmOverloads
fun StreamableBuffer.readAll(chunkSize: Long = DEFAULT_CHUNK_SIZE, onReadChunkFunction: (chunkBuffer: ByteBuffer) -> Unit) {
    FutureUtils.get(async().readAll(chunkSize, onReadChunkFunction))
}

/**
 * Read a [StreamableBuffer] entirely, chunk by chunk.
 * @receiver the [StreamableBuffer] to be read.
 * @param [chunkSize] the chunk size.
 * @param [onReadChunkConsumer] the [Consumer] called for each chunk read, providing the chunk data.
 * @since 7
 */
@JvmOverloads
fun StreamableBuffer.readAll(chunkSize: Long = DEFAULT_CHUNK_SIZE, onReadChunkConsumer: Consumer<ByteBuffer>) {
    FutureUtils.get(async().readAll(chunkSize, onReadChunkConsumer))
}

/**
 * Read a [StreamableBuffer] entirely, chunk by chunk, asynchronously.
 * @receiver the [StreamableBuffer.Async] to be read.
 * @param [chunkSize] the chunk size.
 * @param [onReadChunkFunction] the function called for each chunk read, providing the chunk data.
 * @return A [Future] that is completed when the [StreamableBuffer] is read entirely.
 * @since 7
 */
@JvmOverloads
fun StreamableBuffer.Async.readAll(chunkSize: Long = DEFAULT_CHUNK_SIZE, onReadChunkFunction: (chunkBuffer: ByteBuffer) -> Unit): Future<Void> {
    return size.andThenCompose { totalSize: Long -> readAllFrom(0L, chunkSize, totalSize, onReadChunkFunction) }
}

/**
 * Read a [StreamableBuffer] entirely, chunk by chunk, asynchronously.
 * @receiver the [StreamableBuffer.Async] to be read.
 * @param [chunkSize] the chunk size.
 * @param [onReadChunkConsumer] the [Consumer] called for each chunk read, providing the chunk data.
 * @return A [Future] that is completed when the [StreamableBuffer] is read entirely.
 * @since 7
 */
@JvmOverloads
fun StreamableBuffer.Async.readAll(chunkSize: Long = DEFAULT_CHUNK_SIZE, onReadChunkConsumer: Consumer<ByteBuffer>): Future<Void> {
    return readAll(chunkSize) { onReadChunkConsumer.consume(it) }
}

/**
 * Copy a [StreamableBuffer] to an [OutputStream], chunk by chunk.
 * @receiver the [StreamableBuffer] to be copied.
 * @param [outputStream] the destination [OutputStream].
 * @param [chunkSize] the chunk size.
 * @since 7
 */
@JvmOverloads
fun StreamableBuffer.copyToStream(outputStream: OutputStream, chunkSize: Long = DEFAULT_CHUNK_SIZE) {
    FutureUtils.get(async().copyToStream(outputStream, chunkSize))
}

/**
 * Copy a [StreamableBuffer] to an [OutputStream], chunk by chunk, asynchronously.
 * @receiver the [StreamableBuffer.Async] to be copied.
 * @param [outputStream] the destination [OutputStream].
 * @param [chunkSize] the chunk size.
 * @return A [Future] that is completed when the [StreamableBuffer] is copied entirely.
 * @since 7
 */
@JvmOverloads
fun StreamableBuffer.Async.copyToStream(outputStream: OutputStream, chunkSize: Long = DEFAULT_CHUNK_SIZE): Future<Void> {
    return readAll(chunkSize) { byteBuffer: ByteBuffer -> outputStream.write(byteBuffer.array()) }
}

private fun StreamableBuffer.Async.readAllFrom(
        offset: Long,
        chunkSize: Long,
        totalSize: Long,
        onReadChunkFunction: (ByteBuffer) -> Unit
): Future<Void> {
    val nofBytesToRead = min(chunkSize, totalSize - offset)

    return read(offset, nofBytesToRead).andThenCompose { byteBuffer: ByteBuffer ->
        onReadChunkFunction(byteBuffer)
        val nextOffset = offset + byteBuffer.array().size
        if (nextOffset < totalSize) {
            return@andThenCompose readAllFrom(nextOffset, chunkSize, totalSize, onReadChunkFunction)
        } else {
            return@andThenCompose Future.of<Void>(null)
        }
    }
}
