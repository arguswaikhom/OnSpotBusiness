package com.crown.onspotbusiness.utils.compression;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import id.zelory.compressor.Compressor;

public class ImageCompression {
    private final String TAG = ImageCompression.class.getName();

    private final Context context;
    private final Uri imageUri;

    public ImageCompression(Context context, Uri imageUri) {
        this.context = context;
        this.imageUri = imageUri;
    }

    public File compress() {
        File image;
        try {
            image = FileUtil.from(context, imageUri);
            image = new Compressor(context)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(40)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToFile(image);

            Log.v(TAG, "Image path: " + image.getPath());
            Log.v(TAG, "Original Size: " + String.format("Size : %s", getReadableFileSize(image.length())));
            Log.v(TAG, "Compressed Size: " + String.format("Size : %s", getReadableFileSize(image.length())));

            return image;
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "catch:  " + e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
