package com.eos.todolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.eos.todolist.databinding.ActivityAddTodoBinding
import com.eos.todolist.db.AppDatabase
import com.eos.todolist.db.ToDoDao
import com.eos.todolist.db.ToDoEntity

class AddTodoActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddTodoBinding
    lateinit var db: AppDatabase
    lateinit var toDoDao: ToDoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 화면 보여줌

        db = AppDatabase.getInstance(this)!!
        toDoDao = db.getTodoDao()

        binding.btnCompletion.setOnClickListener {
            insertTodo()
        } // 완료 버튼 클릭하면, 추가하기
    }

    private fun insertTodo() {
        // entity -> title(제목), importance(중요도)

        val todoTitle = binding.edtTitle.text.toString()
        var todoImportance = binding.radioGroup.checkedRadioButtonId

        when (todoImportance) {
            R.id.btn_high -> {
                todoImportance = 1
            }
            R.id.btn_middle -> {
                todoImportance = 2
            }
            R.id.btn_low -> {
                todoImportance = 3
            }

            else -> {
                todoImportance = -1
            }
        }
        // radioButton 에 따라서 중요도를 대입

        // 중요도가 체크되지 않았거나 (-1), 제목이 비어있으면 예외처리
        if (todoImportance == -1 || todoTitle.isBlank()) {
            Toast.makeText(this, "모든 항목을 채워주세요.",
                Toast.LENGTH_SHORT).show()
        } else {
            // 데이터베이스 관련 작업 -> 백그라운드 스레드에서 실행해야만 한다.
            Thread {
                toDoDao.insertTodo(ToDoEntity(null, todoTitle, todoImportance))
                runOnUiThread { // 아래 작업은 Ui thread 에서 실행
                    Toast.makeText(this, "추가 되었습니다.",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }
    }
}