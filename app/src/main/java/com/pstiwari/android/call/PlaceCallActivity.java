package com.pstiwari.android.call;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.pstiwari.android.CallActivity;
import com.pstiwari.android.R;
import com.pstiwari.android.SinchService;
import com.pstiwari.android.call.video.CallScreenActivity;
import com.pstiwari.android.common.Utill;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

public class PlaceCallActivity extends BaseActivity implements SinchService.StartFailedListener {
    private TextView mCallButton;
    private String currentUserID = "";
    private FirebaseUser currentUser;
    String receiverID = "";
    String which = "";
    private String phone;
    private com.sinch.android.rtc.calling.Call call = null;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_call);
        receiverID = getIntent().getStringExtra("callerID");
        which = getIntent().getStringExtra("which");
        phone=getIntent().getExtras().get("phone_number").toString();
//        mCallButton = (TextView) findViewById(R.id.callButton);
//        mCallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                callButtonClicked();
//            }
//        });
//        mCallButton.setOnClickListener(buttonClickListener);
        mAuth = FirebaseAuth.getInstance();
        receiverID = getIntent().getStringExtra("callerID");
        which = getIntent().getStringExtra("which");

        try {
            currentUserID = mAuth.getCurrentUser().getUid();
        }
        catch (NullPointerException ignored) {
        }



    }

    @Override
    protected void onServiceConnected() {
        callButtonClicked();
        getSinchServiceInterface().setStartListener(this);

    }

    @Override
    public void onStarted() {

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Utill.isfinish){
            Utill.isfinish = false;
            finish();
        }
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    private void stopButtonClicked() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        finish();
    }

    private void callButtonClicked() {
        String userName = receiverID;
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a user to call", Toast.LENGTH_LONG).show();
            return;
        }

        try {

            if (which.equals("video")){
                call = getSinchServiceInterface().callUserVideo(userName);
            }else {
                call = getSinchServiceInterface().callUser(userName);
            }
            if (call == null) {
                // Service failed for some reason, show a Toast and abort
                Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before "
                        + "placing a call.", Toast.LENGTH_LONG).show();
                return;
            }
            String callId = call.getCallId();
            Intent callScreen = new Intent(this, CallScreenActivity.class);
            callScreen.putExtra("callstatus","Outgoing");
            callScreen.putExtra(SinchService.CALL_ID, callId);
            callScreen.putExtra("phone_number",phone);
            startActivity(callScreen);
        } catch (MissingPermissionException e) {
            ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
        }

    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now place a call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast
                    .LENGTH_LONG).show();
        }
    }

//    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.callButton:
//
//                    break;
//
//            }
//        }
//    };
}