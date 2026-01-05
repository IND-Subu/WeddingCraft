package com.subu.OpenScheme;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.subu.weddingcraft.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GenerateWedCard extends AppCompatActivity {
    private static final String TAG = "GenerateWedCard";
    EditText etBrideName, etGroomName, etDate, etTime, etVenue, etHost, etRSVP, etQuote;
    EditText etBrideParents, etGroomParents;
    Spinner spinnerBridePosition, spinnerGroomPosition, spinnerMantra;
    Spinner spinnerContactType;
    LinearLayout eventContainer;
    Button addEventBtn, btnGenerate;
    String theme;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_activity_generate_wed_card);

        TextView titleTag = findViewById(R.id.titleTag);
        theme = getIntent().getStringExtra("theme");
        String cardType = getIntent().getStringExtra("type");
        assert cardType != null;
        titleTag.setText(cardType + " " + titleTag.getText());

        // Basic fields
        etBrideName = findViewById(R.id.brideNameInput);
        etGroomName = findViewById(R.id.groomNameInput);
        etDate = findViewById(R.id.dateInput);
        etTime = findViewById(R.id.timeInput);
        etVenue = findViewById(R.id.venueInput);
        etHost = findViewById(R.id.hostInput);
        etRSVP = findViewById(R.id.rsvpInput);
        etQuote = findViewById(R.id.quoteInput);
        spinnerContactType = findViewById(R.id.contactTypeSpinner);

        eventContainer = findViewById(R.id.eventContainer);
        addEventBtn = findViewById(R.id.addEventBtn);
        btnGenerate = findViewById(R.id.btnGenerateCard);

        // New fields
        etBrideParents = findViewById(R.id.brideParentsInput);
        etGroomParents = findViewById(R.id.groomParentsInput);
        spinnerBridePosition = findViewById(R.id.bridePositionSpinner);
        spinnerGroomPosition = findViewById(R.id.groomPositionSpinner);
        spinnerMantra = findViewById(R.id.mantraSpinner);

        addEventBtn.setOnClickListener(v -> addEventRow());

        //Add first event by default
        addEventRow();

        btnGenerate.setOnClickListener(v -> generateCard());
    }

    private void generateCard() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bride", etBrideName.getText().toString().trim());
            jsonObject.put("groom", etGroomName.getText().toString().trim());
            jsonObject.put("date", etDate.getText().toString().trim());
            jsonObject.put("time", etTime.getText().toString().trim());
            jsonObject.put("venue", etVenue.getText().toString().trim());
            jsonObject.put("host", etHost.getText().toString().trim());
            jsonObject.put("rsvp", etRSVP.getText().toString().trim());
            jsonObject.put("contact_type", spinnerContactType.getSelectedItem().toString().trim());
            jsonObject.put("quote", etQuote.getText().toString().trim());
            jsonObject.put("theme", getIntent().getStringExtra("theme"));

            // Add new fields
            jsonObject.put("bride_parents", etBrideParents.getText().toString().trim());
            jsonObject.put("groom_parents", etGroomParents.getText().toString().trim());
            jsonObject.put("bride_position", spinnerBridePosition.getSelectedItem().toString().trim());
            jsonObject.put("groom_position", spinnerGroomPosition.getSelectedItem().toString().trim());
            jsonObject.put("mantra", spinnerMantra.getSelectedItem().toString().trim());

            // Events
            JSONArray eventsArray = new JSONArray();
            for (int i = 0; i < eventContainer.getChildCount(); i++) {
                View eventRow = eventContainer.getChildAt(i);
                EditText etEventName = (EditText) ((LinearLayout) eventRow).getChildAt(0);
                EditText etEventTime = (EditText) ((LinearLayout) eventRow).getChildAt(1);

                JSONObject event = new JSONObject();
                event.put("name", etEventName.getText().toString());
                event.put("time", etEventTime.getText().toString());
                eventsArray.put(event);
            }
            jsonObject.put("events", eventsArray);


            // Save wedcard using MediaStore
            String mimeType = "application/octet-stream";
            String relativeLocation = Environment.DIRECTORY_DOCUMENTS + "/WeddingCraft/wedcards";
            String fileName = etBrideName.getText().toString().trim() + "_" + etGroomName.getText().toString().trim() + ".wedcard";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);

                ContentResolver resolver = getContentResolver();
                Uri collection = MediaStore.Files.getContentUri("external");

                Uri fileUri = resolver.insert(collection, values);
                if (fileUri == null) {
                    Log.e(TAG, "Failed to create new MediaStore record.");
                    Toast.makeText(this, "Failed to create new MediaStore record.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1) Create a temp file in cache directory
                File tempFile = new File(getCacheDir(), fileName);
                try {
                    WedCardGenerator.generateWedCard(this, jsonObject, theme, tempFile.getAbsolutePath());
                } catch (IOException e) {
                    Log.e(TAG, "Temp file generation failed", e);
                    Toast.makeText(this, "Card generation failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3) Copy temp file to MediaStore
                try (OutputStream out = resolver.openOutputStream(fileUri);
                     FileInputStream in = new FileInputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        assert out != null;
                        out.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    Log.e("GenerateWedCard", "Copy to MediaStore failed", e);
                    resolver.delete(fileUri, null, null);  // Clean up
                    Toast.makeText(this, "Card save failed", Toast.LENGTH_SHORT).show();
                    return;
                } finally {
                    tempFile.delete();
                }
                // 4) finalize file
                values.clear();
                values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(fileUri, values, null, null);

                Toast.makeText(this, "Card saved to Documents/WeddingCraft", Toast.LENGTH_SHORT).show();
                Log.d("GenerateWedCard", "Saved to: " + fileUri);
            } else {
                // Android 9 and below
                File baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File outDir = new File(baseDir, "WeddingCraft/wedcards");
                if (!outDir.exists()) outDir.mkdirs();

                File outFile = new File(outDir, fileName);
                try {
                    WedCardGenerator.generateWedCard(this, jsonObject, theme, outFile.getAbsolutePath());
                    Toast.makeText(this, "Card saved to " + outFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Card generated successfully at: " + outFile.getAbsolutePath());
                } catch (Exception e) {
                    Log.e("GenerateWedCard", "Save failed", e);
                    Toast.makeText(this, "Card generation failed", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating card", e);
        }
    }

    private void addEventRow() {
        View eventRow = LayoutInflater.from(this).inflate(R.layout.item_event_row, eventContainer, false);
        eventContainer.addView(eventRow);
    }
}