package com.subu.weddingcraft;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class OpenShareableWedCard extends AppCompatActivity {
    private static final String TAG = "OpenShareableWedCard";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
//            handleWedcardFile(data);
            Log.i(TAG, "Wedcard processed "+data);
            startActivity(new Intent(OpenShareableWedCard.this, Venues.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return; // skip main screen
        }
    }
}
