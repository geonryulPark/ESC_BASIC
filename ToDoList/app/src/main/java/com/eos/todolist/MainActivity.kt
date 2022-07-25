package com.eos.todolist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eos.todolist.databinding.ActivityMainBinding
import com.eos.todolist.db.AppDatabase
import com.eos.todolist.db.ToDoDao
import com.eos.todolist.db.ToDoEntity

class MainActivity : AppCompatActivity() {

    // 2일차 : 메인 액티비티에서 데이터베이스로부터 할 일을 가져온다.
    // 이후 리사이클러뷰를 통해서 화면에 데이터를 보여준다.

    private lateinit var binding: ActivityMainBinding

    private lateinit var db : AppDatabase
    private lateinit var toDoDao: ToDoDao
    private lateinit var todoList: ArrayList<ToDoEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivity(intent)
        }

        // db instance 를 가져오고, dao까지 불러온다.
        db = AppDatabase.getInstance(this)!!
        toDoDao = db.getTodoDao()

        getAllTodoList()
    }

    private fun getAllTodoList() {
        Thread {
            todoList = ArrayList(toDoDao.getAll())
            setRecyclerView()
        }.start()
    }

    private fun setRecyclerView() {

    }

}