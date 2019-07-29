package com.example.space.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.space.R;
import com.example.space.chat.ChatActivity;
import com.example.space.model.Contacts;
import com.example.space.viewHolder.ChatsViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment
{
    private View chatsFragment;
    private RecyclerView recyclerView;
    private DatabaseReference chatRef, userRef;
    private FirebaseAuth auth;
    private String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatsFragment = inflater.inflate(R.layout.fragment_chats, container, false);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = chatsFragment.findViewById(R.id.chat_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return chatsFragment;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
            {
                final String userID = getRef(position).getKey();
                final String[] image = {"default_image"};


                userRef.child(userID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild("image"))
                            {
                                image[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(image[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            final String name = dataSnapshot.child("name").getValue().toString();
                            final String status = dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(name);
                            holder.userStatus.setText("Last Seen: "+"\n"+" Date "+" Time");

                            holder.itemView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent i = new Intent(getContext(), ChatActivity.class);
                                    i.putExtra("visit_user_id",userID);
                                    i.putExtra("visit_user_name",name);
                                    i.putExtra("visit_image", image[0]);
                                    startActivity(i);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                ChatsViewHolder holder = new ChatsViewHolder(view);
                return holder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
}
