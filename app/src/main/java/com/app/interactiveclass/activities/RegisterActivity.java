package com.app.interactiveclass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.interactiveclass.Model.User;
import com.app.interactiveclass.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    EditText editTextEmail;
    EditText editTextPass;
    EditText editTextRPass;
    Button btnRegister;
    TextView txtHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail=findViewById(R.id.editTextEmail);
        editTextPass=findViewById(R.id.editTextPass);
        editTextRPass=findViewById(R.id.editTextRPass);

        btnRegister=findViewById(R.id.btnRegister);
        txtHaveAccount=findViewById(R.id.txtHaveAccount);

        btnRegister.setOnClickListener(this);
        txtHaveAccount.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view==btnRegister)
        {
            if(editTextEmail.getText().toString().isEmpty() || editTextPass.getText().toString().isEmpty())
            {
                Toast.makeText(this,"Fill All Fields",Toast.LENGTH_LONG).show();
                return;
            }
            if( ! Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString().trim()).matches())
            {
                Toast.makeText(this,"Invalid Email",Toast.LENGTH_LONG).show();
                return;
            }
            if(editTextPass.getText().toString().length()<4)
            {
                Toast.makeText(this,"Password Should be of at least 4 digits",Toast.LENGTH_LONG).show();
                return;
            }
            if(!editTextPass.getText().toString().equals(editTextRPass.getText().toString()))
            {
                Toast.makeText(this,"Password Miss Match",Toast.LENGTH_LONG).show();
                return;
            }


            User user=new User();
            user.setEmail(editTextEmail.getText().toString());
            user.setPassword(editTextPass.getText().toString());

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getEmail(),user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(RegisterActivity.this,"User Created",Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this,"Failed To Create User | Email is already used",Toast.LENGTH_LONG).show();

                    }
                }
            });


        }
        if(view==txtHaveAccount)
        {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}