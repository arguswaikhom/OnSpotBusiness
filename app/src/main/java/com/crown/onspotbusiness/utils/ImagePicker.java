package com.crown.onspotbusiness.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

import static androidx.core.content.FileProvider.getUriForFile;

public class ImagePicker {
    public static final int RC_SELECT_SINGLE_IMAGES = 1;
    public static final int RC_SELECT_MULTIPLE_IMAGES = 20;

    private Activity mActivity;
    private int mRequestCode;

    public ImagePicker(Activity activity) {
        this(activity, RC_SELECT_SINGLE_IMAGES);
    }

    public ImagePicker(Activity activity, int requestCode) {
        this.mActivity = activity;
        this.mRequestCode = requestCode;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Select image");
        builder.setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
            switch (which) {
                case 0: {
                    onSelectCamera();
                    break;
                }
                case 1: {
                    onSelectGallery();
                    break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void fromGallery() {
        onSelectGallery();
    }

    private void onSelectCamera() {
        Dexter.withActivity(mActivity).withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    String fileName = System.currentTimeMillis() + ".jpg";
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName));
                    if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
                        mActivity.startActivityForResult(intent, RC_SELECT_MULTIPLE_IMAGES);
                    }
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    private void onSelectGallery() {
        Dexter.withActivity(mActivity).withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (mRequestCode == RC_SELECT_MULTIPLE_IMAGES)
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    mActivity.startActivityForResult(intent, RC_SELECT_MULTIPLE_IMAGES);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    private Uri getCacheImagePath(String fileName) {
        File path = new File(mActivity.getExternalCacheDir(), "camera");
        if (!path.exists()) path.mkdirs();
        File image = new File(path, fileName);
        return getUriForFile(mActivity, mActivity.getPackageName() + ".provider", image);
    }
}
