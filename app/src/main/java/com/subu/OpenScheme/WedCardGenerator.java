package com.subu.OpenScheme;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WedCardGenerator {
    public static void generateWedCard(Context context, JSONObject userData, String theme, String outputPath) throws IOException, JSONException {
        JSONObject wedCard = new JSONObject();

        // Add basic metadata
        wedCard.put("bride", userData.getString("bride"));
        wedCard.put("groom", userData.getString("groom"));
        wedCard.put("date", userData.getString("date"));
        wedCard.put("time", userData.getString("time"));
        wedCard.put("venue", userData.getString("venue"));
        wedCard.put("host", userData.getString("host"));
        wedCard.put("rsvp", userData.getString("rsvp"));
        wedCard.put("quote", userData.getString("quote"));
        wedCard.put("contact_type", userData.getString("contact_type"));
        wedCard.put("theme", theme);

        // Add new optional fields
        wedCard.put("bride_parents", userData.optString("bride_parents", ""));
        wedCard.put("groom_parents", userData.optString("groom_parents", ""));
        wedCard.put("bride_position", userData.optString("bride_position", ""));
        wedCard.put("groom_position", userData.optString("groom_position", ""));
        wedCard.put("mantra", userData.optString("mantra", ""));

        // Define fold layout
        JSONArray folds = new JSONArray();

        folds.put(makeFold("front", new JSONArray()
                .put(makeTextBlock(userData.getString("bride") + " ❤️ " + userData.getString("groom")))
                .put(makeQuoteBlock(userData.getString("quote")))));

        folds.put(makeFold("left", new JSONArray()
                .put(makeHeadingBlock("Wedding Events"))
                .put(makeEventListBlock(userData.getJSONArray("events")))));

        JSONArray rightBlocks = new JSONArray();
        rightBlocks.put(makeHeadingBlock("Details"));
        rightBlocks.put(makeTextBlock("Hosted by: " + userData.getString("host")));
        rightBlocks.put(makeTextBlock("Venue: " + userData.getString("venue")));
        rightBlocks.put(makeTextBlock("Date & Time: " + userData.getString("date") + " at " + userData.getString("time")));

        // Add new details if available
        if (!userData.optString("bride_parents").isEmpty())
            rightBlocks.put(makeTextBlock("Bride's Parents: " + userData.getString("bride_parents")));

        if (!userData.optString("groom_parents").isEmpty())
            rightBlocks.put(makeTextBlock("Groom's Parents: " + userData.getString("groom_parents")));

        if (!userData.optString("bride_position").isEmpty())
            rightBlocks.put(makeTextBlock("Bride is the " + userData.getString("bride_position")));

        if (!userData.optString("groom_position").isEmpty())
            rightBlocks.put(makeTextBlock("Groom is the " + userData.getString("groom_position")));

        if (!userData.optString("mantra").isEmpty())
            rightBlocks.put(makeTextBlock(userData.getString("mantra")));

        folds.put(makeFold("right", rightBlocks));

        folds.put(makeFold("back", new JSONArray()
                .put(makeHeadingBlock("RSVP"))
                .put(makeTextBlock(userData.getString("rsvp")))
                .put(makeTextBlock("Contact: " + userData.getString("contact_type")))));

        wedCard.put("folds", folds);

        // Begin writing to zip
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(outputPath))))) {
            // Write card.json
            ZipEntry cardEntry = new ZipEntry("card.json");
            zos.putNextEntry(cardEntry);
            zos.write(wedCard.toString(4).getBytes());
            zos.closeEntry();

            // Write fold images
            String[] foldNames = {"front", "left", "right", "back"};
            for (String fold : foldNames) {
                try (InputStream is = context.getAssets().open(theme + "/" + fold + ".jpg")) {
                    ZipEntry imgEntry = new ZipEntry("images/" + fold + ".jpg");
                    zos.putNextEntry(imgEntry);
                    copyStream(is, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    Log.e("wedCardGenerator", "Missing image for " + fold, e);
                }
            }
        }
    }

    private static void copyStream(InputStream is, ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = is.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }
    }

    private static JSONObject makeTextBlock(String text) throws JSONException {
        JSONObject block = new JSONObject();
        block.put("type", "text");
        block.put("content", text);
        return block;
    }

    private static JSONObject makeQuoteBlock(String quote) throws JSONException {
        JSONObject block = new JSONObject();
        block.put("type", "quote");
        block.put("content", quote);
        return block;
    }

    private static JSONObject makeHeadingBlock(String heading) throws JSONException {
        JSONObject block = new JSONObject();
        block.put("type", "heading");
        block.put("content", heading);
        return block;
    }

    private static JSONObject makeEventListBlock(JSONArray events) throws JSONException {
        JSONObject block = new JSONObject();
        block.put("type", "event_list");
        block.put("events", events);
        return block;
    }

    private static JSONObject makeFold(String foldName, JSONArray blocks) throws JSONException {
        JSONObject fold = new JSONObject();
        fold.put("fold", foldName);
        fold.put("image", "images/" + foldName + ".jpg");
        fold.put("blocks", blocks);
        return fold;
    }
}
