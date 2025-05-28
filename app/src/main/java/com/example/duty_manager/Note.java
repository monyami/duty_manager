// File: Note.java
package com.example.duty_manager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Persistent model. Comments are in English as requested. */
@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String body;
    public boolean done;

    public Note(String title, String body) {
        this.title = title;
        this.body = body;
        this.done = false;
    }
}
