package com.example.libraryapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookDao {

    @Insert
    public void insertBook(Book book);

    @Update
    public void updateBook(Book book);

    @Delete
    public void deleteBook(Book book);

    @Query("SELECT * FROM books")
    public List<Book> getAllBooks();


    @Query("SELECT * FROM Books WHERE Pages > 300")
    List<Book> getBooksAbove300Pages();

    @Query("SELECT title FROM books WHERE id = :i")
    String getBookTitle(int i);

    @Query("SELECT * FROM books WHERE id = :i")
    Book getBookById(int i);
}
