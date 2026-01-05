package com.subu.weddingcraft;

import android.net.Uri;
import android.util.Log;

import java.io.File;

public class CardCatalog {
    // this class is a enum class representing the card format for new card entry
    String title;
    String date;
    private final int imageResId;
    File wedCardFile;
    private final Uri safUri;

    public CardCatalog(String title, String date, int imageResId, Uri safUri){
        this.title = title;
        this.date = date;
        this.imageResId = imageResId;
        this.safUri = safUri;
    }
    public CardCatalog(String title, String date, int imageResId, File wedCardFile, Uri safUri){
        this.title = title;
        this.date = date;
        this.imageResId = imageResId;
        this.wedCardFile = wedCardFile;
        this.safUri = safUri;
    }

    public Uri getSafUri(){
        return safUri;
    }

    public String getTitle(){
        return title;
    }

    public int getImageResId(){
        Log.d("CardCatalog", "getImageResId: " + imageResId);
        return imageResId;
    }

    public String getDate(){return date;}

    public File getWedCardFile() {
        return wedCardFile;
    }
}