package  com.pstiwari.android.calls;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.pstiwari.android.SettingsActivity;
import com.pstiwari.android.calls.AudioPlayer;
import com.pstiwari.android.calls.Global;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;

public class IncAudioActivity extends AppCompatActivity {
    static final String TAG = IncAudioActivity.class.getSimpleName();
    TextView name;
    BlurImageView bg;
    CircleImageView img;
    String nameE, avaE, idd, channel_id;
    private AudioPlayer mAudioPlayer;
    DatabaseReference mUser;
    FirebaseAuth mAuth;
    DatabaseReference mlogs;
    boolean dest = true;
    private DatabaseReference RootRef;
    private FirebaseUser user;
    String currentUserID;
ValueEventListener child;
Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_inc_audio);
        Global.IncAActivity = this;


        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();


        mUser = FirebaseDatabase.getInstance().getReference(Global.USERS);
        mlogs = FirebaseDatabase.getInstance().getReference(Global.CALLS);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        ImageButton answer = findViewById(R.id.btn_accept);
        answer.setOnClickListener(mClickListener);
        ImageButton decline = findViewById(R.id.btn_reject);
        decline.setOnClickListener(mClickListener);

        name = (TextView) findViewById(R.id.username);
        img = findViewById(R.id.circleImageView);
        bg = findViewById(R.id.bg);

        HashMap<String, Object> update = new HashMap<>();
        update.put("callstate", "on");

        RootRef.child("Users").child(currentUserID).updateChildren(update)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                          //  Toast.makeText(IncAudioActivity.this, "call atended", Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                        }
                    }
                });
        if (getIntent() != null) {
            idd = getIntent().getExtras().getString("id");
         //   name.setText(getIntent().getExtras().getString("name"));
            nameE = getIntent().getExtras().getString("name");
            avaE = getIntent().getExtras().getString("ava");
            channel_id = getIntent().getExtras().getString("channel_id");



            Map<String, Object> map = new HashMap<>();
            map.put("incall", true);
            mUser.child(idd).updateChildren(map);
            mUser.child(idd).addValueEventListener(new ValueEventListener() {
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

                        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(idd);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot sp : snapshot.getChildren()) {
                                    Contacts ct = sp.getValue(Contacts.class);
                                    assert ct != null;
                                    System.out.println("ssafgg44444444"+ct.getPhone());
                                    if (!contactExists(IncAudioActivity.this,ct.getPhone()).equals("")){
                                        name.setText(contactExists(IncAudioActivity.this,ct.getPhone()));
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
        /*    if (String.valueOf(getIntent().getExtras().getString("ava")).equals("")) {
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
                Picasso.get()
                        .load(R.drawable.profile)
                        .placeholder(R.drawable.profile) .error(R.drawable.errorimg)

                        .into(img);
            } else {
                Picasso.get()
                        .load(getIntent().getExtras().getString("ava"))
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.errorimg)
                        .into(img);
                Picasso.get()
                        .load(getIntent().getExtras().getString("ava"))
                        .placeholder(R.drawable.bg)
                        .error(R.drawable.errorimg)
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
        query =mUser.child(idd);
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

    }


    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_accept:
                    answerClicked();
                    break;
                case R.id.btn_reject:
                    declineClicked();
                    break;
            }
        }
    };

    private void answerClicked() {

        HashMap<String, Object> update = new HashMap<>();
        update.put("callstate", "off");

        RootRef.child("Users").child(currentUserID).updateChildren(update)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                          //  Toast.makeText(IncAudioActivity.this, "call atended", Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                        }
                    }
                });

        mAudioPlayer.stopRingtone();
        dest = false;
        Intent jumptocall = new Intent(IncAudioActivity.this, CallingActivity.class);
        jumptocall.putExtra("name", nameE);
        jumptocall.putExtra("ava", avaE);
        jumptocall.putExtra("out", false);
        jumptocall.putExtra("history","true");
        jumptocall.putExtra("channel_id", channel_id);
        jumptocall.putExtra("UserId", idd);
        startActivity(jumptocall);
        finish();

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

    private void declineClicked() {
        mAudioPlayer.stopRingtone();
        if(mAuth.getCurrentUser()!= null) {

            final Map<String, Object> map = new HashMap<>();
            map.put("incall", false);
            mlogs.child(mAuth.getCurrentUser().getUid()).child(idd).child(channel_id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mlogs.child(idd).child(mAuth.getCurrentUser().getUid()).child(channel_id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAudioPlayer.stopRingtone();
      //  ((AppBack) this.getApplication()).startActivityTransitionTimer();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioPlayer.stopRingtone();
        query.removeEventListener(child);
        Global.IncAActivity = null;
        if(mAuth.getCurrentUser()!= null) {
            if (dest) {

                final Map<String, Object> map = new HashMap<>();
                map.put("incall", false);
                mlogs.child(mAuth.getCurrentUser().getUid()).child(idd).child(channel_id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mlogs.child(idd).child(mAuth.getCurrentUser().getUid()).child(channel_id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        super.onResume();
        Global.IncAActivity = this;
        Global.currentactivity = this;
      /*  DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Global.USERS);
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

        myApp.stopActivityTransitionTimer();*/
    }

}
