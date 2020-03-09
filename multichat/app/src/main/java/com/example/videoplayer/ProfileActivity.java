package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String  receivedUserId;
    private CircleImageView VisitorprofileImage;
    private TextView visitorName, visitorStatus;
    private Button visitorSendMessage, visitorCancelButton;

    private DatabaseReference UserRef, chatRequestRef, contactRef;
    private String current_state ,sender_userId;
    private FirebaseAuth mAuth;

    private DatabaseReference NotificationRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        current_state ="new";
        mAuth = FirebaseAuth.getInstance();
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactRef     = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");



        receivedUserId = getIntent().getExtras().get("UserID").toString();
        UserRef = FirebaseDatabase.getInstance().getReference().child("USERS").child(receivedUserId);
        sender_userId = mAuth.getCurrentUser().getUid();



        VisitorprofileImage = findViewById(R.id.visit_profile_img);

        visitorName = findViewById(R.id.visit_name);
        visitorStatus = findViewById(R.id.visit_status);
        visitorSendMessage = findViewById(R.id.visit_sendMessage);
        visitorCancelButton = findViewById(R.id.visit_decline);


        reteriveUserInfo();



        Toast.makeText(this, "Received Id: "+receivedUserId, Toast.LENGTH_SHORT).show();
    }

    private void reteriveUserInfo() {
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && (dataSnapshot.hasChild("Image")))
                {
                    String visitor_image = dataSnapshot.child("Image").getValue().toString();
                    String visitor_name = dataSnapshot.child("Name").getValue().toString();
                    String visitor_status = dataSnapshot.child("Status").getValue().toString();

                    Picasso.get().load(visitor_image).placeholder(R.drawable.index).into(VisitorprofileImage);
                    visitorName.setText(visitor_name);
                    visitorStatus.setText(visitor_status);

                    ManageChatRequest();


                }else{

                    String visitor_name = dataSnapshot.child("Name").getValue().toString();
                    String visitor_status = dataSnapshot.child("Status").getValue().toString();
                    //Picasso.get().load(visitor_image).placeholder(R.drawable.index).into(VisitorprofileImage);
                    visitorName.setText(visitor_name);
                    visitorStatus.setText(visitor_status);
                    ManageChatRequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        chatRequestRef.child(sender_userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(receivedUserId))
                {
                    String requesttype = dataSnapshot.child(receivedUserId).child("request_type").getValue().toString();

                    if(requesttype.equals("sent"))
                    {
                        current_state="request_sent";
                        visitorSendMessage.setText("Cancel  Request");

                    }else if(requesttype.equals("received"))
                    {
                       current_state ="request_received";
                       visitorSendMessage.setText("Accept Request");
                       visitorCancelButton.setVisibility(View.VISIBLE);
                       visitorCancelButton.setEnabled(true);
                       visitorCancelButton.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               cancelChatRequest();
                           }
                       });


                    }else
                    {

                    }
                } else
                {
                    contactRef.child(sender_userId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receivedUserId))
                                    {
                                     current_state ="friends";
                                     visitorSendMessage.setText("Remove this Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!sender_userId.equals(receivedUserId))
        {
            visitorSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    visitorSendMessage.setEnabled(false);
                    if(current_state.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(current_state.equals("request_sent"))
                    {
                        cancelChatRequest();

                    }
                    if(current_state.equals("request_received"))
                    {
                        acceptChatRequest();

                    }
                    if(current_state.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }
                }
            });
        }else
        {
            visitorSendMessage.setVisibility(View.INVISIBLE);
        }




    }

    private void RemoveSpecificContact() {

        contactRef.child(sender_userId).child(receivedUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {

                            contactRef.child(receivedUserId).child(sender_userId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                visitorSendMessage.setEnabled(true);
                                                current_state="new";
                                                visitorSendMessage.setText("Send Request");


                                                visitorCancelButton.setVisibility(View.INVISIBLE);
                                                visitorCancelButton.setEnabled(false);
                                            }
                                        }
                                    });

                        }else
                        {

                        }
                    }
                });


    }

    private void acceptChatRequest() {

        contactRef.child(sender_userId).child(receivedUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactRef.child(receivedUserId).child(sender_userId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {

                                                chatRequestRef.child(sender_userId).child(receivedUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(sender_userId).child(receivedUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                            visitorSendMessage.setEnabled(true);
                                                                                            current_state="friends";
                                                                                            visitorSendMessage.setText("Remove Contacts");
                                                                                            visitorCancelButton.setEnabled(false);
                                                                                            visitorCancelButton.setVisibility(View.INVISIBLE);

                                                                                    }else
                                                                                    {

                                                                                    }
                                                                                }
                                                                            });
                                                                }else
                                                                {

                                                                }
                                                            }
                                                        });

                                            }else
                                            {

                                            }
                                        }
                                    });
                        }else
                        {

                        }
                    }
                });

    }

    private void cancelChatRequest() {

        chatRequestRef.child(sender_userId).child(receivedUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                         if(task.isSuccessful())
                         {

                             chatRequestRef.child(receivedUserId).child(sender_userId)
                                     .removeValue()
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    visitorSendMessage.setEnabled(true);
                                                    current_state="new";
                                                    visitorSendMessage.setText("Send Request");


                                                    visitorCancelButton.setVisibility(View.INVISIBLE);
                                                    visitorCancelButton.setEnabled(false);
                                                }
                                         }
                                     });

                         }else
                         {

                         }
                    }
                });


    }

    private void sendChatRequest() {
        chatRequestRef.child(sender_userId).child(receivedUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                     public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                chatRequestRef.child(receivedUserId).child(sender_userId)
                                        .child("request_type").setValue("received")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    HashMap<String, Object> ChatNotificationMap = new HashMap<>();
                                                    ChatNotificationMap.put("from", sender_userId);
                                                    ChatNotificationMap.put("type", "request");

                                                    NotificationRef.child(receivedUserId).push()
                                                            .setValue(ChatNotificationMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful())
                                                                    {

                                                                        visitorSendMessage.setEnabled(true);
                                                                        current_state="request_sent";
                                                                        visitorSendMessage.setText("Cancel Request");
                                                                    }else
                                                                    {

                                                                    }
                                                                }
                                                            });



                                                }
                                            }
                                        });
                            }else
                            {

                            }
                    }
                });
    }
}
