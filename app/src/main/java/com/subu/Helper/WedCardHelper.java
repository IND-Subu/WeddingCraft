package com.subu.Helper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.subu.OpenScheme.ShareWedCardDialogFragment;
import com.subu.weddingcraft.CardCatalog;
import com.subu.weddingcraft.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WedCardHelper {
    private static final String TAG = "WedCardHelper";
    private final Context context;
    private final List<CardCatalog> cardList;
    private final OnWedCardChangedListener listener;
    private WedCardLoadListener wedCardLoadListener;

    public WedCardHelper(Context context, List<CardCatalog> cardList, OnWedCardChangedListener listener) {
        this.context = context;
        this.cardList = cardList;
        this.listener = listener;
    }
    public void setWedCardLoadListener(WedCardLoadListener listener) {
        this.wedCardLoadListener = listener;
    }

    public void loadWedCardsFromMediaStore(Uri folderUri, ContentResolver resolver) {
        if (folderUri == null) {
            Log.e(TAG, "loadWedCardsFromMediaStore called with null folderUri");
            return;
        }

        Log.i(TAG, "Scanning folder URI: " + folderUri);

        String documentId = DocumentsContract.getDocumentId(folderUri);
        Uri childrenuri = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri,documentId);

        cardList.clear(); // prevent duplicates
        int wedCardCount = 0;

        try (Cursor cursor = resolver.query(childrenuri, new String[]{
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
        }, null, null, null)) {

            if (cursor != null) {
                Log.d(TAG, "Found " + cursor.getCount() + " items in received folder.");

                while (cursor.moveToNext()) {
                    String docId = cursor.getString(0);
                    String name = cursor.getString(1);
                    String mime = cursor.getString(2);

                    Log.d(TAG, "Found item: " + name + " [MIME: " + mime + "]");

                    if (name.toLowerCase().endsWith(".wedcard") && !name.toLowerCase().startsWith(".")) {
                        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId);
                        Log.i(TAG, "Adding WedCard file: " + name + " -> " + fileUri);

                        addCatalogEntryFromUri(fileUri);
                        wedCardCount++;
                    }
                }
                if (wedCardLoadListener != null){
                    wedCardLoadListener.onWedCardsLoaded(wedCardCount);
                }
            } else {
                Log.w(TAG, "Cursor is null – unable to read folder contents.");
                if (wedCardLoadListener != null) wedCardLoadListener.onWedCardsLoaded(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while querying folder: ", e);
            if (wedCardLoadListener != null) wedCardLoadListener.onWedCardsLoaded(0);
        }
    }

    public void loadFromLegacyDir(File folder){
        if (folder == null || !folder.exists()){
            Log.e(TAG, "loadWedCardsFromLegacyDir called with invalid folder: " + folder);
            if (wedCardLoadListener != null) wedCardLoadListener.onWedCardsLoaded(0);
            return;
        }
        Log.i(TAG, "Scanning legacy folder path: " + folder.getAbsolutePath());

        cardList.clear(); // prevent duplicates
        int wedCardCount = 0;


        File[] files = folder.listFiles();
        if (files == null) {
            Log.w(TAG, "No files found or folder inaccessible.");
            if (wedCardLoadListener != null) wedCardLoadListener.onWedCardsLoaded(0);
            return;
        }

        Log.d(TAG, "Found " + files.length + " items in legacy folder.");


        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();

                Log.d(TAG, "Found item: " + name + " [Path: " + file.getAbsolutePath() + "]");

                if (name.toLowerCase().endsWith(".wedcard") && !name.startsWith(".")) {
                    Uri fileUri = Uri.fromFile(file);
                    Log.i(TAG, "Adding WedCard file: " + name + " -> " + fileUri);

                    addCatalogEntryFromUri(fileUri);
                    wedCardCount++;
                }
            }
        }

        if (wedCardLoadListener != null){
            wedCardLoadListener.onWedCardsLoaded(wedCardCount);
        }
    }

    private void addCatalogEntryFromUri(Uri fileUri) {
        try {
            File tempFile = new File(context.getCacheDir(), "temp_" + System.currentTimeMillis() + ".wedcard");

            try (FileOutputStream out = new FileOutputStream(tempFile);
                 InputStream in = context.getContentResolver().openInputStream(fileUri)) {
                if (in != null) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            }
            addCatalogEntry(tempFile, fileUri); // Reuse file-based logic
        } catch (Exception e) {
            Log.e(TAG, "Failed to process SAF WedCard file", e);
        }
    }

    private void addCatalogEntry(File wedcardFile, Uri safUri) {
        try {
            File tmpDir = new File(context.getCacheDir(), "unzipped_" + System.currentTimeMillis());
            unzip(wedcardFile, tmpDir);

            File json = new File(tmpDir, "card.json");
            String jsonStr = new String(Files.readAllBytes(json.toPath()));
            JSONObject obj = new JSONObject(jsonStr);

            String bride = obj.optString("bride", "Unknown");
            String groom = obj.optString("groom", "Unknown");
            String title = bride + " & " + groom;
            String date = obj.optString("date", "Unknown");

            cardList.add(new CardCatalog(title, date, R.drawable.envolope_1, wedcardFile, safUri));

        } catch (Exception e) {
            Log.e(TAG, "Failed to process " + wedcardFile, e);
        }
    }

    private void unzip(File zipFile, File targetDir) throws IOException {
        Log.d(TAG, "TargetDir " + targetDir.getAbsolutePath());
        if (!targetDir.exists()) if (targetDir.mkdirs()) Log.d("MyCards", "TargetDir created");

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    if (outFile.mkdirs())
                        Log.d("MyCards", "Directory created: " + outFile.getAbsolutePath());
                } else {
                    if (!Objects.requireNonNull(outFile.getParentFile()).exists())
                        if (outFile.getParentFile().mkdirs())
                            Log.d("MyCards", "Directory created: " + outFile.getParentFile().getAbsolutePath());
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    // Helper class to show context menu on long click App-wide
    public void showPopup(View view, int position) {
        @SuppressLint("InflateParams") View popupView = LayoutInflater.from(view.getContext()).inflate(R.layout.mycards_context_menu_layout, null);

        PopupWindow popup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popup.setElevation(8f);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight = popupView.getMeasuredHeight();

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        int xOffset = (view.getWidth() / 2) - (popupWidth / 2);
        int yOffset = -popupHeight - 20;

        popupView.findViewById(R.id.btn_edit).setOnClickListener(v -> {
            Toast.makeText(view.getContext(), "Edit", Toast.LENGTH_SHORT).show();
            popup.dismiss();
//            onEdit(position);
        });
        popupView.findViewById(R.id.btn_share).setOnClickListener(v -> {
            popup.dismiss();
            onShare(context, position);
        });
        popupView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            popup.dismiss();
            new AlertDialog.Builder(view.getContext()).setTitle("Delete Draft")
                    .setMessage("Are you sure you want to delete this draft?")
                    .setPositiveButton("Delete", (dialog, which) -> onDelete(position))
                    .setNegativeButton("Cancel", null).show();
        });

        popup.showAsDropDown(view, xOffset, yOffset);
    }

    private void onShare(Context context, int pos) {
        CardCatalog shareCard = cardList.get(pos);
        File file = shareCard.getWedCardFile().getAbsoluteFile();

        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }

        ShareWedCardDialogFragment dialog = ShareWedCardDialogFragment.newInstance(file);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "share_dialog");
    }

    private void onDelete(int pos) {
        CardCatalog deletingCard = cardList.get(pos);
        File cachedFile = deletingCard.getWedCardFile().getAbsoluteFile();
        Log.d(TAG, "Cached File to be delete: " + cachedFile);
        if (cachedFile.exists()) {
            if (cachedFile.delete()) Log.e(TAG, "Cached File deleted: " + cachedFile);
        }
        Uri deleteSAFFile = deletingCard.getSafUri();
        Log.d(TAG, "SAF Uri for the file: " + deleteSAFFile);
        try {
            DocumentsContract.deleteDocument(context.getContentResolver(), deleteSAFFile);
            if (listener != null) listener.onWedCardDelete(pos);
            else Log.e(TAG, "Listener is empty: "+ null);
        } catch (Exception e) {
            Log.e("MyCards", "Failed to delete SAF file", e);
            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnWedCardChangedListener {
        void onWedCardDelete(int position);
    }
    public interface WedCardLoadListener{
        void onWedCardsLoaded(int count);
    }
}
