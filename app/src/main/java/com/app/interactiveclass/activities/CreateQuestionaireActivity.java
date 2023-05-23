package com.app.interactiveclass.activities;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.interactiveclass.Model.Question;
import com.app.interactiveclass.R;
import com.app.interactiveclass.adapter.QuestionAdapter;
import com.app.interactiveclass.interfaces.OnRecyclerViewClickListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CreateQuestionaireActivity extends AppCompatActivity {

    private EditText questionEditText;
    private EditText option1EditText;
    private EditText option2EditText;
    private EditText option3EditText;
    private RadioGroup answerRadioGroup;
    private EditText timerEditText;
    private MaterialButton sendButton,saveButton,btnCancel,btnImportQuestionnaire;
    private String question_id="";
    private String channelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_questionaire);

        channelName=getIntent().getStringExtra("Channel_Name");
        questionEditText = findViewById(R.id.questionEditText);
        option1EditText = findViewById(R.id.option1EditText);
        option2EditText = findViewById(R.id.option2EditText);
        option3EditText = findViewById(R.id.option3EditText);
        answerRadioGroup = findViewById(R.id.answerRadioGroup);
        timerEditText = findViewById(R.id.timerEditText);
        sendButton = findViewById(R.id.btnSendQuestionnaire);
        saveButton = findViewById(R.id.btnSaveQuestionnaire);
        btnImportQuestionnaire = findViewById(R.id.btnImportQuestionnaire);
        btnCancel = findViewById(R.id.btnCancel);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = questionEditText.getText().toString();
                String opt1 = option1EditText.getText().toString();
                String opt2 = option2EditText.getText().toString();
                String opt3 = option3EditText.getText().toString();
                int answer = getSelectedAnswer();
                int timer = Integer.parseInt(timerEditText.getText().toString());

                if (question.isEmpty() || opt1.isEmpty() || opt2.isEmpty() || opt3.isEmpty() || timer == 0 || answer==0) {
                    Toast.makeText(CreateQuestionaireActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Question newQuestion = new Question(question, opt1, opt2, opt3, answer, timer);
                sendQuestion(newQuestion);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = questionEditText.getText().toString();
                String opt1 = option1EditText.getText().toString();
                String opt2 = option2EditText.getText().toString();
                String opt3 = option3EditText.getText().toString();
                int answer = getSelectedAnswer();
                int timer = Integer.parseInt(timerEditText.getText().toString());

                if (question.isEmpty() || opt1.isEmpty() || opt2.isEmpty() || opt3.isEmpty() || timer == 0 ) {
                    Toast.makeText(CreateQuestionaireActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(answer==0)
                {
                    Toast.makeText(CreateQuestionaireActivity.this, "Please select radio button for correct answer", Toast.LENGTH_SHORT).show();
                    return;
                }

                Question newQuestion = new Question(question, opt1, opt2, opt3, answer, timer);
                saveQuestion(newQuestion);
            }
        });

        btnImportQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importQuestions();
            }
        });
    }

    private void importQuestions() {

        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Importing Questions..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        List<Question> questionList=new ArrayList<>();
        String host_id= FirebaseAuth.getInstance().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("saved_questions").child(host_id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Question question=snapshot1.getValue(Question.class);
                    questionList.add(question);
                }
                if(questionList.isEmpty())
                {

                    Toast.makeText(CreateQuestionaireActivity.this, "No Questions found on firebase", Toast.LENGTH_SHORT).show();

                }else
                {
                    showQuestionsOnList(questionList);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(CreateQuestionaireActivity.this, "Failed to load..", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showQuestionsOnList(List<Question> questionList) {

        Dialog dialog=new Dialog(this);

        dialog.setContentView(R.layout.dialog_questions_list);
        dialog.getWindow().setLayout(MATCH_PARENT,WRAP_CONTENT);
        dialog.show();

        RecyclerView recyclerView=dialog.findViewById(R.id.recyclerView);
        QuestionAdapter questionAdapter=new QuestionAdapter(questionList, new OnRecyclerViewClickListener() {
            @Override
            public void onClick(int pos)
            {
                dialog.dismiss();
                setQuestion(questionList.get(pos));
            }
        });


        recyclerView.setAdapter(questionAdapter);

    }

    private void setQuestion(Question question) {
        questionEditText.setText(question.question);
        option1EditText.setText(question.opt1);
        option2EditText.setText(question.opt2);
        option3EditText.setText(question.opt3);
        timerEditText.setText(""+question.timer);
    }

    private void saveQuestion(Question question) {
        String host_id= FirebaseAuth.getInstance().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("saved_questions").child(host_id);
        question.question_id=question_id=ref.push().getKey();
        question.host_id=host_id;
        ref.child(question_id).setValue(question);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    DatabaseReference questionnairesRef;
    private void sendQuestion(Question question)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        questionnairesRef = database.getReference("questionnaires").child(channelName);
        questionnairesRef.removeValue();
        question.question_id=question_id=questionnairesRef.push().getKey();
        question.host_id= FirebaseAuth.getInstance().getUid();

        question.create_time=System.currentTimeMillis();
        question.duration=question.timer*60*1000;

        questionnairesRef.child(question_id).setValue(question);


        Dialog dialog=new Dialog(this);
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.lyt_dialog_result);
        dialog.getWindow().setLayout(MATCH_PARENT,WRAP_CONTENT);
        dialog.show();


        TextView txtTimer=dialog.findViewById(R.id.txtTimer);
        MaterialButton btnCancel=dialog.findViewById(R.id.btnCancel);
        CountDownTimer count=new CountDownTimer(   question.duration,1000 )
        {

            @Override
            public void onTick(long millisUntilFinished) {
                String timeString = millisToTime(millisUntilFinished);
                txtTimer.setText(timeString);
            }

            @Override
            public void onFinish() {
                questionnairesRef.child(question_id).removeValue();
                showResult(dialog,question);
            }
        };
        count.start();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count.cancel();
                dialog.dismiss();
                questionnairesRef.child(question_id).removeValue();
            }
        });
    }

    private void showResult(Dialog dialog, Question question) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("results").child(channelName);

        LinearLayout lyt_wait=dialog.findViewById(R.id.lyt_wait);
        LinearLayout lyt_result=dialog.findViewById(R.id.lyt_result);

        TextView txtTotalParticipents=dialog.findViewById(R.id.txtTotalParticipents);
        TextView txtCorrectAnswers=dialog.findViewById(R.id.txtCorrectAnswers);
        TextView txtWrongAnswers=dialog.findViewById(R.id.txtWrongAnswers);

        ProgressBar progressCorrectAns=dialog.findViewById(R.id.progressCorrectAns);
        ProgressBar progressWrongAns=dialog.findViewById(R.id.progressWrongAns);

        MaterialButton btnSave=dialog.findViewById(R.id.btnSave);
        MaterialButton btnCancel=dialog.findViewById(R.id.btnCancel2);

        lyt_wait.setVisibility(View.GONE);
        lyt_result.setVisibility(View.VISIBLE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference("saved_questions").child(question.host_id).child(question.question_id).setValue(question);
                Toast.makeText(CreateQuestionaireActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalParticipants= (int) snapshot.getChildrenCount();
                int correctAnswers=0;
                int wrongAnswers=0;
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Question question=snapshot1.getValue(Question.class);
                    if(question.userAnswer==question.answer)
                        correctAnswers++;
                    else
                        wrongAnswers++;
                }

                txtTotalParticipents.setText("Total Participants : "+totalParticipants);
                txtCorrectAnswers.setText("Correct Answers : "+correctAnswers);
                txtWrongAnswers.setText("Wrong Answers : "+wrongAnswers);

                progressCorrectAns.setMax(totalParticipants);
                progressWrongAns.setMax(totalParticipants);

                progressCorrectAns.setProgress(correctAnswers);
                progressWrongAns.setProgress(wrongAnswers);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static String millisToTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    private int getSelectedAnswer() {
        int selectedId = answerRadioGroup.getCheckedRadioButtonId();
        switch (selectedId) {
            case R.id.answer1RadioButton:
                return 1;
            case R.id.answer2RadioButton:
                return 2;
            case R.id.answer3RadioButton:
                return 3;
            default:
                return 0;
        }
    }
}
