package com.example.space.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.space.R;
import com.example.space.model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessage, receiverMessage;
        public CircleImageView receiverImage;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.sender_message_text);
            receiverMessage = itemView.findViewById(R.id.receiver_messages_text);
            receiverImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout,viewGroup,false);

        auth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i)
    {
        String messageSenderId = auth.getCurrentUser().getUid();

        Messages messages = userMessagesList.get(i);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(fromUserId);

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String image = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverImage);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text"))
        {
            messageViewHolder.receiverMessage.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessage.setVisibility(View.INVISIBLE);

            if (fromUserId.equals(messageSenderId))
            {
                messageViewHolder.senderMessage.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessage.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessage.setTextColor(Color.BLACK);
                messageViewHolder.senderMessage.setText(messages.getMessage());
            }
            else
            {
                messageViewHolder.receiverImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessage.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessage.setBackgroundResource(R.drawable.receivers_message_layout);
                //messageViewHolder.senderMessage.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessage.setText(messages.getMessage());
            }

        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
