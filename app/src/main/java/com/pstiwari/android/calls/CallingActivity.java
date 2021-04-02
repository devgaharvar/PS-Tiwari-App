package com.pstiwari.android.calls;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
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
import com.jgabrielfreitas.core.BlurImageView;
import com.pstiwari.android.ApiService;
import com.pstiwari.android.Contacts;
import com.pstiwari.android.Notification.CallNotification;
import com.pstiwari.android.Notification.Message;
import com.pstiwari.android.Notification.NotificationModel;
import com.pstiwari.android.Notification.NotifyManager;
import com.pstiwari.android.Notification.Tokens;
import com.pstiwari.android.R;
import com.pstiwari.android.SettingsActivity;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CallingActivity extends AppCompatActivity  {

    ImageView mMuteBtn;
    //vars
    private boolean mCallEnd, mMuted, out;
    String channelid, friendid, name, ava,history;
    FirebaseAuth mAuth;
    private RtcEngine mRtcEngine;
    private DatabaseReference RootRef;
    //fcm
    ApiService fcm;

    String Mid, encrypM;

    DatabaseReference mlogs, mChat, mUser;


    Handler mHandler, mTimeout;
    boolean isRunning = true, isRunningTime = true, single = true;
    int time = 0, timeout = 0;
    EmojiTextView nameT;
    TextView state;
    private AudioPlayer mAudioPlayer;

    CircleImageView avaI;
    BlurImageView bgI;
    Object currTime;
    boolean first = true;

    private static final String LOG_TAG = CallingActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAuth.getCurrentUser() != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("incall", true);
                        mUser.child(mAuth.getCurrentUser().getUid()).updateChildren(map);
                    }
                    if (out) {
                        mUser.child(friendid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Usercalldata usercalldata = dataSnapshot.getValue(Usercalldata.class);
                            if (!usercalldata.isIncall()) {
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
                                                            System.out.println("1 11111111111");
                                                        }

                                                    }
                                                });
                                            } catch (Exception e) {
                                            }
                                        }
                                    }
                                }).start();
                            } else {
                                mAudioPlayer.stopProgressTone();
                                state.setText(getString(R.string.userbusy));
                                Toast.makeText(CallingActivity.this, R.string.userbusy, Toast.LENGTH_LONG).show();
                                leaveChannel();

                                System.out.println("222222   222222");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                    if (mAuth.getCurrentUser() != null) {

                        mlogs.child(mAuth.getCurrentUser().getUid()).child(friendid).child(channelid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (mAuth.getCurrentUser() != null) {

                                    Usercalldata usercalldata = dataSnapshot.getValue(Usercalldata.class);

                                    try {
                                        if (!usercalldata.isIncall())
                                        {
                                            leaveChannel();
                                            System.out.println("s 333333333");
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
                }

            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            leaveChannel();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
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
        public void onUserMuteAudio(final int uid, final boolean muted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }


    };

    /*@Override
    protected void onResume() {

        super.onResume();
        changeScreenBrightness();
        Global.currentactivity = this;
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Global.USERS);
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
    }*/

    private void onRemoteUserLeft() {

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
                            Toast.makeText(CallingActivity.this, "Please update your profile...", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_calling);
        Global.currentactivity = this;
        mAuth = FirebaseAuth.getInstance();
        mlogs = FirebaseDatabase.getInstance().getReference(Global.CALLS);
        mUser = FirebaseDatabase.getInstance().getReference(Global.USERS);
        mChat = FirebaseDatabase.getInstance().getReference(Global.CHATS);
        mAudioPlayer = new AudioPlayer(this);
        channelid = getIntent().getStringExtra("channel_id");
        name = getIntent().getStringExtra("name");
        out = getIntent().getBooleanExtra("out", false);
        friendid = getIntent().getStringExtra("UserId");
        ava = getIntent().getStringExtra("ava");
        history=getIntent().getStringExtra("history");
        Mid = mAuth.getCurrentUser().getUid() + "_" + friendid + "_" + String.valueOf(System.currentTimeMillis());


        nameT = findViewById(R.id.name);
        state = findViewById(R.id.state);
        avaI = findViewById(R.id.ava);
         bgI = findViewById(R.id.bg);

        state.setText(getResources().getString(R.string.connecting));



        if (history.equals("true"))
        {
            mUser.child(friendid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if ((dataSnapshot.exists()))
                    {

                        if ((dataSnapshot.hasChild("image")))
                        {
                            String userImage = dataSnapshot.child("image").getValue().toString();
                            if(!userImage.isEmpty()) {
                                Picasso.get().load(userImage).placeholder(R.drawable.profile).into(avaI);
                            } else {
                                avaI.setImageResource(R.drawable.profile);
                            }
                        }
                        else
                        {
                            avaI.setImageResource(R.drawable.profile);
                        }
                        String userName = dataSnapshot.child("name").getValue().toString();

                        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(friendid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot sp : snapshot.getChildren()) {
                                    Contacts ct = sp.getValue(Contacts.class);
                                    assert ct != null;
                                    System.out.println("ssafgg44444444"+ct.getPhone());
                                    if (!contactExists(CallingActivity.this,ct.getPhone()).equals("")){
                                        nameT.setText(contactExists(CallingActivity.this,ct.getPhone()));
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
                        if (!contactExists(CallingActivity.this,ct.getPhone()).equals("")){
                            nameT.setText(contactExists(CallingActivity.this,ct.getPhone()));
                        }else {
                            nameT.setText(ct.getPhone());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if (!ava.isEmpty()) {
            if (ava.equals("")) {
                Picasso.get()
                        .load(R.drawable.profile)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.errorimg)
                        .into(avaI);
                Picasso.get()
                        .load(R.drawable.bg)
                        .placeholder(R.drawable.bg).error(R.drawable.errorimg)

                        .into(bgI, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                bgI.setBlur(22);

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            } else {
                Picasso.get()
                        .load(ava)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.errorimg)

                        .into(avaI);
                Picasso.get()
                        .load(ava)
                        .placeholder(R.drawable.bg)
                        .error(R.drawable.errorimg)

                        .into(bgI, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                bgI.setBlur(22);

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            }
        }

        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel();
        }

        //fcm notify
        fcm = Global.getFCMservies();
        changeScreenBrightness();
        mMuteBtn = findViewById(R.id.btn_muted);

        try {
            currTime = ServerValue.TIMESTAMP;
  /*          Map<String, Object> map33 = new HashMap<>();
            map33.put("from", mAuth.getCurrentUser().getUid());
            map33.put("to", friendid);
            map33.put("time", currTime);
            map33.put("id", channelid);
            map33.put("name", name);
            map33.put("ava", ava);
            map33.put("incall", false);

            mlogs.child(mAuth.getCurrentUser().getUid()).child(friendid).child(channelid).onDisconnect().updateChildren(map33);
         */   if (mAuth.getCurrentUser() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("from", mAuth.getCurrentUser().getUid());
                map.put("to", friendid);
                map.put("time", currTime);
                map.put("id", channelid);
                map.put("name", name);
                map.put("ava", ava);
                map.put("incall", true);
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

        } catch (NullPointerException e) {
            Toast.makeText(CallingActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }


    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();
        joinChannel(channelid);
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPause() {
        mAudioPlayer.stopProgressTone();
        Global.currentactivity = null;
        try {
            if (Global.wl != null) {
                if (Global.wl.isHeld()) {
                    Global.wl.release();
                }
                Global.wl = null;
            }
        } catch (NullPointerException e) {

        }
        // this.getApplication()).startActivityTransitionTimer();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAudioPlayer.stopProgressTone();
        try {
            AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.USE_DEFAULT_STREAM_TYPE );
            audioManager.setSpeakerphoneOn(false);
            if (Global.wl != null) {
                if (Global.wl.isHeld()) {
                    Global.wl.release();
                }
                Global.wl = null;
            }

        } catch (NullPointerException e) {

        }


        leaveChannel();
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

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);
        mRtcEngine.muteLocalAudioStream(mMuted);
    }


    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.setEnableSpeakerphone(view.isSelected());

    }

    public void onEncCallClicked(View view) {
        leaveChannel();
    }

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
            mRtcEngine.setEnableSpeakerphone(false);
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
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


    private void leaveChannel() {

        System.out.println("dddddddddddddddd3333333335");
        mRtcEngine.leaveChannel();
        isRunning = false;
        if (mAuth.getCurrentUser() != null) {
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
                                        encrypM = encryption.encryptOrNull("Audio call " + timeSec(time));
                                    }

                                    else
                                    {
                                        encrypM = encryption.encryptOrNull("Missed Audio call");
                                    }

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

                System.out.println("ssssssssssssssssssss leave");
                finish();
            }
        }

    }


    private void changeScreenBrightness() {
        //Set screen brightness
        Global.pm = (PowerManager) getSystemService(POWER_SERVICE);
        Global.wl = Global.pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "Dim/Light:");
        Global.wl.acquire();
    }

    private void sendMessNotify() {
        try{
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
                                NotificationModel notificationModel = new NotificationModel(Global.nameLocal,Global.nameLocal + getString(R.string.iscalling) );
                                CallNotification callNotification = new CallNotification(name,ava,friendid
                                        ,encryption.encryptOrNull(channelid),encryption.encryptOrNull(mAuth.getCurrentUser().getUid())
                                        ,encryption.encryptOrNull(name),encryption.encryptOrNull(ava)
                                        ,mAuth.getCurrentUser().getUid(),Mid);
                                NotifyManager.VoiceCallNotificationToUser(tokens.getTokens(),isAndroi,callNotification);

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

    }catch (NullPointerException e){

        }
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

        } catch (NullPointerException e){

        }
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
