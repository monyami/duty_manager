// File: AppDatabase.java
package com.example.duty_manager;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/** Simple Room database with a single table. */
@Database(entities = {Note.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();
}
