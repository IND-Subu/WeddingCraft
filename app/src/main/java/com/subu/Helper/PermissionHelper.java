package com.subu.Helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public final class PermissionHelper {
    private static final String TAG = "PermissionHelper";
    private final Context mContext;
    public static final int PERMISSION_REQUEST_CODE = 101;
    List<String> permissions = new ArrayList<>();

    public PermissionHelper(Context context) {
        this.mContext = context;

        permissions.add(Manifest.permission.WRITE_CONTACTS);
        permissions.add(Manifest.permission.READ_CONTACTS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    public void logPermissions(){
        Log.d(TAG, "Permissions: "+permissions);
    }

    public boolean checkPermissions() {
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void requestAllPermissions() {
        ActivityCompat.requestPermissions((Activity) mContext, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }
}