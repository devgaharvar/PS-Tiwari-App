package  com.pstiwari.android.calls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jgabrielfreitas.core.BlurImageView;
import com.pstiwari.android.Contacts;
import com.pstiwari.android.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;

public class IncCallActivity extends AppCompatActivity {
    static final String TAG = IncCallActivity.class.getSimpleName();
    private String mCallId;
    TextView remoteUser;
    ImageButton ans, dec;
    String friendId,nameE,avaE,channel_id;
    TextView name;
    BlurImageView bg;
    CircleImageView img;
    private AudioPlayer mAudioPlayer;
    DatabaseReference mUser,mlogs;
    FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private FirebaseUser user;
    String currentUserID;
boolean dest = true;

ValueEventListener child;
Query query;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_inc_call);

        Global.IncVActivity = this;

        Global.currentactivity = this;

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();

        remoteUser = (TextView) findViewById(R.id.remoteUser);
        dec = findViewById(R.id.declineButton);
        ans = findViewById(R.id.answerButton);

        name = (TextView) findViewById(R.id.remoteUser);
        img =findViewById(R.id.circleImageView);
        bg =  findViewById(R.id.bg);


        mUser = FirebaseDatabase.getInstance().getReference(Global.USERS);
        mlogs = FirebaseDatabase.getInstance().getReference(Global.CALLS);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> update = new HashMap<>();
        update.put("callstate", "on");

        RootRef.child("Users").child(currentUserID).updateChildren(update)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                         //   Toast.makeText(IncCallActivity.this, "call atended", Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                        }
                    }
                });


        if(getIntent() != null)
        {
            friendId =  getIntent().getStringExtra("id");
            nameE = getIntent().getExtras().getString("name");
            avaE = getIntent().getExtras().getString("ava");
            channel_id = getIntent().getExtras().getString("channel_id");
            mUser.child(friendId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if ((dataSnapshot.exists()))
                    {
                        if ((dataSnapshot.hasChild("image")))
                        {
                            String userImage = dataSnapshot.child("image").getValue().toString();
                            if(!userImage.isEmpty()) {
                                Picasso.get().load(userImage).placeholder(R.drawable.profile).into(img);
                            } else {
                                img.setImageResource(R.drawable.profile);
                            }
                        }
                        else
                        {
                            img.setImageResource(R.drawable.profile);
                        }

                        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(friendId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot sp : snapshot.getChildren()) {
                                    Contacts ct = sp.getValue(Contacts.class);
                                    assert ct != null;
                                    System.out.println("ssafgg44444444"+ct.getPhone());
                                    if (!contactExists(IncCallActivity.this,ct.getPhone()).equals("")){
                                        name.setText(contactExists(IncCallActivity.this,ct.getPhone()));
                                    }else {
                                        name.setText(ct.getPhone());
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



                Map<String, Object> map = new HashMap<>();
                map.put("incall", true);
                mUser.child(friendId).updateChildren(map);

    /*        if (String.valueOf(getIntent().getExtras().getString("ava")).equals("")) {
                Picasso.get()
                        .load(R.drawable.profile)
                        .placeholder(R.drawable.profile) .error(R.drawable.errorimg)

                        .into(img);
                Picasso.get()
                        .load(R.drawable.bg)
                        .placeholder(R.drawable.bg) .error(R.drawable.errorimg)

                        .into(bg, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                bg.setBlur(22);

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            } else {
                Picasso.get()
                        .load(getIntent().getExtras().getString("ava"))
                        .placeholder(R.drawable.profile) .error(R.drawable.errorimg)
                        .into(img);
                Picasso.get().load(getIntent().getExtras().getString("ava"))
                        .placeholder(R.drawable.bg) .error(R.drawable.errorimg)
                        .into(bg, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                bg.setBlur(22);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            }*/
        }
        query =mUser.child(friendId);
        child = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser()!=null) {
                    Usercalldata usercalldata = dataSnapshot.getValue(Usercalldata.class);

                    System.out.println("dddddd"+usercalldata.isIncall());
                    try {
                        if (!usercalldata.isIncall())
                            finish();
                    } catch (NullPointerException e) {
                  //      finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.stopRingtone();
                if(mAuth.getCurrentUser()!= null) {

                    final Map<String, Object> map = new HashMap<>();
                    map.put("incall", false);
                    mlogs.child(mAuth.getCurrentUser().getUid()).child(friendId).child(channel_id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mlogs.child(friendId).child(mAuth.getCurrentUser().getUid()).child(channel_id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    finish();
                                }
                            });
                        }
                    });
                }
            }
        });

        ans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> update = new HashMap<>();
                update.put("callstate", "off");

                RootRef.child("Users").child(currentUserID).updateChildren(update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful())
                                {
                           //         Toast.makeText(IncCallActivity.this, "call atended", Toast.LENGTH_LONG).show();

                                }
                                else
                                {
                                }
                            }
                        });
                dest = false;
                mAudioPlayer.stopRingtone();
                Intent jumptocall = new Intent(IncCallActivity.this, CallingActivityVideo.class);
                jumptocall.putExtra("name", nameE);
                jumptocall.putExtra("ava", avaE);
                jumptocall.putExtra("out", false);
                jumptocall.putExtra("history","true");
                jumptocall.putExtra("channel_id", channel_id);
                jumptocall.putExtra("UserId", friendId);
                startActivity(jumptocall);
                finish();
            }
        });


    }
    @Override
    protected void onPause() {
        super.onPause();
        mAudioPlayer.stopRingtone();
        Global.currentactivity = null;
       // ((AppBack) this.getApplication()).startActivityTransitionTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        query.removeEventListener(child);
        Global.IncVActivity = null;
        mAudioPlayer.stopRingtone();
        if(mAuth.getCurrentUser()!= null) {
            if (dest) {
                final Map<String, Object> map = new HashMap<>();
                map.put("incall", false);
                mlogs.child(mAuth.getCurrentUser().getUid()).child(friendId).child(channel_id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mlogs.child(friendId).child(mAuth.getCurrentUser().getUid()).child(channel_id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        });
                    }
                });
            }

            HashMap<String, Object> update = new HashMap<>();
            update.put("callstate", "off");

            RootRef.child("Users").child(currentUserID).updateChildren(update)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                  //              Toast.makeText(IncCallActivity.this, "call atended", Toast.LENGTH_LONG).show();

                            }
                            else
                            {
                            }
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        Global.IncVActivity = this;
        super.onResume();
        Global.currentactivity = this;
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Global.USERS);
        /*AppBack myApp = (AppBack) this.getApplication();
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

        myApp.stopActivityTransitionTimer();*/
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

}
