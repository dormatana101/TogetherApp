package com.example.togetherproject.model.local

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    fun deleteAll()
}
