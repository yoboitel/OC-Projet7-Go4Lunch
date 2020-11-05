package com.yohan.go4lunch.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.model.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<Message> list;

    public ChatAdapter(Context context, List<Message> list) {
        this.context = context;
        this.list = list;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) { super(itemView); }
    }

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

        //Display messages on Right or Left depending if it's from the connected user or not
        LinearLayout linearItemMessage = holder.itemView.findViewById(R.id.linearItemMessage);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams. MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT ) ;
        if (item.getAuthorUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            //bubble on right
            layoutParams.setMargins( 200 , 15 , 15 , 15 ) ;
        } else {
            //bubble on right
            layoutParams.setMargins( 15 , 15 , 200 , 15 ) ;
        }
        linearItemMessage.setLayoutParams(layoutParams);

        //Setup Views
        TextView messageContent = holder.itemView.findViewById(R.id.messageContent);
        TextView messageName = holder.itemView.findViewById(R.id.messageName);
        SimpleDraweeView ivAuthorPhoto = holder.itemView.findViewById(R.id.messagePhoto);

        messageContent.setText(item.getMessage());
        messageName.setText(item.getAuthorName());
        ivAuthorPhoto.setImageURI(item.getAuthorPhotoUrl());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}