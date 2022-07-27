package com.eos.todolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.eos.todolist.databinding.ActivityAddTodoBinding
import com.eos.todolist.db.AppDatabase
import com.eos.todolist.db.ToDoDao
import com.eos.todolist.db.ToDoEntity

class AddTodoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTodoBinding
    private lateinit var db: AppDatabase
    private lateinit var toDoDao: ToDoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)!!
        toDoDao = db.getTodoDao()

        binding.btnCompletion.setOnClickListener {
            insertTodo()
        }
    }

    private fun insertTodo() {
        val todoTitle = binding.edtTitle.text.toString()
        var todoImportance = binding.radioGroup.checkedRadioButtonId

        todoImportance = when (todoImportance) {
            R.id.btn_high -> {
                1
            }
            R.id.btn_middle -> {
                2
            }
            R.id.btn_low -> {
                3
            }
            else -> {
                -1
            }
        }

        // 제대로 기입되지 않은 경우를 제외할 것
        // todoTitle 이 빈칸이거나, todoImportance 가 -1인 경우를 예외 처리

        if (todoImportance == -1 || todoTitle.isBlank()) {
            // 알림을 넣어준다. Toast
            Toast.makeText(this, "모든 항목을 채워주세요",
                Toast.LENGTH_SHORT).show()
        } else {
            // 제대로 넣어준 것이므로 진짜 데이터베이스에 그대로 정보를 넣어주면 된다.
            // 데이터베이스 작업은 백그라운드에서 진행해야 함.
            Thread {
                toDoDao.insertTodo(ToDoEntity(null, todoTitle, todoImportance))
                runOnUiThread {
                    Toast.makeText(this, "추가되었습니다.",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }
    }
}