package com.crown.onspotbusiness.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.crown.onspotbusiness.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class LocationUtil {
    private static final String TAG = LocationUtil.class.getName();

    public static boolean isGPSEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception error) {
            error.printStackTrace();
        }

        return gps_enabled && network_enabled;
    }

    @SuppressLint("MissingPermission")
    public static Location getCurrentLocation(Activity activity) {
        return ((LocationManager) activity.getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    /*@SuppressLint("MissingPermission")
    public static Location getCurrentLocation(Activity activity) {
        final Location[] lastLocation = {null};
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
            lastLocation[0] = location;
            Log.v(TAG, "Location: " + location);
        });

        return lastLocation[0];
    }*/

    public static Location requireLocation(Activity activity) {
        final Location[] location = {null};
        if (!LocationUtil.isGPSEnabled(activity)) {
            String message = "Your location information is require to use OnSpot Business";
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.app_name))
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Enable", (paramDialogInterface, paramInt) -> activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Dexter.withActivity(activity).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Location location1 = getCurrentLocation(activity);
                            if (location1 == null) {
                                showLocationErrorDialog(activity);
                                location[0] = null;
                            } else {
                                location[0] = location1;
                            }
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog(activity);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
            } else {
                Location location1 = getCurrentLocation(activity);
                if (location1 == null) {
                    showLocationErrorDialog(activity);
                } else {
                    location[0] = location1;
                }
            }
        }
        return location[0];
    }

    public static void showLocationErrorDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Location Error")
                .setMessage("Unable to get your current location. \n\nTIP: Run Google Map first and try again.")
                .setPositiveButton("Open Map", ((dialog, which) -> {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/"));
                    activity.startActivity(intent);
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void showSettingsDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Grant Permissions")
                .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
                .setPositiveButton("Goto Settings", (dialog, which) -> {
                    dialog.cancel();
                    openSettings(activity);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    public static void openSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, 101);
    }
}
