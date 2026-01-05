package com.subu.Helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.subu.weddingcraft.R;
import com.subu.weddingcraft.WedCard;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CardViewerActivity extends AppCompatActivity {
    ViewPager2 viewPager;
    HashMap<String, Bitmap> imageMap = new HashMap<>();
    WedCard wedCardData;
    private static final String TAG = "CardViewerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_viewer);

        Log.d(TAG, "CardViewerActivity created");
        Log.d(TAG, "Received wedCardPath: " + getIntent().getStringExtra("wedCardPath"));

        viewPager = findViewById(R.id.fold_view_pager);
        viewPager.setPageTransformer(new FoldPageTransformer());

        String wedCardPath = getIntent().getStringExtra("wedCardPath");
        if (wedCardPath != null) {
            loadAndDisplayWedCard(wedCardPath);
        }
    }

    private void loadAndDisplayWedCard(String wedCardPath) {
        new Thread(() -> {
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(wedCardPath)))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equalsIgnoreCase("card.json")){
                        wedCardData = new Gson().fromJson(new InputStreamReader(zis), WedCard.class);
                    } else if (entry.getName().startsWith("images/") && (entry.getName().endsWith(".jpg") || entry.getName().endsWith(".png"))) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            baos.write(buffer, 0, count);
                        }
                        Bitmap bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
                        imageMap.put(entry.getName(), bitmap);
                    }
                }
                runOnUiThread(() -> {
                    if (wedCardData != null && wedCardData.folds != null) {
                        Toolbar cardViewerToolBar = findViewById(R.id.card_viewer_toolBar);
                        cardViewerToolBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
                        viewPager.setAdapter(new FoldPagerAdapter(this, wedCardData.folds, imageMap));
                    } else {
                        Log.e(TAG, "No images found in the wed card");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading and displaying wed card", e);
            }
        }).start();
    }
}