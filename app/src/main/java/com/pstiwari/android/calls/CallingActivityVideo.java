package com.pstiwari.android.calls;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pstiwari.android.ApiService;
import com.pstiwari.android.Contacts;
import com.pstiwari.android.Notification.CallNotification;
import com.pstiwari.android.Notification.Message;
import com.pstiwari.android.Notification.NotificationModel;
import com.pstiwari.android.Notification.NotifyManager;
import com.pstiwari.android.Notification.Tokens;
import com.pstiwari.android.R;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.util.HashMap;
import java.util.Map;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;



public class CallingActivityVideo extends AppCompatActivity {
    private static final String TAG = CallingActivityVideo.class.getSimpleName();
    private static final int PERMISSION_REQ_ID = 88;
    // Permission WRITE_EXTERNAL_STORAGE is not mandatory
    // for Agora RTC SDK, just in case if you wanna save
    // logs to external sdcard.
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private DatabaseReference RootRef;
    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;
    private ImageView mCallBtn;
    private ImageView mMuteBtn;
    private ImageView mSwitchCameraBtn;
    EmojiTextView nameT;
    TextView state;
    //vars
    private boolean mCallEnd, mMuted, out;
    String channelid, friendid, name, ava,history;
    FirebaseAuth mAuth;
    private RtcEngine mRtcEngine;
    String Mid, encrypM;

    DatabaseReference mlogs, mChat, mUser;


    //fcm
    ApiService fcm;

    Handler mHandler, mTimeout;

    private AudioPlayer mAudioPlayer;

    Object currTime;
    boolean first = true;

    boolean isRunning = true, isRunningTime = true, single = true;
    int time = 0, timeout = 0;


