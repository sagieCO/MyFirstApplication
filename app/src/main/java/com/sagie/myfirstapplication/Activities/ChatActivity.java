package com.sagie.myfirstapplication.Activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.sagie.myfirstapplication.ChatMessage;
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
    private MessageAdapter adapter; // תצטרך ליצור את המחלקה הזו (מפורטת למטה)
    private List<ChatMessage> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String mechina = getIntent().getStringExtra("MECHINA_NAME");
        String branch = getIntent().getStringExtra("BRANCH_NAME");

        // הגדרת הנתיב: chats -> שם מכינה -> שלוחה
        chatRef = FirebaseDatabase.getInstance().getReference("chats")
                .child(mechina)
                .child(branch);

        initViews();
        listenForMessages(); // הפעלת המאזין לקבלת הודעות
    }

    private void initViews() {
        etMessage = findViewById(R.id.etMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);
        rvChat = findViewById(R.id.rvChat);

        adapter = new MessageAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // שליפת השם של המשתמש מה-Database לפי ה-UID שלו
        FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // אם השם קיים במסד, ניקח אותו. אם לא, נרשום "משתמש"
                        String fullName = snapshot.exists() ? snapshot.getValue(String.class) : "משתמש";

                        // יצירת אובייקט הודעה עם השם האמיתי וזמן שרת
                        Map<String, Object> msgMap = new HashMap<>();
                        msgMap.put("text", text);
                        msgMap.put("senderName", fullName);
                        msgMap.put("timestamp", ServerValue.TIMESTAMP); // חשוב: זמן שרת מדויק

                        chatRef.push().setValue(msgMap).addOnSuccessListener(aVoid -> {
                            etMessage.setText(""); // ניקוי התיבה
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
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatMessage chatMessage = ds.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        messageList.add(chatMessage);
                    }
                }
                adapter.notifyDataSetChanged();
                rvChat.scrollToPosition(messageList.size() - 1); // גלילה לסוף
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}