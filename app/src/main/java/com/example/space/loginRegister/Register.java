package com.example.space.loginRegister;

import android.app.ProgressDialog;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity
{
    private ImageView registerImage;
    private Button register_r;
    private EditText email_r,password_r;
    private TextView login_r;
    private FirebaseAuth auth;
    private ProgressDialog loadingBar;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initializeFields();

        login_r.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(Register.this,Login.class);
                startActivity(i);
            }
        });

        register_r.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
             createAccount();
            }
        });

    }

    private void initializeFields()
    {
        loadingBar = new ProgressDialog(this);
        register_r = findViewById(R.id.btn_register);

        email_r = findViewById(R.id.email_register);
        password_r = findViewById(R.id.password_register);

        login_r = findViewById(R.id.login_link_register);

        registerImage = findViewById(R.id.register_image);
    }

    private void createAccount()
    {
        String email = email_r.getText().toString();
        String password = password_r.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please make sure all fields are Entered", Toast.LENGTH_LONG).show();
        }
        else
        {
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Just a Moment...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                String currentUserId = auth.getCurrentUser().getUid();
                                rootRef.child("Users").child(currentUserId).setValue("");
                                sendUserToMain();
                                Toast.makeText(Register.this, "Account Created Successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(Register.this, "Error: " +message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToMain()
    {
        Intent i = new Intent(Register.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
