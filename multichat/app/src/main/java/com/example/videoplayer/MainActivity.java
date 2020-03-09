package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private  ViewPager viewPager;
    private TabLayout tabLayout;
    private  TabsAccessorAdapter tabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rootRef = FirebaseDatabase.getInstance().getReference();

       // Toast.makeText(this, "Current User :"+ currentUser.getUid(), Toast.LENGTH_SHORT).show();

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Multi-Chat");

        viewPager = findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);

        tabLayout = findViewById(R.id.main_page_tabLayout);
        tabLayout.setupWithViewPager(viewPager);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "Sending to Login", Toast.LENGTH_SHORT).show();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        String currentUser = firebaseAuth.getCurrentUser().getUid();
        if (currentUser == null)
        {
           sendUserToLogin();
        }else{
            updateUserStatus("online");
            ValidationChecking();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser   currentUser = firebaseAuth.getCurrentUser();
        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser   currentUser = firebaseAuth.getCurrentUser();
        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }

    }

    private void ValidationChecking() {
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        rootRef.child("USERS").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("UserName").exists()){
                    Toast.makeText(MainActivity.this, "Welcome to Chat", Toast.LENGTH_SHORT).show();

                }else {

                    sendToSettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendUserToLogin() {

        Intent loginActivity = new Intent(MainActivity.this,LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


         super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId() == R.id.findFriends)
         {
            sendtoFriendActivity();
         }
        if(item.getItemId() == R.id.createGroup)
        {
            RequestNewGroup();
        }
         if(item.getItemId() == R.id.settings)
         {
            sendToSettings();
         }
          if(item.getItemId() == R.id.logOut)
          {
              updateUserStatus("offline");

              firebaseAuth.signOut();
              sendUserToLogin();
          }


         return true;
    }

    private void sendtoFriendActivity() {
        Intent friendActivity= new Intent(MainActivity.this, FindFriendActivity.class);
        startActivity(friendActivity);

    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.alertDialog);
        builder.setTitle("Enter Group Name");

        final EditText groupName  =  new EditText(MainActivity.this);
        groupName.setHint("E.g Group Title");
        builder.setView(groupName);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupNameField =  groupName.getText().toString();
                if(TextUtils.isEmpty(groupNameField))
                {
                    Toast.makeText(MainActivity.this, "Please Enter Group Name", Toast.LENGTH_SHORT).show();
                }else{
                         createNewGroup(groupNameField);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               dialog.cancel();

            }
        });
        builder.show();


    }

    private void createNewGroup(String groupNameField) {
        rootRef.child("Groups").child(groupNameField).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(MainActivity.this, "Group Created", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Error : "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    }
                });

    }

    private void sendToSettings() {

        Intent settingActivity = new Intent(MainActivity.this, SettingActivity.class);

        startActivity(settingActivity);


    }


    private void updateUserStatus(String state)
    {
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        rootRef.child("USERS").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }


}
