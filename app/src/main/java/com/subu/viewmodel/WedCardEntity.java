package com.subu.viewmodel;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wedcards")
public class WedCardEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String fileName;
    public String messageId;
    public String senderUid;
    public String name;
    public String downloadUrl;
    public String deliveryStatus;
    public long receivedAt;
    public Long readAt;
    public boolean isNew = true;
    public boolean isDownload = false;
    public boolean isVisited = false;

    // ✅ Required constructor for Room and your FCM logic
    public WedCardEntity(String fileName,
                         String messageId,
                         String senderUid,
                         String senderName,
                         String downloadUrl,
                         String deliveryStatus,
                         long receivedAt) {
        this.fileName = fileName;
        this.messageId = messageId;
        this.senderUid = senderUid;
        this.name = senderName;
        this.downloadUrl = downloadUrl;
        this.deliveryStatus = deliveryStatus;
        this.receivedAt = receivedAt;
        this.isNew = true;
        this.isDownload = false;
        this.isVisited = false;
        this.readAt = null;
    }

    // (Optional) You can also add a default constructor if Room needs it
    public WedCardEntity() {
    }
}
