package com.qStivi.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioSendQueue extends ConcurrentLinkedQueue<byte[]> {

    private byte[] remainder;


    @Override
    public boolean offer(@NotNull byte[] bytes) {
        // We split the audio data into 20ms chunks.
        var format = AudioSendHandler.INPUT_FORMAT;

        // Calculate the size of a 20ms chunk
        int chunkSize = format.getFrameSize() * (int) (format.getFrameRate() * 0.02);

        // If there is a remainder from the previous call, prepend it to the bytes array
        if (remainder != null) {
            byte[] newBytes = new byte[remainder.length + bytes.length];
            System.arraycopy(remainder, 0, newBytes, 0, remainder.length);
            System.arraycopy(bytes, 0, newBytes, remainder.length, bytes.length);
            bytes = newBytes;
            remainder = null;
        }

        var offered = false;

        // Split the array into chunks and add them to the queue
        for (int i = 0; i < bytes.length; i += chunkSize) {
            // Calculate the end index for the current chunk
            int end = Math.min(i + chunkSize, bytes.length);

            // If this is the last chunk, and it's smaller than 20ms, store it as the remainder
            if (end - i < chunkSize) {
                remainder = Arrays.copyOfRange(bytes, i, end);
                break;
            }

            // Create a new array for the chunk
            byte[] chunk = Arrays.copyOfRange(bytes, i, end);

            // Add the chunk to the queue
            offered = super.offer(chunk);
        }
        return offered;
    }
}
