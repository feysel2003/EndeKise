package com.example.endekise.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;

public class SoundUtils {

    private static MediaPlayer mediaPlayer;

    // --- 1. THE DAILY PING (For Home Screen) ---
    // Plays a guaranteed, crisp "BEEP" sound exactly once!
    public static void playNotificationAlarm(Context context) {
        try {
            // TONE_PROP_BEEP is a short, pleasant high-pitched beep
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100); // 100 = Max volume
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200); // Play for 200 milliseconds
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 2. THE EMERGENCY ALARM (For Budget Screen) ---
    // Plays the default notification sound on an endless loop!
    public static void playLoopingAlarm(Context context) {
        try {
            // Stop any existing alarm first just in case
            stopAlarm();

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alarmSound == null) {
                // Fallback to regular alarm sound if notification sound is missing
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }

            mediaPlayer = MediaPlayer.create(context, alarmSound);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true); // Loop endlessly until stopped!
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 3. STOP ALARM ---
    // Stops the looping alarm when the user clicks "I'll be careful"
    public static void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}