package com.qStivi.speech.stt;

import com.qStivi.listeners.TranscriptionListener;

import java.util.concurrent.ExecutionException;

public interface ISpeechToText {
    void startTranscription() throws InterruptedException, ExecutionException;

    void setTranscriptionListener(TranscriptionListener listener);
}
