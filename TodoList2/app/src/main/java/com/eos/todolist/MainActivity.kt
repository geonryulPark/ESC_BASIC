package com.eos.todolist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.eos.todolist.databinding.ActivityMainBinding
import com.eos.todolist.db.AppDatabase
import com.eos.todolist.db.ToDoDao
import com.eos.todolist.db.ToDoEntity

class MainActivity : AppCompatActivity(), OnItemLongClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var toDoDao: ToDoDao
    private lateinit var todoList: ArrayList<ToDoEntity>
    private lateinit var adapter: TodoRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        // layoutInflater : xml 을 view 객체로 반환시켜준다.
        // inflate() : binding 객체를 만드는 과정
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivity(intent)
        }

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
        builder.setNegativeButton("아니오", null)
        builder.setPositiveButton("네") { p0, p1 -> deleteTodo(position) }
        builder.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteTodo(position: Int) {
        Thread {
            toDoDao.deleteTodo(todoList[position])
            todoList.removeAt(position)
            runOnUiThread {
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "삭제되었씁니다.", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}