package com.app.interactiveclass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.interactiveclass.MainActivity;
import com.app.interactiveclass.Model.User;
import com.app.interactiveclass.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    EditText editTextEmail;
    EditText editTextPass;
    Button btnLogin;
    TextView txtCreateAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail=findViewById(R.id.editTextEmail);
        editTextPass=findViewById(R.id.editTextPass);
        btnLogin=findViewById(R.id.btnLogin);
        txtCreateAccount=findViewById(R.id.txtCreateAccount);

        btnLogin.setOnClickListener(this);
        txtCreateAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {

        if(view==btnLogin)
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
            if(editTextPass.getText().toString().length()<6)
            {
                Toast.makeText(this,"Password Should be of at least 6 digits",Toast.LENGTH_LONG).show();
                return;
            }

            User user=new User();
            user.setEmail(editTextEmail.getText().toString());
            user.setPassword(editTextPass.getText().toString());


            FirebaseAuth.getInstance().signInWithEmailAndPassword(user.getEmail(),user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(LoginActivity.this,"Authentication Successful",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,"Email or Password is Wrong.",Toast.LENGTH_LONG).show();

                    }
                }
            });

        }

        if(view==txtCreateAccount)
        {
            startActivity(new Intent(this,RegisterActivity.class));
        }


    }
}