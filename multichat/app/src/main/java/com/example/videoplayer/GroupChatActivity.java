package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton sendBtn;
    private EditText messageInput;
    private ScrollView scrollView;
    private TextView displayTextMessage;
    private  String ReterivegroupName, currentUserId, currentUserName;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, groupNameRef, GroupMessageKeyRef;
    private  String currentMessageDate;
    private  String currentMessageTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mToolbar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(mToolbar);
        ReterivegroupName = getIntent().getExtras().get("GroupName").toString();
        Toast.makeText(this, "Group Chat Activity Name: "+ ReterivegroupName, Toast.LENGTH_SHORT).show();
        getSupportActionBar().setTitle(""+ReterivegroupName);


        mAuth  =FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("USERS");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(ReterivegroupName);


        Initialize();

        getUserInfo();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageToGroup();
                messageInput.setText("");

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    private void SendMessageToGroup() {

        String messageKey = groupNameRef.push().getKey();

        String messageToSend = messageInput.getText().toString();

        if(TextUtils.isEmpty(messageToSend)){
            Toast.makeText(this, "Message cannt be empty", Toast.LENGTH_SHORT).show();
        }
        else{

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,YYYY");
            currentMessageDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentMessageTime= currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();

            groupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoKey = new HashMap<>();
            messageInfoKey.put("Name", currentUserName);
            messageInfoKey.put("Message", messageToSend);
            messageInfoKey.put("Date", currentMessageDate);
            messageInfoKey.put("Time", currentMessageTime);

            GroupMessageKeyRef.updateChildren(messageInfoKey);


        }

    }


    private void Initialize() {


        sendBtn = findViewById(R.id.send_message_btn);
        messageInput = findViewById(R.id.input_groupMessage);
        scrollView  =findViewById(R.id.messageScroll);
        displayTextMessage = findViewById(R.id.textMessageTextView);

    }


    private void getUserInfo() {
        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("Name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
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

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append(chatName + " :\n" + chatMessage + " \n" + chatDate + "  " + chatTime+ "\n\n");

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);


        }

    }
}
