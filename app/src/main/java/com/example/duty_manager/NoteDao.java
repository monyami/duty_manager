// File: NoteDao.java
package com.example.duty_manager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY done ASC, id DESC")
    List<Note> getAll();

    @Insert
    long insert(Note note);

    @Update
    int update(Note note);

    @Delete
    int delete(Note note);
}
