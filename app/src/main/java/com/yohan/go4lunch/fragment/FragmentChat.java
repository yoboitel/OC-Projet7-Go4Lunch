package com.yohan.go4lunch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.adapter.ChatAdapter;
import com.yohan.go4lunch.model.Message;

import java.util.ArrayList;
import java.util.Date;

public class FragmentChat extends Fragment {

    private RecyclerView rcChat;
    private ChatAdapter chatAdapter;
    private ArrayList<Message> messagesList;
    private EditText etMessageZone;


    public FragmentChat() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        //Init RecyclerView And Adapter
        etMessageZone = v.findViewById(R.id.etMessage);
        messagesList = new ArrayList<>();
        rcChat = v.findViewById(R.id.rcChat);
        chatAdapter = new ChatAdapter(requireContext(), messagesList);
        rcChat.setLayoutManager(new LinearLayoutManager(requireActivity()));
        rcChat.setHasFixedSize(true);
        rcChat.setAdapter(chatAdapter);

        //Load chat messages from firestore on init
        loadChatMessagesFromFirestore();

        //Send Message to Firestore and Refresh with the ChatAdapter
        ImageButton ivSend = v.findViewById(R.id.ivSend);
        ivSend.setOnClickListener(view -> {

            if (!etMessageZone.getText().toString().isEmpty()) {

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                    //Get user name and photo before sending message to firestore
                    FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                        if (task.getResult() != null) {
                            String userName = task.getResult().getString("firstnameAndName");
                            String userPhotoUri = task.getResult().getString("photoUrl");
                            sendMessageToFirestore(etMessageZone.getText().toString(), userName, userPhotoUri);
                        }
                    });

                }

            } else
                Toast.makeText(requireContext(), R.string.empty_message_error, Toast.LENGTH_SHORT).show();
        });

        return v;
    }

    private void loadChatMessagesFromFirestore() {

        if (messagesList.size() > 0)
            messagesList.clear();

        //Get user name and photo before sending message to firestore
        FirebaseFirestore.getInstance().collection("Chat").orderBy("timestamp", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {

            if (task.getResult() != null) {
                for (DocumentSnapshot document : task.getResult().getDocuments()) {

                    String message = document.getString("message");
                    String authorPhotoUrl = document.getString("authorPhotoUrl");
                    String authorName = document.getString("authorName");
                    String authorUid = document.getString("authorUid");
                    Date timestamp = document.getDate("timestamp");

                    Message messageToAdd = new Message(message, authorName, authorPhotoUrl, authorUid, timestamp);
                    messagesList.add(messageToAdd);
                    chatAdapter.notifyDataSetChanged();
                    //Smooth scroll to last message
                    rcChat.post(() -> rcChat.smoothScrollToPosition(messagesList.size()));
                }
            }
        });
    }

    //Upload the message in the "Chat" collection in Firestore
    private void sendMessageToFirestore(String message, String userName, String userPhotoUri) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            //We send a null timestamp so firestore set it automatically on the server side
            Message messageToSend = new Message(message, userName, userPhotoUri, FirebaseAuth.getInstance().getCurrentUser().getUid(), null);
            FirebaseFirestore.getInstance().collection("Chat").add(messageToSend).addOnSuccessListener(documentReference -> {

                        messagesList.add(messageToSend);
                        chatAdapter.notifyDataSetChanged();
                        //Clear editText after sending message
                        etMessageZone.getText().clear();
                        //Smooth scroll to last message
                        rcChat.post(() -> rcChat.smoothScrollToPosition(messagesList.size()));
                    }
            ).addOnFailureListener(e -> Toast.makeText(requireActivity(), getString(R.string.firestore_fail_message) + e, Toast.LENGTH_SHORT).show());
        }
    }
}
