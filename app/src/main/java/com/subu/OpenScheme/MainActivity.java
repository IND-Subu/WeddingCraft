package com.subu.OpenScheme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.subu.weddingcraft.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_activity_main);

        Intent intent = new Intent(MainActivity.this, ChooseTheme.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        findViewById(R.id.marriageCard).setOnClickListener(v -> {
            intent.putExtra("type", "Marriage");
            startActivity(intent);
        });
        findViewById(R.id.anniversaryCard).setOnClickListener(v -> {
            intent.putExtra("type", "Anniversary");
            //startActivity(intent);
            Toast.makeText(this, "Card will available soon.", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.birthdayCard).setOnClickListener(v -> {
            intent.putExtra("type", "Birthday");
            //startActivity(intent);
            Toast.makeText(this, "Card will available soon.", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.eventCard).setOnClickListener(v -> {
            intent.putExtra("type", "Event");
            //startActivity(intent);
            Toast.makeText(this, "Card will available soon.", Toast.LENGTH_SHORT).show();
        });
    }
}