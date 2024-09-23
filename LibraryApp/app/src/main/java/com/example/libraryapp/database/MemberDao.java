package com.example.libraryapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MemberDao {

    @Insert
    public void insertMember(Member member);

    @Update
    public void updateMember(Member member);

    @Delete
    public void deleteMember(Member member);

    @Query("SELECT * FROM members")
    public List<Member> getAllMembers();


    @Query("SELECT name FROM members WHERE id = :memberId")
    String getMemberName(Integer memberId);


}
