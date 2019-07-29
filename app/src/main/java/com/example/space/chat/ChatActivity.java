package com.example.space.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.space.R;
import com.example.space.adapter.MessagesAdapter;
import com.example.space.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar chatToolbar;
    private RecyclerView recyclerView;
    private EditText msg;
    private ImageButton send;
    private String messageReceiverName, messageReceiverId,messageReceiverImage, messageSenderId;
    private CircleImageView customImage;
    private TextView customName,customLastSeen;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        messageSenderId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();


        initializeFields();

        customName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(customImage);

        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendMessage();
            }
        });

    }

    private void initializeFields()
    {
        chatToolbar = findViewById(R.id.chat_layoutBar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        msg = findViewById(R.id.input_message);
        send = findViewById(R.id.send_messageBtn);

        customImage = findViewById(R.id.custom_profile_image);
        customName = findViewById(R.id.custom_user_name);
        customLastSeen = findViewById(R.id.custom_last_seen);

        messagesAdapter = new MessagesAdapter(messagesList);
        recyclerView = findViewById(R.id.chat_recycler);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messagesAdapter);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();

                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage()
    {
        final String input = msg.getText().toString();
        if (TextUtils.isEmpty(input))
        {
            Toast.makeText(this, "Text Required", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String messagereceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",input);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody );
            messageBodyDetails.put(messagereceiverRef + "/" + messagePushId, messageTextBody );

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String e = task.getException().toString();
                        Toast.makeText(ChatActivity.this, "Error" + e, Toast.LENGTH_LONG).show();
                    }

                    msg.setText("");
                }
            });
        }
    }
}
