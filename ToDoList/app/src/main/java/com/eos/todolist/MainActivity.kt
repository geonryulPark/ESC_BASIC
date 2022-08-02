package com.eos.todolist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.eos.todolist.databinding.ActivityMainBinding
import com.eos.todolist.db.AppDatabase
import com.eos.todolist.db.ToDoDao
import com.eos.todolist.db.ToDoEntity

class MainActivity : AppCompatActivity(), OnItemLongClickListener {

    // 2일차 : 메인 액티비티에서 데이터베이스로부터 할 일을 가져온다.
    // 이후 리사이클러뷰를 통해서 화면에 데이터를 보여준다.

    private lateinit var binding: ActivityMainBinding

    private lateinit var db : AppDatabase
    private lateinit var toDoDao: ToDoDao

    private lateinit var todoList: ArrayList<ToDoEntity>
    private lateinit var adapter: TodoRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivity(intent)
        }

        // db instance 를 가져오고, dao 까지 불러온다.
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
        runOnUiThread {
            adapter = TodoRecyclerViewAdapter(todoList, this)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    override fun onRestart() {
        super.onRestart()
        getAllTodoList()
    }

    override fun onLongClick(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("할 일 삭제")
        builder.setMessage("정말 삭제하시겠습니까?")
//        builder.setNegativeButton("아니오", null)
        builder.setPositiveButton("네"
        ) { p0, p1 -> deleteTodo(position) }
        builder.setNegativeButton("아니오", null)
        builder.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteTodo(position: Int) {
        Thread {
            toDoDao.deleteTodo(todoList[position])
            todoList.removeAt(position)
            runOnUiThread {
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "삭제되었습니다.",
                    Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}