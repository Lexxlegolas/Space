package com.example.space;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.space.loginRegister.Register;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity
{
    private Button update;
    private EditText username,status;
    private CircleImageView profileImage;
    private String currentUserId;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        //username.setVisibility(View.INVISIBLE);

        initializeFields();

        update.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                updateSettings();
            }
        });
        retrieveUserInfo();
    }

    private void initializeFields()
    {
        update = findViewById(R.id.update_settings);

        username = findViewById(R.id.name_settings);
        status = findViewById(R.id.status_settings);

        profileImage = findViewById(R.id.profile_image_settings);
    }

    private void updateSettings()
    {
        String setName = username.getText().toString();
        String setStatus = status.getText().toString();

        if (TextUtils.isEmpty(setName) || TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please make sure all fields are Entered", Toast.LENGTH_LONG).show();
        }
        else
        {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setName);
            profileMap.put("status",setStatus);

            rootRef.child("Users").child(currentUserId).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                sendUserToMain();
                                Toast.makeText(Settings.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(Settings.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void retrieveUserInfo()
    {
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                {
                    String retrieveName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfImage = dataSnapshot.child("Image").getValue().toString();

                    username.setText(retrieveName);
                    status.setText(retrieveStatus);
                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                {
                    String retrieveName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                    username.setText(retrieveName);
                    status.setText(retrieveStatus);
                }
                else
                {
                   //e: username.setVisibility(View.VISIBLE);
                    Toast.makeText(Settings.this, "Please set and update profile Information", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToMain()
    {
        Intent i = new Intent(Settings.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
