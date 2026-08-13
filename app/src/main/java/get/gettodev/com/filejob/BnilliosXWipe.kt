/*
 * Copyright (c) 2026 GettoDev
 * All Rights Reserved.
 */

package get.gettodev.com.filejob

import java8.nio.channels.SeekableByteChannel
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.StandardOpenOption
import get.gettodev.com.provider.common.ForceableChannel
import get.gettodev.com.provider.common.delete
import get.gettodev.com.provider.common.moveTo
import get.gettodev.com.provider.common.newByteChannel
import get.gettodev.com.provider.linux.isLinuxPath
import get.gettodev.com.provider.linux.media.MediaScanner
import get.gettodev.com.provider.remote.RemoteSeekableByteChannel
import java.io.IOException
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.SecureRandom

/** Flash page–oriented I/O block (typical UFS/eMMC page multiple). */
private const val BNILLIOS_X_BLOCK_SIZE = 16 * 1024

/**
 * BNilliosX pipeline (flash-oriented):
 * rename → SecureRandom pass → zero pass → truncate → delete → MediaStore scrub.
 * Errors during wipe are not swallowed; incomplete writes fail the operation.
 * Paths that cannot [force][java8.nio.channels.FileChannel.force] (weak SAF) fail instead of
 * pretending a secure wipe completed.
 */
@Throws(IOException::class)
internal fun secureWipeBnilliosXFile(originalPath: Path, size: Long) {
    val random = SecureRandom()
    val wipePath = randomRenameForBnilliosX(originalPath, random)
    try {
        if (size > 0L) {
            overwriteEntireFileBnilliosX(wipePath, size) { buffer, _ ->
                random.nextBytes(buffer)
            }
            overwriteEntireFileBnilliosX(wipePath, size) { buffer, length ->
                buffer.fill(0, 0, length)
            }
        }
        truncateAndForceBnilliosX(wipePath)
        wipePath.delete()
    } finally {
        scrubBnilliosXMediaStore(originalPath)
        scrubBnilliosXMediaStore(wipePath)
    }
}

@Throws(IOException::class)
private fun randomRenameForBnilliosX(path: Path, random: SecureRandom): Path {
    val parent = path.parent
        ?: throw IOException("Cannot securely wipe path without a parent: $path")
    var lastError: IOException? = null
    repeat(8) {
        val name = ".bnx_${BigInteger(64, random).toString(16)}.tmp"
        val target = parent.resolve(name)
        try {
            path.moveTo(target, LinkOption.NOFOLLOW_LINKS)
            return target
        } catch (e: IOException) {
            lastError = e
        }
    }
    throw IOException("Failed to rename before BNilliosX wipe: $path", lastError)
}

@Throws(IOException::class)
private fun overwriteEntireFileBnilliosX(
    path: Path,
    size: Long,
    fill: (ByteArray, Int) -> Unit
) {
    val channel = path.newByteChannel(StandardOpenOption.WRITE)
    try {
        requireForceableBnilliosX(channel, path)
        channel.position(0)
        val buffer = ByteArray(BNILLIOS_X_BLOCK_SIZE)
        var offset = 0L
        while (offset < size) {
            val bytesToWrite = minOf(buffer.size.toLong(), size - offset).toInt()
            fill(buffer, bytesToWrite)
            writeFullyBnilliosX(channel, buffer, bytesToWrite)
            offset += bytesToWrite
        }
        if (offset != size) {
            throw IOException("BNilliosX wipe incomplete: wrote $offset of $size bytes for $path")
        }
        val reportedSize = channel.size()
        if (reportedSize < size) {
            throw IOException(
                "BNilliosX wipe channel size mismatch: size=$reportedSize expected>=$size for $path"
            )
        }
        forceBnilliosXChannel(channel)
    } finally {
        channel.close()
    }
}

@Throws(IOException::class)
private fun writeFullyBnilliosX(channel: SeekableByteChannel, data: ByteArray, length: Int) {
    var written = 0
    while (written < length) {
        val n = channel.write(ByteBuffer.wrap(data, written, length - written))
        if (n < 0) {
            throw IOException("Unexpected EOF during BNilliosX wipe write")
        }
        if (n == 0) {
            throw IOException("Zero-length write during BNilliosX wipe")
        }
        written += n
    }
}

private fun requireForceableBnilliosX(channel: SeekableByteChannel, path: Path) {
    val ok = channel is java8.nio.channels.FileChannel ||
        channel is RemoteSeekableByteChannel ||
        channel is ForceableChannel
    if (!ok) {
        throw IOException(
            "BNilliosX requires a forceable channel (unsupported provider/SAF path): $path"
        )
    }
}

@Throws(IOException::class)
private fun forceBnilliosXChannel(channel: SeekableByteChannel) {
    when (channel) {
        is java8.nio.channels.FileChannel -> channel.force(true)
        is RemoteSeekableByteChannel -> channel.force(true)
        is ForceableChannel -> channel.force(true)
        else -> throw IOException(
            "BNilliosX requires a forceable channel (unsupported provider/SAF path)"
        )
    }
}

@Throws(IOException::class)
private fun truncateAndForceBnilliosX(path: Path) {
    val channel = path.newByteChannel(StandardOpenOption.WRITE)
    try {
        requireForceableBnilliosX(channel, path)
        channel.truncate(0)
        forceBnilliosXChannel(channel)
    } finally {
        channel.close()
    }
}

private fun scrubBnilliosXMediaStore(path: Path) {
    try {
        if (!path.isLinuxPath) {
            return
        }
        MediaScanner.scan(path.toFile(), isDeleted = true)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
