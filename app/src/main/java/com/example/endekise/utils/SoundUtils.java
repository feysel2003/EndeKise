package com.example.endekise.utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class SoundUtils {

    public static void playNotificationAlarm(Context context) {
        try {
            // Get the default notification sound of the user's phone (usually a pleasant "Ping")
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);

            // Play the sound!
            if (r != null) {
                r.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}