package com.example.space;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private String receiverUserId, currentState, senderUserId;
    private CircleImageView profileImage;
    private TextView UserName,UserStatus;
    private Button sendMsg, declineMsg;

    private DatabaseReference userRef, chatRequestRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        senderUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();

        profileImage = findViewById(R.id.visit_profile_image);

        UserName = findViewById(R.id.visit_user_name);
        UserStatus = findViewById(R.id.visit_user_status);

        sendMsg = findViewById(R.id.visit_send_message_btn);
        declineMsg = findViewById(R.id.decline_message_btn);

        currentState = "new";

        retrieveInfo();
    }

    private void retrieveInfo()
    {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userNm = dataSnapshot.child("name").getValue().toString();
                    String userSt = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(profileImage);

                    UserName.setText(userNm);
                    UserStatus.setText(userSt);

                    manageChatRequest();
                }
                else
                {
                    String userNm = dataSnapshot.child("name").getValue().toString();
                    String userSt = dataSnapshot.child("status").getValue().toString();

                    UserName.setText(userNm);
                    UserStatus.setText(userSt);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void manageChatRequest()
    {
        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                currentState = "request_sent";
                                sendMsg.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received"))
                            {
                                currentState = "request_received";
                                sendMsg.setText("Accept Chat Request");

                                declineMsg.setVisibility(View.VISIBLE);
                                declineMsg.setEnabled(true);

                                declineMsg.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        cancelRequest();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserId.equals(receiverUserId))
        {
            sendMsg.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sendMsg.setEnabled(false);
                    if (currentState.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        cancelRequest();
                    }
                }
            });
        }
        else
        {
            sendMsg.setVisibility(View.INVISIBLE);
        }
    }

    private void cancelRequest()
    {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMsg.setEnabled(true);
                                                currentState = "new";
                                                sendMsg.setText("Send Message");

                                                declineMsg.setVisibility(View.INVISIBLE);
                                                declineMsg.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest()
    {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMsg.setEnabled(true);
                                                currentState = "request_sent";
                                                sendMsg.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
