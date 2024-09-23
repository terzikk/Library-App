package com.example.libraryapp.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "library_sections")
public class LibrarySection {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String location;
    private int bookCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }


    public void setLocation(String location) {
        this.location = location;

    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }


}



