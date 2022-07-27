package com.eos.todolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eos.todolist.databinding.ItemTodoBinding
import com.eos.todolist.db.ToDoEntity

class TodoRecyclerViewAdapter(private val todoList: ArrayList<ToDoEntity>)
    : RecyclerView.Adapter<TodoRecyclerViewAdapter.MyViewHolder>() {

        inner class MyViewHolder(binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
            val tv_importance = binding.tvImportance
            val tv_title = binding.tvTitle

            val root = binding.root
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // MyViewHolder 에서 만든 뷰홀더 객체를 생성
        // 뷰 홀더를 새로 만들 때 필요한 함수
        val binding: ItemTodoBinding =
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // 받은 데이터를 어떻게 onCreateViewHolder 에 넣어줄지를 결정
        // 뷰 홀더를 데이터와 연결할 때 호출되는 함수
        val todoData = todoList[position]

        when (todoData.importance) {
            1 -> {
                holder.tv_importance.setBackgroundResource(R.color.red)
            }
            2 -> {
                holder.tv_importance.setBackgroundResource(R.color.yellow)
            }
            3 -> {
                holder.tv_importance.setBackgroundResource(R.color.green)
            }
        }

        holder.tv_importance.text = todoData.importance.toString()
        holder.tv_title.text = todoData.title
    }

    override fun getItemCount(): Int {
        // 데이터의 개수를 반환
        return todoList.size
    }
}