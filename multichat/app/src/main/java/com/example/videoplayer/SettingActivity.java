package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private EditText personUsername, personName,personStatus;
    private Button updateBtn;
    private CircleImageView profileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private  static  final  int gallaryPick = 1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;

    private Toolbar toolbar;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Accounts Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        initialize();
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference().child("USERS");
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpData();
            }
        });
        currentUserId = mAuth.getCurrentUser().getUid();

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


       ReteriveUserInfo();

       profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallaryIntent = new Intent();
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, gallaryPick);
            }
        });


    }

    private void ReteriveUserInfo() {

        rootRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if((dataSnapshot.exists()) && dataSnapshot.hasChild("userName") && dataSnapshot.hasChild("Status") ){

                String RuserName = dataSnapshot.child("UserName").getValue().toString();
                String Rstatus = dataSnapshot.child("Status").getValue().toString();
                String  Rname = dataSnapshot.child("Name").getValue().toString();

                personUsername.setText(RuserName);
                personStatus.setText(Rstatus);
                personName.setText(Rname);

            }else if(dataSnapshot.hasChild("Image")){
                String RName = dataSnapshot.child("Name").getValue().toString();
                String RuserName = dataSnapshot.child("UserName").getValue().toString();
                String Rstatus = dataSnapshot.child("Status").getValue().toString();
                String RprofileImage = dataSnapshot.child("Image").getValue().toString();


                personUsername.setText(RuserName);
                personStatus.setText(Rstatus);
                personName.setText(RName);
                if(!RprofileImage.equals(""))
                {
                    Picasso.get().load(RprofileImage).into(profileImage);
                }else
                {
                    Toast.makeText(SettingActivity.this, "Image Not Steup", Toast.LENGTH_SHORT).show();
                }



            }else{
                Toast.makeText(SettingActivity.this, "Update your profile Informations", Toast.LENGTH_SHORT).show();
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpData() {
        currentUserId = mAuth.getCurrentUser().getUid();
        String name = personName.getText().toString();
        String userName = personUsername.getText().toString();
        String status = personStatus.getText().toString();

        if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Username & Status cann't be null ", Toast.LENGTH_SHORT).show();
        }else{

            HashMap<String, Object>profileMap = new HashMap<>();
            profileMap.put("UID", currentUserId);
            profileMap.put("Name", name);
            profileMap.put("UserName", userName);
            profileMap.put("Status", status);


            rootRef.child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SettingActivity.this, "Profile Setup Successfully", Toast.LENGTH_SHORT).show();
                                sendToMainActivity();
                            }else
                            {
                                Toast.makeText(SettingActivity.this, "Error in Connection " +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void sendToMainActivity() {

        Intent mainActivity  =  new Intent(SettingActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);

    }


    private void initialize() {
        profileImage = findViewById(R.id.profileImage);
        personName = findViewById(R.id.personName);
        personUsername = findViewById(R.id.personUsername);
        personStatus = findViewById(R.id.personStatus);
        updateBtn = findViewById(R.id.settingUpdateBtn);
        loadingBar = new ProgressDialog(this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == gallaryPick && resultCode == RESULT_OK && data!=null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                Toast.makeText(this, "Enter ", Toast.LENGTH_SHORT).show();

                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if(resultCode == RESULT_OK)
                {
                    loadingBar.setTitle("Updating ProfileImage");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    Uri resultUri = result.getUri();
                    StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SettingActivity.this, "Profile Image Uploaded successfully", Toast.LENGTH_SHORT).show();

                                Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();
                                while (!urlTask.isSuccessful()) ;
                                Uri downloadUrl = urlTask.getResult();

                              //  final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                                Toast.makeText(SettingActivity.this, "Download :"+downloadUrl, Toast.LENGTH_SHORT).show();

                                rootRef.child(currentUserId).child("Image")
                                        .setValue(downloadUrl.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(SettingActivity.this, "Image saved Successfully", Toast.LENGTH_SHORT).show();

                                                }else
                                                {
                                                    Toast.makeText(SettingActivity.this, "Error in task: " +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }else{
                                Toast.makeText(SettingActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

                    loadingBar.dismiss();

                }


            }
        }

}
