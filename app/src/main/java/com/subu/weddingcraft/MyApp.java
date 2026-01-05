package com.subu.weddingcraft;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyApp extends Application {
    private static final String TAG = "MyApp";
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("AppCheck", "App Check Starts");

        // Initialize Firebase (if not already initialized elsewhere)
        FirebaseApp.initializeApp(this);
        Log.d("AppCheck", "Firebase Initialized");

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();

        if (BuildConfig.DEBUG) {
            Log.d("AppCheck", "🧪 Debug build: using DebugAppCheckProviderFactory");
            firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
            );
        } else {
            Log.d("AppCheck", "🚀 Release build: using PlayIntegrityAppCheckProviderFactory");
            firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
            );
        }

        // Clear Firestore cache and reset settings
        FirebaseFirestore.getInstance().clearPersistence()
                .addOnCompleteListener(task ->{
                    if (task.isSuccessful()){
                        Log.d(TAG, "✅ Firestore cache cleared");

                        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                .setPersistenceEnabled(true)
                                .build();
                        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
                        Log.d(TAG, "✅ Firestore settings reset");
                    } else {
                        Log.e(TAG, "❌ Failed to clear Firestore cache", task.getException());
                    }
                });
    }
}
