package com.qStivi.textToSpeech;

import javax.sound.sampled.AudioInputStream;

public interface ITextToSpeech {
    AudioInputStream convertTextToSpeech(String text);
}
