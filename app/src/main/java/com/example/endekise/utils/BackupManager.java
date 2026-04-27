package com.example.endekise.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupManager {

    // The exact name of your Room database file
    private static final String DATABASE_NAME = "endekise_database";

    public static void exportDatabase(Context context) {
        try {
            // 1. Find the internal database file
            File dbFile = context.getDatabasePath(DATABASE_NAME);

            if (!dbFile.exists()) {
                Toast.makeText(context, "Database doesn't exist yet!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Create a timestamped name for the backup file
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String backupFileName = "EndeKise_Backup_" + timeStamp + ".db";

            // 3. Prepare to save it in the public "Downloads" folder
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For modern Android (Android 10+): Use MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, backupFileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    Toast.makeText(context, "Failed to create backup file.", Toast.LENGTH_SHORT).show();
                    return;
                }
                outputStream = context.getContentResolver().openOutputStream(uri);
            } else {
                // For older Android versions: Save directly to the Downloads folder
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File backupFile = new File(downloadsDir, backupFileName);
                outputStream = new java.io.FileOutputStream(backupFile);
            }

            // 4. Copy the data byte-by-byte
            FileInputStream inputStream = new FileInputStream(dbFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // 5. Clean up
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Toast.makeText(context, "✅ Backup saved to Downloads folder!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "❌ Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}