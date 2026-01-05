package com.subu.weddingcraft;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.subu.Helper.CardViewerActivity;
import com.subu.Helper.WedCardHelper;
import com.subu.Login.DatabaseClient;
import com.subu.Login.Login;
import com.subu.Login.User;
import com.subu.OpenScheme.MainActivity;
import com.subu.OpenScheme.MyCards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Venues extends AppCompatActivity {
    private static final String TAG = "Venues";
    private final List<CardCatalog> cardList = new ArrayList<>();
    RecyclerView recyclerView;
    WedCardHelper wedCardHelper;
    VenuesViewAdapter.OnItemClickListener listener = position -> {
        CardCatalog clickedCard = cardList.get(position);
        File file = clickedCard.getWedCardFile();
        if (file.exists()) {
            Intent intent = new Intent(Venues.this, CardViewerActivity.class);
            intent.putExtra("wedCardPath", file.getAbsolutePath());
            startActivity(intent);
        } else {
            Log.e(TAG, "File not found");
            Toast.makeText(Venues.this, "File not found", Toast.LENGTH_SHORT).show();
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venues);

        VenuesViewAdapter adapter = new VenuesViewAdapter(this, cardList, listener);
        TextView emptyInvitation = findViewById(R.id.emptyInvitation);

        DrawerLayout drawerLayout = findViewById(R.id.venus_drawer_layout);
        NavigationView navigationView = findViewById(R.id.venus_navigation_view);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        Button logout_button = findViewById(R.id.logout_button);
        logout_button.setOnClickListener(v -> Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseClient.getInstance(getApplicationContext()).getUserDatabase().userDao().logoutUser();
            FirebaseAuth.getInstance().signOut();
            runOnUiThread(() -> {
                Intent logEntryIntent = new Intent(Venues.this, Login.class);
                logEntryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logEntryIntent);
                finish();
            });
        }));

        View headerView = navigationView.getHeaderView(0);
        Executors.newSingleThreadExecutor().execute(() -> {

            User user = DatabaseClient.getInstance(getApplicationContext()).getUserDatabase().userDao().getLoggedInUser();
            if (user != null) {
                runOnUiThread(() -> {
                    TextView nameTextView = headerView.findViewById(R.id.userName);
                    TextView mobileTextView = headerView.findViewById(R.id.mobNumber);
                    TextView emailTextView = headerView.findViewById(R.id.emailId);
                    nameTextView.setText(user.name);
                    mobileTextView.setText(user.mobile);
                    emailTextView.setText(user.email);
                });
            }
        });

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.add_new_card) {
                Intent intent = new Intent(Venues.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            if (id == R.id.my_cards) {
                startActivity(new Intent(Venues.this, MyCards.class));
            }
            if (id == R.id.shareApp) {
                shareAppApk();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        recyclerView = findViewById(R.id.invitation_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);
        wedCardHelper = new WedCardHelper(this, cardList, null);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            String treeUriString = getSharedPreferences(com.subu.weddingcraft.MainActivity.PREFS_NAME, MODE_PRIVATE).getString(com.subu.weddingcraft.MainActivity.PARENT_URI_KEY, null);
            if (treeUriString == null) {
                Log.w(TAG, "No parent folder URI found. User may need to select folder again.");
                Toast.makeText(this, "No folder selected. Please restart the app to select a folder.", Toast.LENGTH_LONG).show();
                return;
            }
            Uri treeUri = Uri.parse(treeUriString);
            if (treeUri != null) {
                String baseDocId = DocumentsContract.getTreeDocumentId(treeUri);
                String receivedDocId = baseDocId + "/received";

                Uri receivedFolderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, receivedDocId);
                ContentResolver resolver = getContentResolver();

                wedCardHelper.setWedCardLoadListener(count -> {
                    if (count == 0) {
                        emptyInvitation.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyInvitation.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
                wedCardHelper.loadWedCardsFromMediaStore(receivedFolderUri, resolver);
                adapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Tree URI not found");
            }
        } else {
            File legacyFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WeddingCraft/received");
            wedCardHelper.loadFromLegacyDir(legacyFolder);
        }
    }

    private void shareAppApk() {
       /*
       TODO: Implement PlayStore Sharing
        */
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String appPackageName = getPackageName();
        String shareMessage = "Check out this amazing app!\n\n" +
                "https://play.google.com/store/apps/details?id=" + appPackageName;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.status_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle icons later
        if (item.getItemId() == R.id.add_new_card) {
            Intent intent = new Intent(Venues.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return true;
    }
}