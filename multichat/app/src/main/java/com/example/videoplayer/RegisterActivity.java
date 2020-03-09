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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class RegisterActivity extends AppCompatActivity {
    private Button registerBtn, googleBtn;
    private EditText email;
    private  EditText password;
    private TextView AlreadyAnAccount;
    private ProgressDialog loadingBar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootReference;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        intializeFields();
        firebaseAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();


        AlreadyAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLoginActivity();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 createNewAccount();
            }
        });

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

    }

    private void signInWithGoogle() {

//        firebaseAuth.signInWi
    }

    private void createNewAccount() {

            String Useremail = email.getText().toString();
            String UserPassword = password.getText().toString();

            if(TextUtils.isEmpty(Useremail) || TextUtils.isEmpty(UserPassword))
            {
                Toast.makeText(this, "Please Enter Correct  Email or Password", Toast.LENGTH_SHORT).show();
            }else{
                loadingBar.setTitle("Please Wait");
                loadingBar.setMessage(" We are creating your account");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();
                firebaseAuth.createUserWithEmailAndPassword(Useremail,UserPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    String deviceToken = FirebaseInstanceId.getInstance().getToken();


                                    String currentUserId = firebaseAuth.getCurrentUser().getUid();
                                    rootReference.child("USERS").child(currentUserId).setValue("null");

                                    rootReference.child("USERS").child(currentUserId).child("device_token")
                                            .setValue(deviceToken);




                                    Toast.makeText(RegisterActivity.this, "Creation Successfull", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                    sendToMainActivity();
                                }else
                                {
                                    Toast.makeText(RegisterActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }

                            }
                        });

            }

    }

    private void sendToMainActivity() {

        Intent mainActivity  =  new Intent(RegisterActivity.this, MainActivity.class);
       mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainActivity);
        finish();

    }

    private void sendToLoginActivity() {

        Intent  loginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginActivity);
    }

    private void intializeFields() {
        registerBtn = findViewById(R.id.registerBtn);
        email  = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        AlreadyAnAccount =findViewById(R.id.logintext);
        loadingBar  = new ProgressDialog(this);
        googleBtn = findViewById(R.id.googleBtn);





    }
}
