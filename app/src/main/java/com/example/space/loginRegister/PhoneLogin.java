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
import android.widget.Toast;

import com.example.space.MainActivity;
import com.example.space.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLogin extends AppCompatActivity
{
    private Button verifyCode,verifyNumber;
    private EditText phoneNumber,code;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth auth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        auth = FirebaseAuth.getInstance();

        initializeFields();

        verifyCode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String number = phoneNumber.getText().toString();
                if (TextUtils.isEmpty(number))
                {
                    Toast.makeText(PhoneLogin.this, "Please Enter Phone number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Just a Moment...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    getCode(number);
                }
            }
        });

        verifyNumber.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyCode.setVisibility(View.INVISIBLE);
                phoneNumber.setVisibility(View.INVISIBLE);

                String codeMsg = code.getText().toString();
                if (TextUtils.isEmpty(codeMsg))
                {
                    Toast.makeText(PhoneLogin.this, "Please enter Code", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verifying Code");
                    loadingBar.setMessage("Just a Moment...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeMsg);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(PhoneLogin.this, "Please Enter Correct Phone number", Toast.LENGTH_LONG).show();

                verifyCode.setVisibility(View.VISIBLE);
                phoneNumber.setVisibility(View.VISIBLE);

                verifyNumber.setVisibility(View.INVISIBLE);
                code.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Toast.makeText(PhoneLogin.this, "Verification Code Sent", Toast.LENGTH_SHORT).show();

                verifyCode.setVisibility(View.INVISIBLE);
                phoneNumber.setVisibility(View.INVISIBLE);

                verifyNumber.setVisibility(View.VISIBLE);
                code.setVisibility(View.VISIBLE);

            }
        };


    }

    private void initializeFields()
    {
        loadingBar = new ProgressDialog(this);
        verifyNumber = findViewById(R.id.verify);
        verifyCode = findViewById(R.id.verify_code_btn);

        phoneNumber = findViewById(R.id.phone_number);
        code = findViewById(R.id.verify_code);
    }

    private void getCode(String number)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                PhoneLogin.this,               // Activity (for callback binding)
                callbacks);        // OnVerificationStateChangedCallbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            sendUserToMain();

                        }
                        else
                        {
                            String msg = task.getException().toString();
                            Toast.makeText(PhoneLogin.this, "Error : "+ msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMain()
    {
        Intent i = new Intent(PhoneLogin.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
