package com.pstiwari.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;
import com.pstiwari.android.calls.Global;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText phoneText;
    private TextView phoneText2;
    private String phoneVerificationID;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    String number;

    private ProgressDialog lodingBar;

    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputVerificationCode;

    CountryCodePicker ccp;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    private String mVerificationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);
        VerifyButton = (Button) findViewById(R.id.verify_button);

        phoneText = (EditText) findViewById(R.id.phoneText);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);

        phoneText2 = (TextView) findViewById(R.id.textView);

        lodingBar = new ProgressDialog(this);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = phoneText.getText().toString();

                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter Phone Number...", Toast.LENGTH_LONG).show();
                }
                else
                {
                    lodingBar.setTitle("Phone Verification");
                    lodingBar.setMessage("Please wait, your number is being checked...");
                    lodingBar.setCanceledOnTouchOutside(false);
                    lodingBar.show();

                    number = ccp.getFullNumberWithPlus();


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            number,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,             // Activity (for callback binding)
                            verificationCallbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });


        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneText.setVisibility(View.INVISIBLE);
                phoneText2.setVisibility(View.INVISIBLE);
                ccp.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter OTP...", Toast.LENGTH_LONG).show();
                }
                else
                {
                    lodingBar.setTitle("OTP Verification");
                    lodingBar.setMessage("Please wait, your OTP is being verified...");
                    lodingBar.setCanceledOnTouchOutside(false);
                    lodingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });


        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                lodingBar.dismiss();
                System.out.println("ddd"+e.getMessage());
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number...", Toast.LENGTH_LONG).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                phoneText.setVisibility(View.VISIBLE);
                phoneText2.setVisibility(View.VISIBLE);
                ccp.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {


                mVerificationId = verificationId;
                resendToken = token;

                lodingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "OTP sent...", Toast.LENGTH_LONG).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneText.setVisibility(View.INVISIBLE);
                phoneText2.setVisibility(View.INVISIBLE);
                ccp.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

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
                                                sendUserToMainActivity();
                                                Toast.makeText(PhoneLoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                                                lodingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : "+message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }



    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
