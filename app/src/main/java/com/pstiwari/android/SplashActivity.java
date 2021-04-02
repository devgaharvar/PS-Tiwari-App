package com.pstiwari.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pstiwari.android.call.BaseActivity;
import com.pstiwari.android.common.Utill;

public class SplashActivity extends BaseActivity {
    private FirebaseUser currentUser;
    private String currentUserID;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goTolandingPage();
            }
        }, 1500);

    }

    private void goTolandingPage() {
        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            mAuth = FirebaseAuth.getInstance();
            try {
                currentUserID = mAuth.getCurrentUser().getUid();
                Utill.uid= currentUserID;
                if (!getSinchServiceInterface().isStarted()) {
                    getSinchServiceInterface().startClient(currentUserID);
                }
            } catch (NullPointerException ignored) {
            }
            Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mIntent);
            SplashActivity.this.finish();
        }
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }
}