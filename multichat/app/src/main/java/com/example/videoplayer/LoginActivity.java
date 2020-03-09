package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn, PhoneLoginBtn;
    private EditText email;
    private  EditText password;
    private TextView NeedNewAccount;
   private  TextView forgetPassword;
   private  EditText phoneNumber;
    private ProgressDialog loadingBar;

    private DatabaseReference UsersRef;

   private FirebaseAuth mAuth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeFields();
        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("USERS");



        NeedNewAccount.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
               startActivity(registerActivity);
           }
       });

       loginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AllowUserToLogin();
           }
       });

       PhoneLoginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent  phoneActivity = new Intent(LoginActivity.this, PhoneLoginActivity.class);
               startActivity(phoneActivity);

           }
       });

    }

    private void AllowUserToLogin() {

        String Useremail = email.getText().toString();
        String UserPassword = password.getText().toString();

        if(TextUtils.isEmpty(Useremail) || TextUtils.isEmpty(UserPassword))
        {
            Toast.makeText(this, "Email or Password cant be empty", Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.setTitle("Please Wait");
            loadingBar.setMessage(" We are checking the credentials");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(Useremail, UserPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    String currentUserId = mAuth.getCurrentUser().getUid();
                                    String deviceToken = FirebaseInstanceId.getInstance().getToken();


                                    UsersRef.child(currentUserId).child("device_token")
                                            .setValue(deviceToken)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        sendToMainActivity();
                                                        Toast.makeText(LoginActivity.this, "Logged in Successful...", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });



                                }else
                                {
                                    Toast.makeText(LoginActivity.this, "Error: Incorrect Password or Email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        }
                    });
        }

    }

    private void initializeFields() {
        loginBtn = findViewById(R.id.loginBtn);
        PhoneLoginBtn = findViewById(R.id.phoneLoginBtn);
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        NeedNewAccount = findViewById(R.id.signUptext);
        forgetPassword  =findViewById(R.id.forgetPassword);
        phoneNumber  = findViewById(R.id.phoneNumber);
        loadingBar  = new ProgressDialog(this);



    }



    private void sendToMainActivity() {

        Intent mainActivity  =  new Intent(LoginActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainActivity);
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "Main Activity", Toast.LENGTH_SHORT).show();
       FirebaseUser currentUser = mAuth.getCurrentUser();
       if(currentUser!=null)
       {
           sendToMainActivity();
       }
    }
}

