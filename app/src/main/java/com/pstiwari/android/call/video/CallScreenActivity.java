package com.pstiwari.android.call.video;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.pstiwari.android.LogCall;
import com.pstiwari.android.R;
import com.pstiwari.android.SinchService;

import com.pstiwari.android.call.AudioPlayer;
import com.pstiwari.android.call.BaseActivity;
import com.pstiwari.android.common.Utill;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.pstiwari.android.R.drawable.ic_volume_off_white_24dp;
import static com.pstiwari.android.R.drawable.ic_volume_on_white_24dp;

public class CallScreenActivity extends BaseActivity implements SinchService.StartFailedListener {

    static final String TAG = CallScreenActivity.class.getSimpleName();
    static final String ADDED_LISTENER = "addedListener";
    static final String VIEWS_TOGGLED = "viewsToggled";
    private boolean mAcceptVideo = true;
    private AudioPlayer mAudioPlayer;
    private int mCallDurationSecond = 0;
    private Timer mTimer;
    String stat;
    private String phone;
    private String user="ali";
    String dateStamp ;
    String revciverid;
    String reciverimage="";
    TextView callringstatus;
    String namereciver;

    private UpdateCallDurationTask mDurationTask;
    private String currentUserID = "";
    private FirebaseUser currentUser;
    String receiverID = "";
    String which = "";
    String callstatus = "";
    String userName = "";
    private DatabaseReference calllogRef;
    private FirebaseAuth mAuth;
    private String mCallId;
    private boolean mAddedListener = false;
    private boolean mLocalVideoViewAdded = false;
    private boolean mRemoteVideoViewAdded = false;

    private static String EXTRA_DATA_IN_OR_OUT = "extradatainorout";





    private TextView mCallDuration;
    String inOrOut;
    private TextView mCallState;
    private TextView mCallerName;
    boolean mToggleVideoViewPositions = false;

    //bb
    private ImageView userImage1, userImage2, logo, switchVideo, switchMic, switchVolume, hangupButton;
    private boolean  isVideo, isMute, isSpeaker, alphaInvisible;


    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
        savedInstanceState.putBoolean(VIEWS_TOGGLED, mToggleVideoViewPositions);
    }

    @Override
    protected void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }

        updateUI();
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
        mToggleVideoViewPositions = savedInstanceState.getBoolean(VIEWS_TOGGLED);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callscreen);
