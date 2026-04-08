package com.example.endekise.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupManager {

    private static final String DATABASE_NAME = "endekise_database";

    // 1. BACKUP FUNCTION
    public static void exportDatabase(Context context) {
        try {
            // Get the current live database file
            File currentDB = context.getDatabasePath(DATABASE_NAME);

            // Go to the phone's public "Documents" or "Downloads" folder
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "EndeKise_Backups");

            if (!exportDir.exists()) {
                exportDir.mkdirs(); // Create the folder if it doesn't exist
            }

            // Name the backup file with today's date
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File backupDB = new File(exportDir, "backup_" + timeStamp + ".db");

            // Copy the file
            if (currentDB.exists()) {
                copyFile(new FileInputStream(currentDB), new FileOutputStream(backupDB));
                Toast.makeText(context, "Backup Successful!\nSaved in Documents/EndeKise_Backups", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "No database found to backup.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Backup Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 2. RESTORE FUNCTION
    public static void importDatabase(Context context, File backupFile) {
        try {
            File currentDB = context.getDatabasePath(DATABASE_NAME);

            if (backupFile.exists()) {
                // Warning: This overwrites current data!
                // In a real app, you would ask the user "Are you sure?" first.
                copyFile(new FileInputStream(backupFile), new FileOutputStream(currentDB));
                Toast.makeText(context, "Restore Successful! Please restart the app.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Restore Failed.", Toast.LENGTH_SHORT).show();
        }
    }

    // Standard Java logic to copy bytes from one file to another
    private static void copyFile(InputStream src, OutputStream dst) throws Exception {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = src.read(buffer)) > 0) {
            dst.write(buffer, 0, length);
        }
        src.close();
        dst.close();
    }
}