    /**
     * Event handler registered into RTC engine for RTC callbacks.
     * Note that UI operations needs to be in UI thread because RTC
     * engine deals with the events in a separate thread.
     */
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(final String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (out) {
                        mUser.child(friendid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Usercalldata usercalldata = dataSnapshot.getValue(Usercalldata.class);
                                if(!usercalldata.isIncall())
                                {
                                    mAudioPlayer.stopProgressTone();
                                    mAudioPlayer.playProgressTone();
                                    sendMessNotify();
                                    mTimeout = new Handler();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub
                                            while (isRunningTime) {
                                                try {
                                                    Thread.sleep(1000);
                                                    mTimeout.post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            timeout = timeout + 1;
                                                            if (timeout >= 61) {
                                                                isRunningTime = false;
                                                                leaveChannel();
                                                            }

                                                        }
                                                    });
                                                } catch (Exception e) {
                                                }
                                            }
                                        }
                                    }).start();
                                }
                                else
                                {
                                    mAudioPlayer.stopProgressTone();
                                    state.setText(getString(R.string.userbusy));
                                    Toast.makeText(CallingActivityVideo.this, R.string.userbusy, Toast.LENGTH_LONG).show();
                                    leaveChannel();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    mlogs.child(mAuth.getCurrentUser().getUid()).child(friendid).child(channelid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(mAuth.getCurrentUser()!=null) {

                                Usercalldata usercalldata = dataSnapshot.getValue(Usercalldata.class);

                                try {
                                    if (!usercalldata.isIncall()) {
                                        leaveChannel();
                                    }
                                } catch (NullPointerException e) {

                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            });
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, final int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });

        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isRunningTime = false;

                    if (out) {
                        mAudioPlayer.stopProgressTone();
                    }
                    if (first) {
                        first = false;
                        mHandler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                while (isRunning) {
                                    try {
                                        Thread.sleep(1000);
                                        mHandler.post(new Runnable() {

                                            @Override
                                            public void run() {
                                                time = time + 1;
                                                state.setText(timeSec(time));

                                            }
                                        });
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }).start();
                    }

                }
            });

        }


        @Override
        public void onUserOffline(final int uid, int reason) {
            isRunning = false;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    onRemoteUserLeft();
                }
            });
            leaveChannel();
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {

            isRunning = false;
            super.onLeaveChannel(stats);

        }

    };


    private void setupRemoteVideo(int uid) {
        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        int count = mRemoteContainer.getChildCount();
        View view = null;
        for (int i = 0; i < count; i++) {
            View v = mRemoteContainer.getChildAt(i);
            if (v.getTag() instanceof Integer && ((int) v.getTag()) == uid) {
                view = v;
            }
        }

        if (view != null) {
            return;
        }

        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteContainer.addView(mRemoteView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mRemoteView.setTag(uid);
    }

    private void onRemoteUserLeft() {
        removeRemoteVideo();
    }

    private void removeRemoteVideo() {
        if (mRemoteView != null) {
            mRemoteContainer.removeView(mRemoteView);
        }
        mRemoteView = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.keepSynced(true);
        RootRef.child("Users").child(friendid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("callstate")))
                        {
                            String callstate = dataSnapshot.child("callstate").getValue().toString();
                            if (callstate.equals("on"))
                            {
                                state.setText("Ringing");
                                System.out.println("daasddd");
                            }
                        }
                        else
                        {
                            Toast.makeText(CallingActivityVideo.this, "Please update your profile...", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_calling_video);
        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseDatabase.getInstance().getReference(Global.USERS);
        mlogs = FirebaseDatabase.getInstance().getReference(Global.CALLS);
        mChat = FirebaseDatabase.getInstance().getReference(Global.CHATS);
        Global.currentactivity = this;
        mAudioPlayer = new AudioPlayer(this);
        channelid = getIntent().getStringExtra("channel_id");
        name = getIntent().getStringExtra("name");
        out = getIntent().getBooleanExtra("out", false);
        friendid = getIntent().getStringExtra("UserId");
        ava = getIntent().getStringExtra("ava");
        history=getIntent().getStringExtra("history");
        Mid = mAuth.getCurrentUser().getUid() + "_" + friendid + "_" + String.valueOf(System.currentTimeMillis());


        //fcm notify
        fcm = Global.getFCMservies();
        initUI();


        if(mAuth.getCurrentUser()!= null) {
            Map<String, Object> map = new HashMap<>();
            map.put("incall", true);
            mUser.child(mAuth.getCurrentUser().getUid()).updateChildren(map);
        }
        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }



        currTime = ServerValue.TIMESTAMP;

/*

        Map<String, Object> map33 = new HashMap<>();
        map33.put("from", mAuth.getCurrentUser().getUid());
        map33.put("to", friendid);
        map33.put("time", currTime);
        map33.put("id", channelid);
        map33.put("name", name);
        map33.put("ava", ava);
        map33.put("incall", false);

        mlogs.child(mAuth.getCurrentUser().getUid()).child(friendid).child(channelid).onDisconnect().updateChildren(map33);

*/

        Map<String, Object> map = new HashMap<>();
        map.put("from", mAuth.getCurrentUser().getUid());
        map.put("to", friendid);
        map.put("time", currTime);
        map.put("id", channelid);
        map.put("name", name);
        map.put("ava", ava);
        map.put("incall", true);
        if(mAuth.getCurrentUser()!= null) {
            mlogs.child(mAuth.getCurrentUser().getUid()).child(friendid).child(channelid).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("from", mAuth.getCurrentUser().getUid());
                    map2.put("to", friendid);
                    map2.put("time", currTime);
                    map2.put("id", channelid);
                    map2.put("name", name);
                    map2.put("ava", ava);
                    map2.put("incall", true);
                    mlogs.child(friendid).child(mAuth.getCurrentUser().getUid()).child(channelid).updateChildren(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {


                        }
                    });
                }
            });
        }

    }


    private void initUI() {


        mAuth = FirebaseAuth.getInstance();

        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);

        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        nameT = findViewById(R.id.name);
        state = findViewById(R.id.state);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);


        if (history.equals("true"))
        {
            mUser.child(friendid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if ((dataSnapshot.exists()))
                    {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        nameT.setText(userName);

                        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(friendid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot sp : snapshot.getChildren()) {
                                    Contacts ct = sp.getValue(Contacts.class);
                                    assert ct != null;
                                    System.out.println("ssafgg44444444"+ct.getPhone());
                                    if (!contactExists(CallingActivityVideo.this,ct.getPhone()).equals("")){
                                        nameT.setText(contactExists(CallingActivityVideo.this,ct.getPhone()));
                                    }else {
                                        nameT.setText(ct.getPhone());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(friendid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        Contacts ct = sp.getValue(Contacts.class);
                        assert ct != null;
                        System.out.println("ssafgg44444444"+ct.getPhone());
                        if (!contactExists(CallingActivityVideo.this,ct.getPhone()).equals("")){
                            nameT.setText(contactExists(CallingActivityVideo.this,ct.getPhone()));
                        }else {
                            nameT.setText(ct.getPhone());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

        state.setText(getResources().getString(R.string.connecting));


    }


    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initEngineAndJoinChannel();
        }
    }
    public static String contactExists(Activity _activity, String number) {
        if (_activity == null) {
            return "";
        }
        try {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
            Cursor cur = _activity.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
            try {
                if (cur.moveToFirst()) {
                    String nameu = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    return nameu;

                }
            } finally {
                if (cur != null)
                    cur.close();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        joinChannel(channelid);
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);

        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();
        mRtcEngine.enableAudio();
        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void joinChannel(String channel) {
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name that
        // you use to generate this token.
        String token = getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null; // default, no token
        }
        mRtcEngine.joinChannel(token, channel, "Extra Optional Data", 0);

    }

    @Override
    protected void onDestroy() {
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.USE_DEFAULT_STREAM_TYPE );
        audioManager.setSpeakerphoneOn(false);
        super.onDestroy();
        mAudioPlayer.stopProgressTone();
        if (!mCallEnd) {
            leaveChannel();

        }
        RtcEngine.destroy();

        HashMap<String, Object> update = new HashMap<>();
        update.put("callstate", "off");

        RootRef.child("Users").child(friendid).updateChildren(update)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                           // Toast.makeText(CallingActivityVideo.this, "call atended", Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                        }
                    }
                });

    }


    private void leaveChannel() {
        isRunning = false;
        mRtcEngine.leaveChannel();
        if(mAuth.getCurrentUser()!= null) {
        if (single) {
            single = false;
            if (out) {
                mAudioPlayer.stopProgressTone();
            }


            Map<String, Object> map2 = new HashMap<>();
            map2.put("incall", false);
            mUser.child(mAuth.getCurrentUser().getUid()).updateChildren(map2);
            mlogs.child(mAuth.getCurrentUser().getUid()).child(friendid).child(channelid).updateChildren(map2);
            mlogs.child(friendid).child(mAuth.getCurrentUser().getUid()).child(channelid).updateChildren(map2);
            if (out) {
                Map<String, Object> map = new HashMap<>();
                map.put("dur", time);
                map.put("incall", false);
                mlogs.child(mAuth.getCurrentUser().getUid()).child(friendid).child(channelid).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("dur", time);
                        map2.put("incall", false);
                        mlogs.child(friendid).child(mAuth.getCurrentUser().getUid()).child(channelid).updateChildren(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (time > 0)
                                {
                                    encrypM = encryption.encryptOrNull("Video call " + timeSec(time));
                                }

                                else
                                {
                                    encrypM = encryption.encryptOrNull("Missed video call");
                                }

                                System.out.println("jkjjjjjjdddsssssssrerw");
                                sendMessNotifyMM(encrypM, Mid, friendid);

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {


                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                    }
                });
            }

            finish();

        }

    }
    }

    @Override
    protected void onPause() {
        mAudioPlayer.stopProgressTone();
        try {
            if (mRtcEngine != null)
                mRtcEngine.muteLocalVideoStream(true);

        } catch (NullPointerException e) {

        }
      //  ((AppBack) this.getApplication()).startActivityTransitionTimer();
        Global.currentactivity = null;

        super.onPause();
    }

    @Override
    protected void onResume() {
        try {
            if (mRtcEngine != null)
                mRtcEngine.muteLocalVideoStream(false);

        } catch (NullPointerException e) {

        }

        Global.currentactivity = this;
       /* DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Global.USERS);
        AppBack myApp = (AppBack) this.getApplication();
        if (myApp.wasInBackground) {
            //init data
            Map<String, Object> map = new HashMap<>();
            map.put(Global.Online, true);
            if(mAuth.getCurrentUser() != null)
                mData.child(mAuth.getCurrentUser().getUid()).updateChildren(map);
            Global.local_on = true;
            //lock screen
            ((AppBack) getApplication()).lockscreen(((AppBack) getApplication()).shared().getBoolean("lock", false));
        }

        myApp.stopActivityTransitionTimer();
*/
        super.onResume();
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        mRtcEngine.muteLocalAudioStream(mMuted);

        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);

    }

    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel(channelid);
    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();

    }

    private void removeLocalVideo() {
        if (mLocalView != null) {
            mLocalContainer.removeView(mLocalView);
        }
        mLocalView = null;
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }


    private void sendMessNotify() {
        try {


        DatabaseReference mTokenget = FirebaseDatabase.getInstance().getReference(Global.tokens);
        mTokenget.child(friendid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser()!= null) {

                    final Tokens tokens = dataSnapshot.getValue(Tokens.class);


                    FirebaseDatabase.getInstance().getReference(Global.USERS).child(friendid).child(Global.device).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                String isAndroi = dataSnapshot.getValue(String.class);
                                NotificationModel notificationModel = new NotificationModel(Global.nameLocal,Global.nameLocal +getString(R.string.iscallingV) );
                                CallNotification callNotification = new CallNotification(name,ava,friendid
                                        ,encryption.encryptOrNull(channelid),encryption.encryptOrNull(mAuth.getCurrentUser().getUid())
                                        ,encryption.encryptOrNull(name),encryption.encryptOrNull(ava)
                                        ,mAuth.getCurrentUser().getUid(),Mid);
                                NotifyManager.VideoCallNotificationToUser(tokens.getTokens(),isAndroi,callNotification);


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }catch (NullPointerException e){}
    }

    private void sendMessNotifyMM(final String message, final String Mid, final String id) {
        try {
            DatabaseReference mTokenget = FirebaseDatabase.getInstance().getReference(Global.tokens);
            mTokenget.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (mAuth.getCurrentUser() != null) {
                        final Tokens tokens = dataSnapshot.getValue(Tokens.class);
                        NotificationModel notificationModell = new NotificationModel(Global.nameLocal,encryption.decryptOrNull(message));
                        final Message messageMM = new Message(name,ava,id,message,null,mAuth.getCurrentUser().getUid(),Mid);
                        FirebaseDatabase.getInstance().getReference(Global.USERS).child(id).child(Global.device).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String isAndroid = dataSnapshot.getValue(String.class);
                                    System.out.println("jkjjjjjjjjjjjjkkll");
                                    NotifyManager.MessageNotificationToUser(tokens.getTokens(),isAndroid,messageMM);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (NullPointerException e){}
    }
    public static String timeSec(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds - minutes * 60;

        String formattedTime = "";

        if (minutes < 10)
            formattedTime += "0";
        formattedTime += minutes + ":";

        if (seconds < 10)
            formattedTime += "0";
        formattedTime += seconds;

        return formattedTime;
    }

}
