package com.example.space.viewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.space.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsViewHolder extends RecyclerView.ViewHolder
{
    public TextView userName,userStatus;
    public CircleImageView profileImage;

    public FindFriendsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        userName = itemView.findViewById(R.id.user_profile_name);
        userStatus = itemView.findViewById(R.id.user_status);
        profileImage = itemView.findViewById(R.id.users_profile_image);
    }
}
