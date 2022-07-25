package com.eos.todolist.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

interface ToDoDao {
    @Insert
    fun insertTodo(todo: ToDoEntity)

    @Delete
    fun deleteTodo(todo: ToDoEntity)

    @Query("SELECT * FROM ToDoEntity")
    fun getAll() : List<ToDoEntity>
}