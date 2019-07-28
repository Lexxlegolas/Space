package com.example.space;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.space.loginRegister.Register;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity
{
    private Button update;
    private EditText username,status;
    private CircleImageView profileImage;
    private String currentUserId;
    private Uri imageUri;
    private String myUrl = "";
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private StorageReference usersProfileImage;
    private String checker = "";
    private StorageTask uploadTask;
    private Toolbar toolbar;

    private static final int GallaryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Images");

        //username.setVisibility(View.INVISIBLE);

        initializeFields();

        update.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (checker.equals("clicked"))
                {
                    userInfoSaved();
                }
                else
                {
                    updateOnlyUserInfo();
                }
            }
        });
        retrieveUserInfo();

        profileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                checker = "clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(Settings.this);
            }
        });
    }

    private void initializeFields()
    {
        toolbar = findViewById(R.id.settings_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");

        update = findViewById(R.id.update_settings);

        username = findViewById(R.id.name_settings);
        status = findViewById(R.id.status_settings);

        profileImage = findViewById(R.id.profile_image_settings);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            profileImage.setImageURI(imageUri);
        }
        else
        {
            Toast.makeText(Settings.this,"Error: Try Again",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.this,Settings.class));
            finish();
        }
    }

    private void updateOnlyUserInfo()
    {
        String setName = username.getText().toString();
        String setStatus = status.getText().toString();
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setName);
            profileMap.put("status",setStatus);

            rootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
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

    private void userInfoSaved()
    {
        String setName = username.getText().toString();
        String setStatus = status.getText().toString();

        if (TextUtils.isEmpty(setName) || TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please make sure all fields are Entered", Toast.LENGTH_LONG).show();
        }
        else if(checker.equals("clicked"))
        {
            uploadImage(setName,setStatus);
        }
    }

    private void uploadImage(final String setName, final String setStatus)
    {
        if (imageUri != null)
        {
            final StorageReference fileref = usersProfileImage
                    .child(currentUserId + ".jpg");

            uploadTask = fileref.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation()
            {
                @Override
                public Object then(@NonNull Task task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUrl = task.getResult();
                        myUrl= downloadUrl.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("name",setName);
                        userMap.put("status",setStatus);
                        userMap.put("uid",currentUserId);
                        userMap.put("image",myUrl);

                        rootRef.child("Users").child(currentUserId).updateChildren(userMap);

                        Toast.makeText(Settings.this,"Profile Updated successfully",Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        Toast.makeText(Settings.this,"Error",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else
        {
            Toast.makeText(Settings.this,"Image is not selected",Toast.LENGTH_SHORT).show();
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
                    String retrieveProfImage = dataSnapshot.child("image").getValue().toString();

                    username.setText(retrieveName);
                    status.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfImage).placeholder(R.drawable.profile_image).into(profileImage);

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
