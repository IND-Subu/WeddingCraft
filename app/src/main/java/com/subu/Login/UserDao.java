package com.subu.Login;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for the User entity.
 */
@Dao
public interface UserDao {
    /**
     * Inserts a new user into the database.
     * @param user The user to insert.
     */
    @Insert
    void insert(User user);

    /**
     * Updates an existing user in the database.
     * @param user The user to update.
     */
    @Update
    void update(User user);

    /**
     * Retrieves the currently logged-in user.
     * @return The logged-in user, or null if no user is logged in.
     */
    @Query("SELECT * FROM User WHERE isLoggedIn = 1 LIMIT 1")
    User getLoggedInUser();

    /**
     * Retrieves all users from the database.
     * @return A list of all users.
     */
    @Query("SELECT * FROM User")
    List<User> getAllUsers();

    /**
     * Deletes all users from the database.
     */
    @Query("DELETE FROM User")
    void clearAll();

    /**
     * Logs out the currently logged-in user.
     */
    @Query("UPDATE User SET isLoggedIn = 0 WHERE isLoggedIn = 1")
    void logoutUser();
}
