package com.eos.todolist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. abstract class
// 2. entity 에 대한 정보를 기입
// 3. DAO 를 반환하는 method 를 구현해야 함.

@Database(entities = arrayOf(ToDoEntity::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getTodoDao() : ToDoDao


    // singleton 구현하기 위한 companion object
    companion object {
        val databaseName = "db_todo"
        var appDatabase: AppDatabase? = null

        fun getInstance(context: Context) : AppDatabase? {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context,
                    AppDatabase::class.java,
                    databaseName).build()
            }
            return appDatabase
        }
    }
}