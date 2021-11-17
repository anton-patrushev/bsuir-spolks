package com.astronaut.common.utils

import com.astronaut.common.repository.impl.CHUNK_SIZE
import io.ktor.utils.io.core.*
import java.nio.ByteBuffer

abstract class WindowingHandler {
    companion object {
        const val APPROVAL_COUNT = 200
        const val MAX_MISS_COUNT = 10
        const val PACKAGE_SIZE = Long.SIZE_BYTES + CHUNK_SIZE
    }

    private var miss = 0

    private var withoutApprovalCount = 0
    private var sentWithoutApprovalCount = 0

    private var lastReadPackage: Long = -1

    private var packagesSent: Long = 0
    private var packagesReceived: Long = 0

    private var inputBuffer: MutableMap<Long, ByteArray> = mutableMapOf()
    private var outputBuffer: MutableMap<Long, ByteArray> = mutableMapOf()

    abstract suspend fun sendByteArray(byteArray: ByteArray)
    abstract suspend fun receiveByteReadPacket(): ByteReadPacket

    private fun parseByteReadPacket(data: ByteReadPacket): ByteArray {
        val array = ByteArray(PACKAGE_SIZE)

        try {
            data.readFully(array)
        } catch (e: Throwable) {
            // Package is not aligned to chunk size, removing trailing zeros automatically
            var lastNonZeroIndex = 0

            for(i in (PACKAGE_SIZE-1) downTo 0) {
                if(array[i].toInt() != 0) {
                    lastNonZeroIndex = i
                    break
                }
            }

            val newArray = ByteArray(lastNonZeroIndex)

            array.copyInto(newArray, 0, 0, lastNonZeroIndex)

            return newArray
        }

        return array
    }

    protected suspend fun sendApproval() {
        //println("APPROVAL $lastReadPackage")
        pureSend(Events.APPROVE(withoutApprovalCount.toLong()).toString().encodeToByteArray(), lastReadPackage)

        withoutApprovalCount = 0
    }

    private suspend fun sendRetry(id: Long) {
        println("RETRY $id")
        pureSend(Events.RETRY(id).toString().encodeToByteArray(), id)
    }

    private fun flushAllBuffers() {
        lastReadPackage = -1
        withoutApprovalCount = 0
        inputBuffer.clear()
        outputBuffer.clear()
    }

    private suspend fun checkForRetryOrApprove(id: Long, data: ByteArray): Boolean {
        if(!outputBuffer.contains(id)) {
            return false
        }

        try {
            return when(val command = Events.parseFromClientString(String(data))) {
                is Events.APPROVE -> {
                    sentWithoutApprovalCount -= command.payload.toInt()

                    /*for(i in (id - APPROVAL_COUNT + 1)..id) {
                        outputBuffer.remove(i)
                    }*/

                    true
                }
                is Events.RETRY -> {
                    pureSend(outputBuffer[id]!!, id)

                    true
                }
                else -> {
                    print(command)
                    false
                }
            }
        } catch (e: Throwable) {
            println("Cant parse to string for check")
            return false
        }
    }

    private suspend fun pureSend(bytes: ByteArray, id: Long) {
        val packageNumberEncoded = ByteBuffer
            .allocate(Long.SIZE_BYTES)
            .putLong(id)
            .array()

        val newBytes = ByteArray(packageNumberEncoded.size + bytes.size)

        packageNumberEncoded.copyInto(newBytes, 0)
        bytes.copyInto(newBytes, packageNumberEncoded.size)

        sendByteArray(newBytes)
    }

    private suspend fun listenForSentPackagesApprovalOrRetry() {
        while (sentWithoutApprovalCount != 0) {
            receive(true)
        }
    }

    protected suspend fun send(bytes: ByteArray, forceListen: Boolean = false) {
        pureSend(bytes, packagesSent)

        outputBuffer[packagesSent] = bytes
        packagesSent++
        sentWithoutApprovalCount++

        if(sentWithoutApprovalCount == APPROVAL_COUNT || forceListen) {
            listenForSentPackagesApprovalOrRetry()
        }
    }

    private suspend fun retry() {
        miss++

        if(miss == MAX_MISS_COUNT) {
            sendRetry(lastReadPackage + 1)
            miss = 0
        }
    }

    protected suspend fun receive(internalUsage: Boolean = false): ByteArray {
        var wasReceived = false

        if(inputBuffer.contains(lastReadPackage + 1) && !internalUsage) {
            lastReadPackage++
            wasReceived = true
        }

        while(!wasReceived) {
            println("TRYING TO GET ${lastReadPackage + 1} package")
            val packet = receiveByteReadPacket()
            val byteArray = parseByteReadPacket(packet)

            if(byteArray.isEmpty()) {
                retry()
                continue
            }

            val receivedNumber = ((ByteBuffer
                .allocate(Long.SIZE_BYTES)
                .put(byteArray, 0, Long.SIZE_BYTES)
                .flip()) as ByteBuffer)
                .long

            println("RECEIVED BYTE ARRAY SIZE ${byteArray.size} $receivedNumber")

            val data = byteArray.copyOfRange(Long.SIZE_BYTES, byteArray.size)

            val status = checkForRetryOrApprove(receivedNumber, data)

            if(status) {
                if(internalUsage) {
                    return data
                } else {
                    continue
                }
            } else {
                if(inputBuffer.contains(receivedNumber) && !internalUsage) {
                    retry()
                    continue
                }
            }

            println("PACKAGE RECEIVED $receivedNumber")

            packagesReceived++

            if(!internalUsage) {
                if(receivedNumber == lastReadPackage + 1) {
                    lastReadPackage++
                    wasReceived = true
                } else {
                    retry()
                }
            }

            inputBuffer[receivedNumber] = data
        }

        withoutApprovalCount++

        if(withoutApprovalCount == APPROVAL_COUNT) {
            sendApproval()
        }

        return inputBuffer[lastReadPackage]!!
    }
}
