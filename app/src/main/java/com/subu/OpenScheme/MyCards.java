package com.subu.OpenScheme;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.subu.Helper.CardViewerActivity;
import com.subu.Helper.WedCardHelper;
import com.subu.weddingcraft.CardCatalog;
import com.subu.weddingcraft.MainActivity;
import com.subu.weddingcraft.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyCards extends AppCompatActivity {

    private static final String TAG = "MyCards";
    private final List<CardCatalog> cardList = new ArrayList<>();
    WedCardHelper wedCardHelper;
    MyCardsAdapter.OnItemClickListener listener = position -> {
        CardCatalog clickedCard = cardList.get(position);
        File file = clickedCard.getWedCardFile();

        if (file.exists()) {
            Intent intent = new Intent(MyCards.this, CardViewerActivity.class);
            intent.putExtra("wedCardPath", file.getAbsolutePath());
            startActivity(intent);
        } else {
            Log.e("MyCards", "File not found");
            Toast.makeText(this, "No WedCard found", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_activity_my_cards);

        Toolbar myCardsToolBar = findViewById(R.id.my_cards_toolBar);
        myCardsToolBar.setNavigationOnClickListener(v-> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.myCards_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TextView emptyDraft = findViewById(R.id.emptyDraft);

        recyclerView.setAdapter(new MyCardsAdapter(this, cardList, listener));
        wedCardHelper = new WedCardHelper(this, cardList, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            String treeUriString = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE).getString(MainActivity.PARENT_URI_KEY, null);
            if (treeUriString == null) {
                Log.w(TAG, "No parent folder URI found. User may need to select folder again.");
                Toast.makeText(this, "No folder selected. Please restart the app to select a folder.", Toast.LENGTH_LONG).show();
                return;
            }

            Uri treeUri = Uri.parse(treeUriString);
            if (treeUri != null) {
                String baseDocId = DocumentsContract.getTreeDocumentId(treeUri);
                String receivedDocId = baseDocId + "/wedcards";

                Uri wedcardsFolderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, receivedDocId);
                ContentResolver resolver = getContentResolver();

                wedCardHelper.setWedCardLoadListener(count -> runOnUiThread(() -> {
                    if (count == 0) {
                        emptyDraft.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyDraft.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }));

                wedCardHelper.loadWedCardsFromMediaStore(wedcardsFolderUri, resolver);

            } else {
                Log.e(TAG, "Tree URI not found");
                emptyDraft.setVisibility(View.VISIBLE);
            }
        } else {
            File legacyFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WeddingCraft/wedcards");
            wedCardHelper.loadFromLegacyDir(legacyFolder);
        }
    }
}
