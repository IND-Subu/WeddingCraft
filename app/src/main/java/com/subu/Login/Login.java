package com.subu.Login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.subu.weddingcraft.R;
import com.subu.weddingcraft.Venues;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * LoginActivity handles user authentication using Firebase Phone Authentication.
 * It allows users to sign in or register using their phone number.
 */
public class Login extends AppCompatActivity {

    private UserDatabase db;
    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private TextInputEditText nameInput, emailInput, mobileInput, otpInput;
    private Button sendOtpBtn, verifyOtpBtn;
    private TextView resendOtp;
    // Callbacks for phone number verification
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices, Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    signInWithCredential(phoneAuthCredential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // This callback is invoked when there is an invalid request for verification,
                    // for example, if the phone number format is not valid.
                    Toast.makeText(Login.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Login", "Verification Failed", e);
                }

                @Override
                public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    verificationId = verifyId;
                    resendToken = token;
                    // Show UI to enter the OTP
                    sendOtpBtn.setVisibility(View.GONE);
                    otpInput.setVisibility(View.VISIBLE);
                    verifyOtpBtn.setVisibility(View.VISIBLE);
                    resendOtp.setVisibility(View.VISIBLE);
                    Toast.makeText(Login.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Room database and Firebase Auth
        db = DatabaseClient.getInstance(getApplicationContext()).getUserDatabase();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        mobileInput = findViewById(R.id.mobile_input);
        otpInput = findViewById(R.id.otp_input);
        sendOtpBtn = findViewById(R.id.send_otp_button);
        verifyOtpBtn = findViewById(R.id.verify_otp_button);
        resendOtp = findViewById(R.id.resend_text);

        // Auto login if a user is already logged in
        Executors.newSingleThreadExecutor().execute(() -> {
            User loggedInUser = db.userDao().getLoggedInUser();
            if (loggedInUser != null && loggedInUser.isLoggedIn) {
                String mobile = normalizeMobile(loggedInUser.mobile);
                String name = loggedInUser.name;

                // Upload FCM token to Firestore
                uploadTokenToFireStore(mobile, name);

                runOnUiThread(() -> {
                    // Navigate to the main activity
                    startActivity(new Intent(Login.this, Venues.class));
                    finish();
                });
            }
        });

        // Skip login and proceed to the main activity
        findViewById(R.id.skip_text).setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Venues.class));
            finish();
        });

        // Send OTP to the user's phone number
        sendOtpBtn.setOnClickListener(v -> {
            String mobile = Objects.requireNonNull(mobileInput.getText()).toString().trim();
            if (mobile.length() != 10) {
                mobileInput.setError("Enter valid 10-digit number");
                return;
            }
            sendOtp("+91" + mobile);
        });

        // Verify the entered OTP
        verifyOtpBtn.setOnClickListener(v -> {
            String code = Objects.requireNonNull(otpInput.getText()).toString().trim();
            if (code.isEmpty()) {
                otpInput.setError("Enter OTP");
                return;
            }
            verifyOtp(code);
        });

        // Resend the OTP
        resendOtp.setOnClickListener(v -> {
            String mobile = Objects.requireNonNull(mobileInput.getText()).toString().trim();
            if (resendToken != null) {
                resendOtp("+91" + mobile, resendToken);
            } else {
                Toast.makeText(this, "Please wait or try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends a verification code to the given phone number.
     * @param mobileNumber The phone number to send the code to.
     */
    private void sendOtp(String mobileNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(mobileNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Resends the verification code to the given phone number.
     * @param mobileNumber The phone number to resend the code to.
     * @param token The token received from the initial OTP request.
     */
    private void resendOtp(String mobileNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(mobileNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .setForceResendingToken(token) // Force resending the OTP
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Verifies the OTP code entered by the user.
     * @param code The OTP code to verify.
     */
    private void verifyOtp(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    /**
     * Signs in the user with the given credential.
     * @param credential The PhoneAuthCredential to sign in with.
     */
    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        assert firebaseUser != null;
                        saveUserLocally(firebaseUser);
                        Toast.makeText(Login.this, "Verified", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves user data to local database and Firebase.
     * @param firebaseUser The authenticated Firebase user.
     */
    private void saveUserLocally(FirebaseUser firebaseUser) {
        String name = Objects.requireNonNull(nameInput.getText()).toString().trim();
        String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
        String mobile = firebaseUser.getPhoneNumber();

        Executors.newSingleThreadExecutor().execute(() -> {
            // Log out any previously logged-in user
            List<User> users = db.userDao().getAllUsers();
            for (User u : users) {
                u.isLoggedIn = false;
                db.userDao().update(u);
            }

            // Create a new user and save to local database
            User user = new User();
            user.name = name;
            user.email = email;
            user.mobile = mobile;
            user.isLoggedIn = true;

            db.userDao().insert(user);

            // Save user data to Firebase Realtime Database
            assert mobile != null;
            String normalizedMobile = normalizeMobile(mobile);

            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(normalizedMobile).setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Login", "User data pushed to Firebase");
                        // Upload FCM token to Firestore
                        uploadTokenToFireStore(normalizedMobile, name);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            // Navigate to the main activity
                            startActivity(new Intent(Login.this, Venues.class));
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Login", "Firebase push failed", e);
                        runOnUiThread(() ->
                                Toast.makeText(Login.this, "Login failed, try again", Toast.LENGTH_SHORT).show()
                        );
                    });
        });
    }

    /**
     * Uploads the user's FCM token to Firestore.
     * @param mobile The user's mobile number.
     * @param name The user's name.
     */
    private void uploadTokenToFireStore(String mobile, String name) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    UserToken userToken = new UserToken(name, token);
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(mobile)
                            .set(userToken.toMap())
                            .addOnSuccessListener(unused -> Log.d("FireStore", "Token saved"))
                            .addOnFailureListener(e -> Log.e("FireStore", "Failed to store token", e));
                });
    }

    /**
     * Normalizes the mobile number by removing non-digit characters and taking the last 10 digits.
     * @param mobile The mobile number to normalize.
     * @return The normalized mobile number.
     */
    private String normalizeMobile(String mobile) {
        mobile = mobile.replaceAll("\\D", "");
        return mobile.length() > 10 ? mobile.substring(mobile.length() - 10) : mobile;
    }
}
// 996CDC30-C516-4357-8325-17B33EA0ED69 - DebugToken -- do not delete this
