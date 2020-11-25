package com.yohan.go4lunch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.model.Message;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final Context context;
    private final List<Message> list;

    public ChatAdapter(Context context, List<Message> list) {
        this.context = context;
        this.list = list;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_message, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message item = list.get(position);

        //Setup Views
        TextView messageContent = holder.itemView.findViewById(R.id.messageContent);
        TextView messageName = holder.itemView.findViewById(R.id.messageName);
        SimpleDraweeView ivAuthorPhoto = holder.itemView.findViewById(R.id.messagePhoto);

        messageContent.setText(item.getMessage());
        messageName.setText(item.getAuthorName());
        ivAuthorPhoto.setImageURI(item.getAuthorPhotoUrl());

        //Display messages on Right or Left depending if it's from the connected user or not
        LinearLayout linearItemMessage = holder.itemView.findViewById(R.id.linearItemMessage);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            if (item.getAuthorUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                //bubble on right
                layoutParams.setMargins(300, 15, 15, 15);
                linearItemMessage.setBackground(context.getResources().getDrawable(R.drawable.message_bubble_sent));
                messageName.setText(R.string.message_sender_title);
            } else {
                //bubble on left
                layoutParams.setMargins(15, 15, 300, 15);
                linearItemMessage.setBackground(context.getResources().getDrawable(R.drawable.message_bubble_received));
            }
            linearItemMessage.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}