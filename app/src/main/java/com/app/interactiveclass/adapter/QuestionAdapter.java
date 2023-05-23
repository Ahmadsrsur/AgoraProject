package com.app.interactiveclass.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.interactiveclass.Model.Question;
import com.app.interactiveclass.R;
import com.app.interactiveclass.interfaces.OnRecyclerViewClickListener;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
    private List<Question> questionList;
    private OnRecyclerViewClickListener onRecyclerViewClickListener;

    public QuestionAdapter(List<Question> questionList,OnRecyclerViewClickListener onRecyclerViewClickListener) {
        this.questionList = questionList;
        this.onRecyclerViewClickListener=onRecyclerViewClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question question = questionList.get(position);
        holder.txtQuestion.setText(question.question);
        holder.btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecyclerViewClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtQuestion;
        MaterialButton btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestion = itemView.findViewById(R.id.txtQuestion);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }
}