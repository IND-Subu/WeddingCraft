package com.subu.Helper;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.subu.Login.DatabaseClient;
import com.subu.Login.User;
import com.subu.Login.UserDatabase;

import java.util.Map;
import java.util.concurrent.Executors;

public class FirebaseMsgService extends FirebaseMessagingService {
    private static final String TAG = "FCM_Service";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔁 Refreshed token: " + token);

        // Update token in FireStore
        Executors.newSingleThreadExecutor().execute(() -> {
            UserDatabase db = DatabaseClient.getInstance(getApplicationContext()).getUserDatabase();
            User user = db.userDao().getLoggedInUser();

            if (user != null) {
                FirebaseFirestore.getInstance().collection("users")
                        .document(user.mobile)
                        .update("fcmToken", token)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Token updated " + token))
                        .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to update token", e));
            }
        });
    }
}
