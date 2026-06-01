package com.sagie.myfirstapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sagie.myfirstapplication.models.ChatMessage;
import com.sagie.myfirstapplication.MessageAdapter;
import com.sagie.myfirstapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference chatRef;
    private EditText etMessage;
    private RecyclerView rvChat;
    private ImageButton btnBack, btnSend;
    private MessageAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<ChatMessage>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String mechina = intent.getStringExtra("MECHINA_NAME");
        String branch = intent.getStringExtra("BRANCH_NAME");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        chatRef = database.getReference("chats").child(mechina).child(branch);

        initViews();
        listenForMessages();
    }

    private void initViews() {
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        rvChat = findViewById(R.id.rvChat);
        btnBack = findViewById(R.id.btnBack);

        adapter = new MessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(backIntent);
                finish();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        final String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String uid = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("name");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = "משתמש";
                if (snapshot.exists()) {
                    fullName = snapshot.getValue(String.class);
                }

                Map<String, Object> msgMap = new HashMap<String, Object>();
                msgMap.put("text", text);
                msgMap.put("senderName", fullName);
                msgMap.put("timestamp", ServerValue.TIMESTAMP);

                chatRef.push().setValue(msgMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        etMessage.setText("");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "שגיאה בשליפת שם המשתמש", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();

                java.util.Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    DataSnapshot ds = iterator.next();
                    ChatMessage chatMessage = ds.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        messageList.add(chatMessage);
                    }
                }

                adapter.notifyDataSetChanged();

                if (messageList.size() > 0) {
                    rvChat.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}