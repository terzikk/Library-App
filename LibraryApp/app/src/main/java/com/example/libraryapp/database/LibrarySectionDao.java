package com.example.libraryapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LibrarySectionDao {
    @Insert
    void insertSection(LibrarySection section);

    @Update
    void updateSection(LibrarySection section);

    @Delete
    void deleteSection(LibrarySection section);

    @Query("SELECT * FROM library_sections")
    public List<LibrarySection> getAllSections();

    @Query("SELECT library_sections.*, COUNT(books.id) AS bookCount " +
            "FROM library_sections " +
            "LEFT JOIN books ON library_sections.id = books.sectionId " +
            "GROUP BY library_sections.id")
    public List<LibrarySection> getAllSectionsWithBookCount();



}
