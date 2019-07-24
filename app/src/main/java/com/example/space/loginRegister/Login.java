package com.example.space.loginRegister;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.space.MainActivity;
import com.example.space.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity
{
    private Button login_l,phone_l;
    private EditText email_l,password_l;
    private TextView forgot_l,register_l;
    private ImageView loginImage;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();


        initializeFields();

        register_l.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
             Intent i = new Intent(Login.this,Register.class);
             startActivity(i);
            }
        });

        login_l.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                login();
            }
        });

    }

    private void initializeFields()
    {
        login_l = findViewById(R.id.btn_login);
        phone_l = findViewById(R.id.phone_btn_login);

        email_l = findViewById(R.id.email_login);
        password_l = findViewById(R.id.password_login);

        forgot_l = findViewById(R.id.forget_password_link);
        register_l = findViewById(R.id.register_link);

        loginImage = findViewById(R.id.login_image);
    }

    private void sendUserToMain()
    {
        Intent i = new Intent(Login.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void login()
    {
        String email = email_l.getText().toString();
        String password = password_l.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please make sure all fields are Entered", Toast.LENGTH_LONG).show();
        }
        else
        {
            auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(Login.this, "Welcome", Toast.LENGTH_SHORT).show();
                                sendUserToMain();
                            }else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(Login.this, "Error" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
