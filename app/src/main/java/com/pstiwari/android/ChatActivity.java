package com.pstiwari.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.khizar1556.mkvideoplayer.MKPlayerActivity;
import com.pstiwari.android.Notification.SendNotificationUtil;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.pstiwari.android.call.PlaceCallActivity;
import com.pstiwari.android.call.video.CallScreenActivity;
import com.pstiwari.android.calls.CallingActivity;
import com.pstiwari.android.calls.CallingActivityVideo;
import com.pstiwari.android.calls.Global;
import com.pstiwari.android.common.Utill;
import com.rygelouv.audiosensei.player.AudioSenseiListObserver;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.sql.Ref;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity implements MessageAdapter.onclickListener {
    private MediaPlayer mediaPlayer;
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    public TextView userName, userLastSeen,txtBlock;
    private CircleImageView userImage;
    private ImageView backpress;
    private String phone;
    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private MenuItem itemBlock;
    private MenuItem itemUnblock;
    private MenuItem addContact;
    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;
    private RelativeLayout inboxLayout,chat_layout;

    private final List<Messages> messagesList = new ArrayList<>();
    private final List<String> keysList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private DatabaseReference BlockRef;

    private String saveCurrentTime, saveCurrentDate,nameu;
    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    String fileName = "";
    ImageView camera_btn, phone_btn;
    private MediaRecorder recorder = null;
    private ProgressDialog loadingBar;

    public static String notificationRefId;
    private RecordView record_view = null;
    private RecordButton record_button = null;
    public String myUserName;
    private Boolean bit = true;

    private ValueEventListener seenListener;
    private ChildEventListener chatListener;
    ValueEventListener blocklistner;
    private AdView mAdView;


    //private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AudioSenseiListObserver.getInstance().registerLifecycle(getLifecycle());

        BlockRef = FirebaseDatabase.getInstance().getReference().child("Blocks");

        chat_layout = findViewById(R.id.chat_layout);
        txtBlock = findViewById(R.id.txtBlock);
        record_view = findViewById(R.id.record_view);
        record_button = findViewById(R.id.record_button);
        inboxLayout = findViewById(R.id.inbox_layout);
//

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.keepSynced(true);
        phone=getIntent().getExtras().get("visit_phone").toString();
        record_button.setRecordView(record_view);
        record_view.setCancelBounds(16f);
        record_view.setSoundEnabled(false);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        voiceRecorder();
        record_button.setListenForRecord(false);
        if (isPermissionGranted()) {
            record_button.setListenForRecord(true);
        }

//        backpress.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        fileName = getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.mp3";

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        nameu=getIntent().getExtras().get("visit_user_name").toString();
        if (getIntent().getExtras().get("visit_user_name") != null) {
           // messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        } else {
            Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(messageReceiverID);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        Contacts ct = sp.getValue(Contacts.class);
                        assert ct != null;
                        if (!contactExists(ChatActivity.this,phone).equals("")){
                            userName.setText(contactExists(ChatActivity.this,phone));
                        }else {
                            userName.setText(phone);
                        }
                        if(!ct.getImage().isEmpty()) {
                            Picasso.get().load(ct.getImage()).placeholder(R.drawable.dp2).into(userImage);
                        } else {
                            userImage.setImageResource(R.drawable.dp2);
                        }
                        Utill.ContacterImage = ct.getImage();
                        bit = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        if (getIntent().getExtras().get("visit_image") != null) {
            messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        }


        IntializeControllers();
        if (!contactExists(ChatActivity.this,phone).equals("")) {
            userName.setText(contactExists(ChatActivity.this,phone));

        }else {
            userName.setText(phone);

        }
        if (bit) {
            Utill.ContacterImage = messageReceiverImage == null ? "" : messageReceiverImage;
        }
        if(!messageReceiverImage.isEmpty()) {
            Picasso.get().load(messageReceiverImage).placeholder(R.drawable.dp2).into(userImage);
        } else {
            userImage.setImageResource(R.drawable.dp2);
        }

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });


        DisplayLastSeen();

        notificationRefId = messageReceiverID;

        RootRef.child("Users").child(messageSenderID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                            myUserName = dataSnapshot.child("name").getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "Audio",
                                "Video",
                                "PDF Files",
                                "MS Word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Send Image"), 438);
                        }
                        if (i == 3) {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF file"), 438);
                        }
                        if (i == 4) {
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word File"), 438);
                        }

                        if (i == 2) {
                            checker = "video";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("video/mp4");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word File"), 438);
                        }

                        if (i == 1) {
                            checker = "audio";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/*");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word File"), 438);
                        }
                    }
                });
                builder.show();
            }
        });

        System.out.println("dddddddd"+messageReceiverID+" 22222222"+messageSenderID);
   /*   seenListener=RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    System.out.println("gaaaaaaaaaaaa"+messages.getIsseen());
                    if(messages.getIsseen()==false) {
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                        RootRef.child("Users").child(messageReceiverID).child("lastMessage").child(messageSenderID).updateChildren(hashMap);

                    }
                }
                RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).removeEventListener( seenListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).removeEventListener( seenListener);
            }
        });
*/
        seenListener=RootRef.child("Messages").child(messageSenderID).child("Receive").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    System.out.println("gaaaaaaaaaaaa"+messages.getIsseen());
                    if(messages.getIsseen()==false) {
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                        RootRef.child("Users").child(messageReceiverID).child("lastMessage").child(messageSenderID).updateChildren(hashMap);
                    }
                }
                RootRef.child("Messages").child(messageSenderID).child("Receive").child(messageReceiverID).removeEventListener( seenListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                RootRef.child("Messages").child(messageSenderID).child("Receive").child(messageReceiverID).removeEventListener( seenListener);
            }
        });

        chatListener = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);
                        keysList.add(messages.getMessageID());
                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        int index = keysList.indexOf(messages.getMessageID());
                        if(index != -1) {
                            messagesList.remove(index);
                            messagesList.add(index, messages);
                            messageAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        int index = keysList.indexOf(messages.getMessageID());
                         if(index != -1) {
                            keysList.remove(index);
                            messagesList.remove(index);
                            messageAdapter.notifyDataSetChanged();
                         }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        System.out.println("adassssdfss"+messageSenderID+"  "+messageReceiverID);
        BlockRef.child(messageSenderID).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String status = snapshot.child("status").getValue().toString();
                    System.out.println("sssssssassssssss"+status);
                    if (status.equals("false"))
                    {
                       txtBlock.setText("you have blocked this user");
                    }
                    else if (status.equals("true"))
                    {
                        txtBlock.setText("This user has blocked you");
                    }
                    txtBlock.setVisibility(View.VISIBLE);
                    chat_layout.setVisibility(View.INVISIBLE);
                } else {
                    txtBlock.setVisibility(View.GONE);
                    chat_layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //updateStatus();

        //apiService = Client.getClient("https://fcm.googleapis.com/").create(ApiService.class);

    }

    public void shareVideo(View view) {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                String newPath = getVideoContentUriFromFilePath(ChatActivity.this, "videoPath");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Title");
                //intent.setAction(Intent.ACTION_SEND);
                intent.setType("video/mp4");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(newPath));
                try {
                    startActivity(Intent.createChooser(intent, "Upload video via:"));
                } catch (android.content.ActivityNotFoundException ex) {

                }
            }
        });
    }

    public static String getVideoContentUriFromFilePath(Context ctx, String filePath) {

        ContentResolver contentResolver = ctx.getContentResolver();
        String videoUriStr = null;
        long videoId = -1;
        Log.d("first log", "Loading file " + filePath);

        // This returns us content://media/external/videos/media (or something like that)
        // I pass in "external" because that's the MediaStore's name for the external
        // storage on my device (the other possibility is "internal")
        Uri videosUri = MediaStore.Video.Media.getContentUri("external");

        Log.d("second log", "videosUri = " + videosUri.toString());

        String[] projection = {MediaStore.Video.VideoColumns._ID};

        // TODO This will break if we have no matching item in the MediaStore.
        Cursor cursor = contentResolver.query(videosUri, projection, MediaStore.Video.VideoColumns.DATA + " LIKE ?", new String[]{filePath}, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        videoId = cursor.getLong(columnIndex);

        Log.d("third log", "Video ID is " + videoId);
        cursor.close();
        if (videoId != -1) videoUriStr = videosUri.toString() + "/" + videoId;
        return videoUriStr;
    }

    @Override
    protected void onPause() {
        super.onPause();
        notificationRefId = null;
        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationRefId = null;
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).removeEventListener(chatListener);
       // RootRef.child("Messages").child(messageReceiverID).child(messageSenderID).removeEventListener(seenListener);
    }

    private void IntializeControllers() {

        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        backpress = findViewById(R.id.backpress);
        phone_btn = findViewById(R.id.phone_btn);
        camera_btn = findViewById(R.id.camera_btn);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList, messageSenderID, messageReceiverID, ChatActivity.this);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
        messageAdapter.navigator(this);
        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();


        phone_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                blocklistner=BlockRef.child(messageSenderID).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String status = snapshot.child("status").getValue().toString();
                            System.out.println("sssssssassssssss"+status);
                            if (status.equals("false"))
                            {
                                Toast.makeText(ChatActivity.this, "you have blocked this user can't make call", Toast.LENGTH_SHORT).show();
                                BlockRef.child(messageSenderID).child(messageReceiverID).removeEventListener(blocklistner);
                            }
                            else if (status.equals("true"))
                            {
                                Toast.makeText(ChatActivity.this, "This user has blocked you can't make call", Toast.LENGTH_SHORT).show();
                                BlockRef.child(messageSenderID).child(messageReceiverID).removeEventListener(blocklistner);
                            }

                        } else {
                            System.out.println("dssssssssssssss");
                            Toast.makeText(ChatActivity.this, "calling", Toast.LENGTH_LONG).show();
                            String callid = mAuth.getCurrentUser().getUid()+ System.currentTimeMillis();
                            Intent jumptocall = new Intent(getApplicationContext(), CallingActivity.class);
                            jumptocall.putExtra("name", nameu);
                            jumptocall.putExtra("ava", messageReceiverImage);
                            jumptocall.putExtra("out", true);
                            jumptocall.putExtra("history","false");
                            jumptocall.putExtra("channel_id", callid);
                            jumptocall.putExtra("UserId", messageReceiverID);
                            jumptocall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(jumptocall);
                            BlockRef.child(messageSenderID).child(messageReceiverID).removeEventListener(blocklistner);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
             //           BlockRef.child(messageSenderID).child(messageReceiverID).removeEventListener(blocklistner);
                    }
                });


            }
        });
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blocklistner=BlockRef.child(messageSenderID).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String status = snapshot.child("status").getValue().toString();
                            System.out.println("sssssssassssssss"+status);
                            if (status.equals("false"))
                            {
                                Toast.makeText(ChatActivity.this, "you have blocked this user can't make call", Toast.LENGTH_SHORT).show();
                                BlockRef.child(messageSenderID).child(messageReceiverID).removeEventListener(blocklistner);
                            }
                            else if (status.equals("true"))
                            {
                                Toast.makeText(ChatActivity.this, "This user has blocked you can't make call", Toast.LENGTH_SHORT).show();
                                BlockRef.child(messageSenderID).child(messageReceiverID).removeEventListener(blocklistner);
                            }

                        } else {
                            System.out.println("kjsssssssssssss");
                            System.out.println("sssssssssssssa"+"messageReceiverID"+messageReceiverID+"ava"+messageReceiverImage+"name"+userName.getText().toString());
                            String callid = mAuth.getCurrentUser().getUid() + System.currentTimeMillis();
                            Intent jumptocall = new Intent(ChatActivity.this, CallingActivityVideo.class);
                            jumptocall.putExtra("UserId", messageReceiverID);
                            jumptocall.putExtra("channel_id", callid);
                            jumptocall.putExtra("ava", messageReceiverImage);
                            jumptocall.putExtra("history","false");
                            jumptocall.putExtra("name", nameu);
                            jumptocall.putExtra("id", messageReceiverID);
                            jumptocall.putExtra("out", true);
                            startActivity(jumptocall);
                            BlockRef.child(messageSenderID).child(messageReceiverID).removeEventListener(blocklistner);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        MessageInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    SendMessageButton.setVisibility(View.VISIBLE);
                    record_button.setVisibility(View.GONE);
                } else {
                    record_button.setVisibility(View.VISIBLE);
                    SendMessageButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backpress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadingBar.setTitle("Sending file");
            loadingBar.setMessage("Please wait, we are sending your file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if (!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
                final String messageSenderRefsend = "Messages/" + messageSenderID + "/" +"Send" + "/" + messageReceiverID;
                final String messageReceiverRefrecive = "Messages/" + messageReceiverID + "/" +"Receive" + "/" + messageSenderID;
                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Map messageFileBody = new HashMap();
                                messageFileBody.put("message", downloadUrl);
                                messageFileBody.put("name", fileUri.getLastPathSegment());
                                messageFileBody.put("type", checker);
                                messageFileBody.put("from", messageSenderID);
                                messageFileBody.put("to", messageReceiverID);
                                messageFileBody.put("messageID", messagePushID);
                                messageFileBody.put("time", saveCurrentTime);
                                messageFileBody.put("date", saveCurrentDate);
                                messageFileBody.put("isseen", false);


                                Map messageBodyDetail = new HashMap();
                                messageBodyDetail.put(messageSenderRef + "/" + messagePushID, messageFileBody);
                                messageBodyDetail.put(messageReceiverRef + "/" + messagePushID, messageFileBody);
                                messageBodyDetail.put(messageSenderRefsend + "/" + messagePushID, messageFileBody);
                                messageBodyDetail.put(messageReceiverRefrecive + "/" + messagePushID, messageFileBody);

                                RootRef.updateChildren(messageBodyDetail);
                                loadingBar.dismiss();
                                new SendNotificationUtil().sendNotifiaction(getApplicationContext(), messageReceiverID,
                                        myUserName, "File", messageSenderID,phone,messageReceiverImage,nameu);
                                setUserLastMessage(messageFileBody, messagePushID);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });

            } else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
                final String messageSenderRefsend = "Messages/" + messageSenderID + "/" +"Send" + "/" + messageReceiverID;
                final String messageReceiverRefrecive = "Messages/" + messageReceiverID + "/" +"Receive" + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderID);
                            messageImageBody.put("to", messageReceiverID);
                            messageImageBody.put("messageID", messagePushID);
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrentDate);
                            messageImageBody.put("isseen", false);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageSenderRefsend + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRefrecive + "/" + messagePushID, messageImageBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        new SendNotificationUtil().sendNotifiaction(getApplicationContext(), messageReceiverID,
                                                myUserName, "Image", messageSenderID,phone,messageReceiverImage,nameu);

                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });

                            setUserLastMessage(messageImageBody, messagePushID);
                        }
                    }
                });


            } else {
                loadingBar.dismiss();
                Toast.makeText(this, "ERROR, Nothing Selected...", Toast.LENGTH_SHORT).show();
            }
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                && (data.getData() == null) && requestCode == 438 && resultCode == RESULT_OK) {
            //Multiple images
            if (checker.equals("image")) {
                ClipData clipdata = data.getClipData();
                for (int i = 0; i < clipdata.getItemCount(); i++) {
                    //final Uri muri = clipdata.getItemAt(i).getUri();
                    //System.out.println("file = "+clipdata.getItemAt(i).getUri().getPath());

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                    final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                    final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
                    final String messageSenderRefsend = "Messages/" + messageSenderID + "/" +"Send" + "/" + messageReceiverID;
                    final String messageReceiverRefrecive = "Messages/" + messageReceiverID + "/" +"Receive" + "/" + messageSenderID;

                    DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                            .child(messageSenderID).child(messageReceiverID).push();

                    final String messagePushID = userMessageKeyRef.getKey();

                    final StorageReference filePath = storageReference.child(messagePushID + ".jpg");

                    uploadTask = filePath.putFile(clipdata.getItemAt(i).getUri());

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                myUrl = downloadUri.toString();

                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                                saveCurrentDate = currentDate.format(calendar.getTime());
                                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                                saveCurrentTime = currentTime.format(calendar.getTime());

                                Random rand = new Random();

                                Map messageImageBody = new HashMap();
                                messageImageBody.put("message", myUrl);
                                messageImageBody.put("name", "PST_" + rand.nextInt(10000) + ".jpg");
                                messageImageBody.put("type", checker);
                                messageImageBody.put("from", messageSenderID);
                                messageImageBody.put("to", messageReceiverID);
                                messageImageBody.put("messageID", messagePushID);
                                messageImageBody.put("time", saveCurrentTime);
                                messageImageBody.put("date", saveCurrentDate);
                                messageImageBody.put("isseen", false);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);
                                messageBodyDetails.put(messageSenderRefsend + "/" + messagePushID, messageImageBody);
                                messageBodyDetails.put(messageReceiverRefrecive + "/" + messagePushID, messageImageBody);

                                RootRef.updateChildren(messageBodyDetails);

                                new SendNotificationUtil().sendNotifiaction(getApplicationContext(), messageReceiverID,
                                        myUserName, "Image", messageSenderID,phone,messageReceiverImage,nameu);

                                setUserLastMessage(messageImageBody, messagePushID);
                            }


                        }
                    });
                }
            }
        }
    }

    private void setUserLastMessage(Map lastMessage, String messagePushID) {
        RootRef.child("Users").child(messageSenderID).child("lastMessage").child(messageReceiverID).setValue(lastMessage);
        RootRef.child("Users").child(messageReceiverID).child("lastMessage").child(messageSenderID).setValue(lastMessage);
        RootRef.child("Contacts").child(messageSenderID).child(messageReceiverID).child("key").setValue(messagePushID);
        RootRef.child("Contacts").child(messageReceiverID).child(messageSenderID).child("key").setValue(messagePushID);

    }

    private void DisplayLastSeen() {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            } else if (state.equals("offline")) {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        } else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        /*RootRef.child("Users").child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                if (dataSnapshot.child("userState").hasChild("state"))
                {
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online"))
                    {
                        userLastSeen.setText("online");
                    }
                    else if (state.equals("offline"))
                    {
                        userLastSeen.setText("Last Seen: " + date + " " + time);
                    }
                }
                else
                {
                    userLastSeen.setText("offline");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    private void SendMessage() {
        if (messageSenderID == messageReceiverID) {
            Toast.makeText(getApplicationContext(), "Can't send message to self", Toast.LENGTH_SHORT).show();
            return;
        }
        final String messageText = MessageInputText.getText().toString();
        MessageInputText.setText("");

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "type a message...", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
            final String messageSenderRefsend = "Messages/" + messageSenderID + "/" +"Send" + "/" + messageReceiverID;
            final String messageReceiverRefrecive = "Messages/" + messageReceiverID + "/" +"Receive" + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("isseen", false);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageSenderRefsend + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRefrecive + "/" + messagePushID, messageTextBody);




            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                        Date currentTime = Calendar.getInstance().getTime();

                        RootRef.child("Users").child(messageSenderID).child("date")
                                .setValue(currentTime)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {

                                        }
                                    }
                                });

                        RootRef.child("Users").child(messageReceiverID).child("date")
                                .setValue(currentTime)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {

                                        }
                                    }
                                });


                        new SendNotificationUtil().sendNotifiaction(getApplicationContext(), messageReceiverID,
                                myUserName, messageText, messageSenderID,phone,messageReceiverImage,nameu);
                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });

            setUserLastMessage(messageTextBody, messagePushID);
        }
    }


    public void refreshChatList() {
        messagesList.clear();
        userMessagesList.getAdapter().notifyDataSetChanged();
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    /*public void sendNotifiaction(final String receiverId, final String username, final String message, final String senderId){

        Query query = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot sp : snapshot.getChildren()){
                    Contacts ct = sp.getValue(Contacts.class);

                    assert ct != null;
                    Data data = new Data(senderId, R.drawable.appicon, username+": "+message, "New Message",
                            receiverId);

                    Sender sender = new Sender(data, ct.getDevice_token());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(ChatActivity.this, "Notification Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(ChatActivity.this, "Notification Sent!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/
    void voiceRecorder() {
        record_view.setOnRecordListener(new OnRecordListener() {
                                            @Override
                                            public void onStart() {
                                                //Start Recording..
                                                inboxLayout.setVisibility(View.GONE);
                                                startRecording();
//                Log.d("RecordView", "onStart");
                                            }

                                            @Override
                                            public void onCancel() {
                                                //On Swipe To Cancel
//                Log.d("RecordView", "onCancel");

                                                inboxLayout.setVisibility(View.VISIBLE);

                                            }

                                            @Override
                                            public void onFinish(long recordTime) {
                                                inboxLayout.setVisibility(View.VISIBLE);
                                                stopRecording();
                                                File bytesInput = new File(fileName);
                                                sendVoice(bytesInput);
                                                //Stop Recording..
//                String time = getHumanTimeText(recordTime);
//                Log.d("RecordView", "onFinish");
//
//                Log.d("RecordTime", time);
                                            }

                                            @Override
                                            public void onLessThanSecond() {
                                                //When the record time is less than One Second
                                                 Log.d("RecordView", "onLessThanSecond");
                                                Handler handler = new Handler();
                                                Runnable runnable = new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        inboxLayout.setVisibility(View.VISIBLE);
                                                    }
                                                };
                                                handler.postDelayed(runnable, 150);
                                            }
                                        });


                //ListenForRecord must be false ,otherwise onClick will not be called
//        record_button.setOnRecordClickListener(new OnRecordClickListener() {
//            @Override
//            public void onClick(View v) {
////                Toast.makeText(MainActivity.this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show();
////                Log.d("RecordButton","RECORD BUTTON CLICKED");
//            }
//        });

//        record_view?.setOnRecordListener(object : OnRecordListener {
//            override fun onStart() {
//                Utills.encodedVoice = ""
//                editText?.visibility = View.INVISIBLE
//                voicePlayerLayout?.visibility = View.GONE
//                startRecording()
//                timerVoice = object : CountDownTimer(60000, 1000) {
//                    override fun onTick(millisUntilFinished: Long) {
//                    }
//
//                    override fun onFinish() {
//                        record_button?.visibility = View.GONE
//
//                    }
//                }
//                (timerVoice as CountDownTimer).start()
//            }
//
//            override fun onCancel() {
//            }
//
//            override fun onFinish(recordTime: Long) {
//                voicePlayerLayout?.visibility = View.VISIBLE
//                record_button?.visibility = View.GONE
//                editText?.visibility = View.VISIBLE
//                stopRecording()
//                voicePlayerView?.setAudio(fileName)
//                val bytes = FileUtils.readFileToByteArray(File(fileName))
//                Utills.encodedVoice = Base64.encodeToString(bytes, 0)
//                if (timerVoice != null)
//                    (timerVoice as CountDownTimer).cancel()
//            }
//
//            override fun onLessThanSecond() {
//                editText?.visibility = View.VISIBLE
//                stopRecording()
//                if (timerVoice != null)
//                    (timerVoice as CountDownTimer).cancel()
//
//            }
//        })
//
//        record_view?.setOnBasketAnimationEndListener(OnBasketAnimationEnd {
//            if (timerVoice != null)
//                (timerVoice as CountDownTimer).cancel()
//            val handler = Handler()
//            val runnable = Runnable {
//
//                editText?.visibility = View.VISIBLE
//
//            }
//            handler.postDelayed(
//                    runnable, 300
//            )
//        })

    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
        recorder.setAudioSamplingRate(44100);
        recorder.setAudioEncodingBitRate(96000);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }

        recorder.start();

    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    //    override fun onStop() {
//        super.onStop()
//        recorder?.release()
//        recorder = null
//        player?.release()
//        player = null
//    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            {
                record_button.setListenForRecord(true);
                break;
            }

        }
        if (!permissionToRecordAccepted) {

        }

    }

    private Boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if ((ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED)
            ) {
                return true;
            } else {
                requestPermissions(
                        permissions,
                        1
                );
//                activity?.let {requestPermissions(it, arrayOf<String>(Manifest.permission.CALL_PHONE), 1) }
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }


    private void sendVoice(File file) {

        loadingBar.setTitle("Sending file");
        loadingBar.setMessage("Please wait, we are sending your file...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        fileUri = (Uri.fromFile(file));

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
        final String messageSenderRefsend = "Messages/" + messageSenderID + "/" +"Send" + "/" + messageReceiverID;
        final String messageReceiverRefrecive = "Messages/" + messageReceiverID + "/" +"Receive" + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();

        final String messagePushID = userMessageKeyRef.getKey();

        final StorageReference filePath = storageReference.child(messagePushID + "." + checker);
        filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        Map messageFileBody = new HashMap();
                        messageFileBody.put("message", downloadUrl);
                        messageFileBody.put("name", fileUri.getLastPathSegment());
                        messageFileBody.put("type", "audio");
                        messageFileBody.put("from", messageSenderID);
                        messageFileBody.put("to", messageReceiverID);
                        messageFileBody.put("messageID", messagePushID);
                        messageFileBody.put("time", saveCurrentTime);
                        messageFileBody.put("date", saveCurrentDate);
                        messageFileBody.put("isseen", false);
                        Map messageBodyDetail = new HashMap();
                        messageBodyDetail.put(messageSenderRef + "/" + messagePushID, messageFileBody);
                        messageBodyDetail.put(messageReceiverRef + "/" + messagePushID, messageFileBody);
                        messageBodyDetail.put(messageSenderRefsend + "/" + messagePushID, messageFileBody);
                        messageBodyDetail.put(messageReceiverRefrecive + "/" + messagePushID, messageFileBody);

                        RootRef.updateChildren(messageBodyDetail);
                        loadingBar.dismiss();
                        new SendNotificationUtil().sendNotifiaction(getApplicationContext(), messageReceiverID,
                                myUserName, "File", messageSenderID,phone,messageReceiverImage,nameu);
                        setUserLastMessage(messageFileBody,messagePushID);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                loadingBar.setMessage((int) p + " % Uploading...");
            }
        });
    }

    @Override
    public void onvideoClick(String url) {
        MKPlayerActivity.configPlayer(this).play(url);
//        MxVideoPlayerWidget.startFullscreen(this, MxVideoPlayerWidget.class, url, "");
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

    /*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub

        MenuInflater menuINF= getMenuInflater();
        menuINF.inflate(R.menu.menu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.block:
                blockUser();
                break;
        }

        return true;
    }*/
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    // TODO Auto-generated method stub

    MenuInflater menuINF= getMenuInflater();
    menuINF.inflate(R.menu.menu, menu);
    itemBlock = menu.findItem(R.id.block);
    itemUnblock = menu.findItem(R.id.unblock);
    addContact = menu.findItem(R.id.addcontact);

    BlockRef.child(messageSenderID).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()) {
                System.out.println("block1");
                String status = snapshot.child("status").getValue().toString();
                System.out.println("sssssssassssssss"+status);
                if (status.equals("false"))
                {
                    if (itemUnblock.isVisible())
                    {

                        itemUnblock.setVisible(true);
                    }
                    if (itemBlock.isVisible())
                    {
                        itemBlock.setVisible(false);
                    }
                }
                else if (status.equals("true"))
                {
                    if (itemUnblock.isVisible())
                    {

                        itemUnblock.setVisible(false);
                    }
                    if (itemBlock.isVisible())
                    {
                        itemBlock.setVisible(false);
                    }

                }

            } else {
                System.out.println("block2");

                if (itemUnblock.isVisible())
                {
                    itemUnblock.setVisible(false);
                }
                if (!itemBlock.isVisible())
                {
                    itemBlock.setVisible(true);
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    if (!contactExists(ChatActivity.this,phone).equals("")){
        addContact.setVisible(false);
    }
    else {
        addContact.setVisible(true);
    }

    return true;
}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.block: {
                if (itemBlock.isVisible()) {
                    itemBlock.setVisible(false);
                }
                if (!itemUnblock.isVisible()) {
                    itemUnblock.setVisible(true);
                }
                blockUser();
                break;
              }
            case R.id.unblock: {
                if (!itemBlock.isVisible()) {
                    itemBlock.setVisible(true);
                }
                if (itemUnblock.isVisible()) {
                    itemUnblock.setVisible(false);
                }
                unblockUser();
                break;
            }
            case  R.id.addcontact:
            {
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                        .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
                startActivity(intent);
                break;
            }

        }

        return true;
    }

    private void blockUser(){
        Map hashmap1 = new HashMap<>();
        hashmap1.put("id",messageReceiverID);
        hashmap1.put("status",false);

        final Map hashmap2 = new HashMap<>();
        hashmap2.put("id",messageSenderID);
        hashmap2.put("status",true);

        RootRef.child("Blocks").child(messageSenderID).child(messageReceiverID).setValue(hashmap1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                RootRef.child("Blocks").child(messageReceiverID).child(messageSenderID).setValue(hashmap2).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                });
            }
        });
    }

    private void unblockUser(){

        RootRef.child("Blocks").child(messageSenderID).child(messageReceiverID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()){
                    RootRef.child("Blocks").child(messageReceiverID).child(messageSenderID).removeValue();
                    Toast.makeText(ChatActivity.this, "unblock user successful", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
