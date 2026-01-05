package com.subu.weddingcraft;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.subu.Helper.PermissionHelper;
import com.subu.Login.DatabaseClient;
import com.subu.Login.Login;
import com.subu.Login.User;
import com.subu.Login.UserDatabase;
import com.subu.Login.UserToken;

import java.io.File;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String PREFS_NAME = "folder_prefs";
    public static final String PARENT_URI_KEY = "parent_folder_uri";

    private PermissionHelper permissionHelper;
    private ActivityResultLauncher<Intent> folderPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        hideSystemBars();
        startCycleAnimation();

        permissionHelper = new PermissionHelper(this);

        // Step 1: Create "WeddingCraft" folder under Documents if it doesn't exist
        checkAndCreateParentDir();


        // Step 2: Register SAF launcher
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri folderUri = result.getData().getData();
                        if (folderUri != null) {
                            getContentResolver().takePersistableUriPermission(folderUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                    .edit().putString(PARENT_URI_KEY, folderUri.toString()).apply();
                            checkAndCreateParentDir();
                            proceedAfterPermission();
                        }
                    }
                }
        );

        // Step 3: If URI already granted
        if (!permissionHelper.checkPermissions()) {
            permissionHelper.requestAllPermissions();
        } else {
            handleFolderAccessAfterPermissions();
        }
    }

    private void handleFolderAccessAfterPermissions() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uriStr = prefs.getString(PARENT_URI_KEY, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (uriStr == null) {
                requestFolderAccess();
            } else {
                proceedAfterPermission();
            }
        } else {
            proceedAfterPermission();
        }
    }

    private void checkAndCreateParentDir() {
        File parentDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WeddingCraft");
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            Log.d(TAG, "WeddingCraft folder created: " + created);
        } else {
            Log.d(TAG, "WeddingCraft folder already exists");
        }

        // Step 2: Create subfolders "received" and "wedcards"
        String[] subFolders = {"received", "wedcards"};
        for (String name : subFolders) {
            File subDir = new File(parentDir, name);
            if (!subDir.exists()) {
                boolean subCreated = subDir.mkdirs();
                Log.d(TAG, name + " folder created: " + subCreated);
            } else {
                Log.d(TAG, name + " folder already exists");
            }
        }
    }

    private void requestFolderAccess() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        folderPickerLauncher.launch(intent);
    }

    private void startCycleAnimation() {
        ImageView embroideredCircle = findViewById(R.id.embroid_circle);
        ObjectAnimator animator = ObjectAnimator.ofFloat(embroideredCircle, "rotation", 0f, 360f);
        animator.setDuration(10000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void proceedAfterPermission() {
        new Handler().postDelayed(() -> Executors.newSingleThreadExecutor().execute(() -> {
            UserDatabase db = DatabaseClient.getInstance(getApplicationContext()).getUserDatabase();
            User user = db.userDao().getLoggedInUser();

            Intent intent;
            if (user != null && user.isLoggedIn) {
                SharedPreferences userPrefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
                userPrefs.edit().putString("userName", user.name).apply();

                String mobile = user.mobile.replaceAll("\\D", "");
                String normalized = mobile.length() > 10 ? mobile.substring(mobile.length() - 10) : mobile;

                FirebaseMessaging.getInstance().getToken()
                        .addOnSuccessListener(token -> {
                            UserToken userToken = new UserToken(user.name, token);
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(normalized)
                                    .set(userToken.toMap())
                                    .addOnSuccessListener(unused -> Log.d(TAG, "Token saved: "+token+", Unused Token: "+unused))
                                    .addOnFailureListener(e -> Log.e(TAG, "Token failed", e));
                        });
                intent = new Intent(this, Venues.class);
            } else {
                intent = new Intent(this, Login.class);
            }

            runOnUiThread(() -> {
                startActivity(intent);
                finish();
            });
        }), 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : results) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                handleFolderAccessAfterPermissions();
            } else {
                boolean permanentlyDenied = false;
                for (int i = 0; i < results.length; i++) {
                    if (results[i] != PackageManager.PERMISSION_GRANTED &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        permanentlyDenied = true;
                        break;
                    }
                }

                if (permanentlyDenied) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Denied")
                            .setMessage("Some permissions were denied permanently. Please enable them in Settings.")
                            .setPositiveButton("Open Settings", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            })
                            .setNegativeButton("Exit", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                } else {
                    permissionHelper.requestAllPermissions();
                }
            }
        }
    }
}
