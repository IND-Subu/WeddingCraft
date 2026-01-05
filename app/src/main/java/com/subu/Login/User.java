package com.subu.Login;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a user entity in the local Room database.
 */
@Entity
public class User {
    /**
     * The unique ID of the user.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * The name of the user.
     */
    public String name;
    
    /**
     * The email of the user.
     */
    public String email;
    
    /**
     * The mobile number of the user.
     */
    public String mobile;
    
    /**
     * A flag indicating whether the user is currently logged in.
     */
    public boolean isLoggedIn;
}
