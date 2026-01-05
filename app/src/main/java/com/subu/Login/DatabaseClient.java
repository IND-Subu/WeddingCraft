package com.subu.Login;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {

    private static DatabaseClient instance;
    private final UserDatabase userDatabase;

    private DatabaseClient(Context context) {
        userDatabase = Room.databaseBuilder(
                context.getApplicationContext(),
                UserDatabase.class,
                "user-db"
        ).build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

    public UserDatabase getUserDatabase() {
        return userDatabase;
    }
}
