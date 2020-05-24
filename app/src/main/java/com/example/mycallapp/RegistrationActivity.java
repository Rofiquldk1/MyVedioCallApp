package com.example.mycallapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText Phonetext,Codetext;
    private Button continueAndNextbtn;
    String checker="",phoneNumber="";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResndToken;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        loadingbar = new ProgressDialog(this);

        ccp = findViewById(R.id.ccp);
        Phonetext = findViewById(R.id.phoneText);
        Codetext = findViewById(R.id.codeText);
        continueAndNextbtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);

        ccp.registerCarrierNumberEditText(Phonetext);

        continueAndNextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(continueAndNextbtn.getText().equals("Submit")||checker.equals("Code Sent")){
                       String verificaticode=Codetext.getText().toString();
                       if(verificaticode.equals("")){
                           Toast.makeText(RegistrationActivity.this,"Please write verification code",Toast.LENGTH_LONG).show();
                       }
                       else{
                           loadingbar.setTitle("Code Verification");
                           loadingbar.setMessage("Please Wait,While we are Varyfing your code");
                           loadingbar.setCanceledOnTouchOutside(false);
                           loadingbar.show();

                           PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificaticode);
                           signInWithPhoneAuthCredential(credential);
                       }
                }
                else{
                    phoneNumber=ccp.getFullNumberWithPlus();
                    if(!phoneNumber.equals("")){
                        loadingbar.setTitle("Phone Number Verification");
                        loadingbar.setMessage("Please Wait,While we are Varyfing your phone number");
                        loadingbar.setCanceledOnTouchOutside(false);
                        loadingbar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,        // Phone number to verify
                                60,                 // Timeout duration
                                TimeUnit.SECONDS,   // Unit of timeout
                                RegistrationActivity.this,               // Activity (for callback binding)
                                mCallbacks);        // OnVerificationStateChangedCallbacks
                    }
                    else{
                        Toast.makeText(RegistrationActivity.this,"Please enter valid phone number",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegistrationActivity.this,"Invalid phone number.....",Toast.LENGTH_LONG).show();
                loadingbar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);
                continueAndNextbtn.setText("Continue");
                Codetext.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                continueAndNextbtn.setText("Submit");
                Codetext.setVisibility(View.VISIBLE);

                mVerificationId=s;
                mResndToken=forceResendingToken;

                loadingbar.dismiss();
                Toast.makeText(RegistrationActivity.this,"Code has been sent,Please check...",Toast.LENGTH_LONG).show();

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            Intent homeintent = new Intent(RegistrationActivity.this,ContactsActivity.class);
            startActivity(homeintent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                           loadingbar.dismiss();
                           Toast.makeText(RegistrationActivity.this,"Congratulations,you are login successfully",Toast.LENGTH_LONG).show();
                           sendUserToMainActivity();
                        } else {
                           loadingbar.dismiss();
                           String e = task.getException().toString();
                            Toast.makeText(RegistrationActivity.this,"Error: "+e,Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void sendUserToMainActivity(){
        Intent intent = new Intent(RegistrationActivity.this,ContactsActivity.class);
        startActivity(intent);
        finish();
    }
}
