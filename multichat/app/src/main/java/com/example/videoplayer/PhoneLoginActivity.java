package com.example.videoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendCode, submitCode;

    private EditText phoneInput, verificationCodeInput;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;

    private String mVerificationId;
  private  PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);


        sendCode = findViewById(R.id.send_code_btn);
        submitCode =findViewById(R.id.submit_code_btn);
        phoneInput =findViewById(R.id.phone_number_input);
        verificationCodeInput = findViewById(R.id.code_input);

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingBar.setTitle("Phone Verification");
                loadingBar.setMessage("Please wait, Code is sending");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                String phoneNumber = phoneInput.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Phone number required", Toast.LENGTH_SHORT).show();
                }else{

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            mCallBack

                    );

                }

            }
        });

        submitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCode.setVisibility(View.INVISIBLE);
                phoneInput.setVisibility(View.INVISIBLE);

                String verificationCode = verificationCodeInput.getText().toString();

                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please Write Code", Toast.LENGTH_SHORT).show();

                }else{

                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait, verificyoing is sending");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Code Error: ", Toast.LENGTH_SHORT).show();
                phoneInput.setVisibility(View.VISIBLE);
                sendCode.setVisibility(View.VISIBLE);

            }

            public void onCodeSent( String verificationId,
                                    PhoneAuthProvider.ForceResendingToken token) {


                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();


                Toast.makeText(PhoneLoginActivity.this, "Code Send Successfully", Toast.LENGTH_SHORT).show();
                verificationCodeInput.setVisibility(View.VISIBLE);
                submitCode.setVisibility(View.VISIBLE);
                phoneInput.setVisibility(View.INVISIBLE);
                sendCode.setVisibility(View.INVISIBLE);

            }

        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    public void onComplete( Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
                            sendToMainActivity();
                        } else {

                            Toast.makeText(PhoneLoginActivity.this, "Error Unknown: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void sendCodeFun() {





    }


    private void sendToMainActivity() {

        Intent mainActivity = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }

}