//        receiverID = getIntent().getStringExtra("callerID");
//        which = getIntent().getStringExtra("which");
//        userName = getIntent().getStringExtra("receiverName");
        Intent intent = getIntent();
        callringstatus = findViewById( R.id.callringstatus );
        inOrOut = intent.getStringExtra(EXTRA_DATA_IN_OR_OUT);
        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        calllogRef = FirebaseDatabase.getInstance().getReference().child("callLog");
        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = findViewById(R.id.callDuration);
        mCallerName = findViewById(R.id.remoteUser);
        mCallState = findViewById(R.id.callState);
        phone =getIntent().getExtras().get("phone_number").toString();
        callstatus = getIntent().getStringExtra("callstatus");
        //bb
        switchVolume = findViewById(R.id.switchVolume);



        switchVolume.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isSpeaker = !isSpeaker;
                enableSpeaker(isSpeaker);
            }
        });


        Button endCallButton = findViewById(R.id.hangupButton);

        endCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Utill.isfinish = true;
                endCall();

            }
        });
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
    }


    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallState.setText(call.getState().toString());
            System.out.println("gggggggggggg"+call.getRemoteUserId());
            FirebaseDatabase.getInstance().getReference().child("Users").child(call.getRemoteUserId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (contactExists(CallScreenActivity.this,phone)){
                            String name=snapshot.child("name").getValue().toString();
                            mCallerName.setText(name);
                            System.out.println("exitttttt1111"+name);
                        }else {
                            mCallerName.setText(phone);
                            System.out.println("exitttttt111"+phone);

                        }

                        revciverid=snapshot.child("uid").getValue().toString();
                        namereciver = snapshot.child("name").getValue().toString();
                        try {
                            reciverimage = snapshot.child("image").getValue().toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("ddddddddddddddddddddddddddddddddddd"+e);
                        }
                        System.out.println("nnnnnnnnnnnn"+namereciver);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            if (call.getDetails().isVideoOffered()) {
                isVideo=true;
                if (call.getState() == CallState.ESTABLISHED) {
                    setVideoViewsVisibility(true, true);
                } else {
                    setVideoViewsVisibility(true, false);
                }
            }
        } else {
            setVideoViewsVisibility(false, false);
        }
    }
    public static boolean contactExists(Activity _activity, String number) {
        if (_activity == null) {
            return false;
        }
        try {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
            Cursor cur = _activity.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
            try {
                if (cur.moveToFirst()) {
                    return true;
                }
            } finally {
                if (cur != null)
                    cur.close();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mDurationTask.cancel();
        mTimer.cancel();
        if (which.equals("video"))
            removeVideoViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        updateUI();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void endCall() {
        Utill.isfinish = true;
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();

        }

        finish();
    }

    private void saveLog() {
        /*Long currentTime = System.currentTimeMillis(); //getting current time in millis
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        String showTime = String.format("%1$tI:%1$tM:%1$tS %1$Tp" + "  /  ", cal);
        Date now = new Date();
        long timestamp = now.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String dateStr = sdf.format(timestamp);
       dateStamp = showTime + dateStr;*/

        Long currentTime = System.currentTimeMillis(); //getting current time in millis
        String ss = new SimpleDateFormat("dd MMM kk:mm", Locale.getDefault()).format(new Date(currentTime));
        dateStamp =  ss;


        LogCall logCall=new LogCall(  dateStamp, mCallDurationSecond, callstatus, currentUserID, revciverid, namereciver, reciverimage, isVideo);
        String pushid=calllogRef.push().getKey();
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

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            stat = call.getState().toString();
            mCallDurationSecond = call.getDetails().getDuration();
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    private ViewGroup getVideoView(boolean localView) {
        if (mToggleVideoViewPositions) {
            localView = !localView;
        }
        return (ViewGroup) (localView ? findViewById(R.id.localVideo) : findViewById(R.id.remoteVideo));
    }

    private void addLocalView() {
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewGroup localView = getVideoView(true);
                    localView.addView(vc.getLocalView());
                    localView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            vc.toggleCaptureDevicePosition();
                        }
                    });
                    mLocalVideoViewAdded = true;
                    vc.setLocalVideoZOrder(!mToggleVideoViewPositions);
                }
            });
        }
    }
    private void addRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewGroup remoteView = getVideoView(false);
                    remoteView.addView(vc.getRemoteView());
                    remoteView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeVideoViews();
                            mToggleVideoViewPositions = !mToggleVideoViewPositions;
                            addRemoteView();
                            addLocalView();
                        }
                    });
                    mRemoteVideoViewAdded = true;
                    vc.setLocalVideoZOrder(!mToggleVideoViewPositions);
                }
            });
        }
    }


    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup)(vc.getRemoteView().getParent())).removeView(vc.getRemoteView());
                    ((ViewGroup)(vc.getLocalView().getParent())).removeView(vc.getLocalView());
                    mLocalVideoViewAdded = false;
                    mRemoteVideoViewAdded = false;
                }
            });
        }
    }

    private void setVideoViewsVisibility(final boolean localVideoVisibile, final boolean remoteVideoVisible) {
        if (getSinchServiceInterface() == null)
            return;
        if (mRemoteVideoViewAdded == false) {
            addRemoteView();
        }
        if (mLocalVideoViewAdded == false) {
            addLocalView();
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vc.getLocalView().setVisibility(localVideoVisibile ? View.VISIBLE : View.GONE);
                    vc.getRemoteView().setVisibility(remoteVideoVisible ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
//            Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            saveLog();
            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            callringstatus.setVisibility( View.GONE );
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            isSpeaker = call.getDetails().isVideoOffered();
            enableSpeaker(isSpeaker);
            if (call.getDetails().isVideoOffered()) {
                setVideoViewsVisibility(true, true);
            }
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            if (callringstatus.getVisibility() == View.VISIBLE)
            {
                callringstatus.setText( "Ringing" );
                mAudioPlayer.playProgressTone();
            }
            else {
                callringstatus.setVisibility( View.VISIBLE );
                callringstatus.setText( "Ringing" );
                mAudioPlayer.playProgressTone();
            }

        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

        @Override
        public void onVideoTrackAdded(Call call) {
            mAcceptVideo = true;
        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }
    }



    private void enableSpeaker(boolean enable) {
        //setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        AudioController audioController = getSinchServiceInterface().getAudioController();
        if (enable) {
            audioController.enableSpeaker();
            switchVolume.setImageResource( ic_volume_on_white_24dp);
        }
        else {
            audioController.disableSpeaker();
            switchVolume.setImageResource( ic_volume_off_white_24dp);
        }
        //switchVolume.setImageDrawable(ContextCompat.getDrawable(this, isSpeaker ? R.drawable.ic_volume_on_white_24dp : ic_volume_off_white_24dp));
    }

    public static String getDateTime(Long milliseconds) {
        return new SimpleDateFormat("dd MMM kk:mm", Locale.getDefault()).format(new Date(milliseconds));
    }
}