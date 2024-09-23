package com.example.libraryapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Book.class, Member.class, LibrarySection.class}, version = 1)
public abstract class LibraryDatabase extends RoomDatabase {
    public abstract BookDao bookDao();
    public abstract MemberDao memberDao();
    public abstract LibrarySectionDao sectionDao();

}
