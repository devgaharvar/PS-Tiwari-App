package com.pstiwari.android.call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import com.pstiwari.android.LogCall;
import com.pstiwari.android.R;
import com.pstiwari.android.SinchService;
import com.pstiwari.android.call.video.CallScreenActivity;
import com.pstiwari.android.common.Utill;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class IncomingCallScreenActivity extends BaseActivity {
    private String mCallId;
    private AudioPlayer mAudioPlayer;
    private FirebaseAuth mAuth;
    private int mCallDurationSecond = 0;
    private DatabaseReference calllogRef;
    String dateStamp ;
    String revciverid;
    private String currentUserID = "";
    String reciverimage = "";
    String namereciver;
    String callstatus = "";
    String phone;
    private boolean  isVideo;
    TextView callingType;
    private DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);
        Button answerButton = findViewById(R.id.answerButton);
        Button declineButton = findViewById(R.id.declineButton);
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerClicked();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        calllogRef = FirebaseDatabase.getInstance().getReference().child("callLog");

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineClicked();
            }
        });


        callingType = findViewById(R.id.callState);

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);


    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onServiceConnected() {
        final Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            final TextView remoteUser = (TextView) findViewById(R.id.remoteUser);
            FirebaseDatabase.getInstance().getReference().child("Users").child(call.getRemoteUserId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        remoteUser.setText(snapshot.child("name").getValue().toString());
                        revciverid=snapshot.child("uid").getValue().toString();
                        namereciver = snapshot.child("name").getValue().toString();
                        phone=snapshot.child("phone").getValue().toString();
                        try {
                            reciverimage = snapshot.child("image").getValue().toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("ddddddddddddddddddddddddddddddddddd"+e);
                        }


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            callingType.setText(getString(call.getDetails().isVideoOffered() ? R.string.video_calling_in : R.string.voice_calling_in));
            if (call.getDetails().isVideoOffered())
            {
                isVideo = true;
            }
            else
            {
                isVideo = false;
            }

        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void answerClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            try {
                call.answer();
                Intent intent = new Intent(this, CallScreenActivity.class);
                intent.putExtra(SinchService.CALL_ID, mCallId);
                intent.putExtra("callstatus","Incoming");
                intent.putExtra("phone_number", phone);
                startActivity(intent);
            } catch (MissingPermissionException e) {
                ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            finish();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now answer the call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void declineClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();

            callstatus = "Rejected";

        }
        finish();
    }

    private void saveLog(String status) {
        System.out.println("tttttttttttttttttttttttt");
        status = callstatus;
        Long currentTime = System.currentTimeMillis(); //getting current time in millis
        String ss = new SimpleDateFormat("dd MMM kk:mm", Locale.getDefault()).format(new Date(currentTime));
        dateStamp =  ss;
        /*Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
      //  String showTime = String.format("%1$tI:%1$tM:%1$tS %1$Tp" + "  /  ", cal);
        Date now = new Date();
        long timestamp = now.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String dateStr = sdf.format(timestamp);*/




        LogCall logCall=new LogCall(  dateStamp, mCallDurationSecond, callstatus, currentUserID, revciverid, namereciver, reciverimage, isVideo);
        String pushid = calllogRef.push().getKey();
        calllogRef.child( pushid).setValue(logCall).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete())
                {
                    Toast.makeText( getApplicationContext(),"call log saved",Toast.LENGTH_SHORT ).show();
                }
            }
        } );

    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
            if (callstatus.equals( "Rejected" ))
            {
                saveLog(callstatus);
            }
            else if (cause.toString().equals("CANCELED") || cause.toString().equals("DENIED"))
            {
                System.out.println("ccccccccccccccccccccccc"+cause.toString());
                if (cause.toString().equals("CANCELED")) {
                    System.out.println("9999999999999");
                    callstatus = "Missed";
                    saveLog(callstatus);
                }

            }
           /*
            else
            {
                callstatus = "Missed";
                saveLog(callstatus);
            }*/


            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

    }

}