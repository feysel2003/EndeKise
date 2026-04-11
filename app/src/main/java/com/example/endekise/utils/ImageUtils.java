package com.example.endekise.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageUtils {

    // Safely copies the gallery image to internal storage so the app never loses permission
    public static String saveImageToInternalStorage(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Create a file named "profile_pic.jpg" in the app's private directory
            File file = new File(context.getFilesDir(), "profile_pic.jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return Uri.fromFile(file).toString(); // Return the safe, permanent URI
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to load the image into any ImageView (Home, Budget, Profile, etc.)
    public static void loadProfileImage(Context context, ImageView imageView) {
        String savedUri = context.getSharedPreferences("EndeKisePrefs", Context.MODE_PRIVATE)
                .getString("user_avatar_uri", null);

        if (savedUri != null) {
            try {
                imageView.setImageURI(Uri.parse(savedUri));
                imageView.setImageTintList(null); // Remove the white tint mask!
            } catch (Exception e) {
                e.printStackTrace();
                // If it fails, it just stays as the default purple icon
            }
        }
    }
}