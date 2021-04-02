package com.pstiwari.android.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pstiwari.android.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pstiwari.android.MainActivity;
import com.pstiwari.android.R;
import com.pstiwari.android.SettingsActivity;
import com.pstiwari.android.calls.Global;
import com.pstiwari.android.calls.IncAudioActivity;
import com.pstiwari.android.calls.IncCallActivity;
import com.pstiwari.android.calls.Usercalldata;
import com.pstiwari.android.calls.encryption;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    String notificationId[];
    boolean deleted, online;
    int oneTimeId;
    private DatabaseReference RootRef;
    Context conn;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mlogs;
    String senderId;
    String senderName;
    String senderAva;
    String Mid;
    ValueEventListener listner;
    String to;
    String message;
    String channelId;
    String callerId;
    String callerName;
    String callerAva, type;
    int[] noUnread = {0};

    PendingIntent pIntent;
    TaskStackBuilder stackBuilder;
    //notifi id
    String notifiID[];

    @Override
    public void onNewToken(String token) {
        try {
            if (mAuth.getCurrentUser() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("tokens", token);
                DatabaseReference mToken = FirebaseDatabase.getInstance().getReference(Global.tokens);
                mToken.child(mAuth.getCurrentUser().getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
                sendRegistrationToServer(token);
            }
        } catch (NullPointerException e) {

        }
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        conn = this;
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        RootRef = FirebaseDatabase.getInstance().getReference();
        if (remoteMessage.getData() != null) {
            //get data

            String sented = remoteMessage.getData().get("sented");
            String user = remoteMessage.getData().get("user");

            System.out.println("dsssssssssssssssssss"+remoteMessage.getData());
            System.out.println("Dddsaaaaaaaaf"+remoteMessage);
            Map<String,String> map = remoteMessage.getData();
            Log.wtf("RCVDNOTIFICATION",map +"");
            String type = map.get("nType");
            if (type.equals("oldtype"))
            {
                Oldnoti(sented,user,remoteMessage);
            }
            else
            {
                newNotify(map);
            }

        }

        // Check if message contains a data payload.
        /*if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleNow(remoteMessage.getData().get("body"));
        }*/

        // Check if message contains a notification payload.
        /*if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNow(remoteMessage.getNotification().getBody());
        }*/



        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    public void Oldnoti(String sented, String user, RemoteMessage remoteMessage)
    {
        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = preferences.getString("currentuser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sented.equals(firebaseUser.getUid())){
            if (!currentUser.equals(user)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage);
                } else {
                    sendNotification(remoteMessage);
                }
            }
        }
    }



    /**
     * New notifications
     */
    void newNotify(Map<String, String> map) {
        Log.wtf("tawgeh", type);
        String type = map.get("nType");
        String senderId = map.get("senderId");
        if (!TextUtils.isEmpty(senderId)) {
                String Mid = map.get("Mid");
                notificationId = Mid.split("_");
                oneTimeId = (int) Long.parseLong(notificationId[2]);
                switch (type) {
                    case "message":
                        if (mAuth.getCurrentUser().getUid() != null)
                            Log.wtf("tawgeh", type);
                        GetMessage(map);
                        break;
                        case "voiceCall":
                        Log.wtf("tawgeh", type);
                        Call(map);
                        break;
                    case "videoCall":
                        Log.wtf("tawgeh", type);
                        Call(map);
                        break;


                }

        }

    }
    void GetMessage(Map<String, String> map) {

        String react;
        senderId = map.get("senderId");
        senderName = map.get("senderName");
        senderAva = map.get("senderAva");
        Mid = map.get("Mid");
        to = map.get("to");
        message = map.get("message");
        react = map.get("react");

        message = encryption.decryptOrNull(message);
        Log.wtf("react", react);
        Log.wtf("react", message);
        online = false;
        tawgeh();
    }

    public void tawgeh() {
        //go activity
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.putExtra("name", senderName);
        intent.putExtra("id", senderId);
        intent.putExtra("ava", senderAva);
        intent.putExtra("codetawgeh", 1);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        pIntent = PendingIntent.getActivity(this, 11, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        //if chat app is not running
        if (!online) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        CustomNotAPI25(message, senderName, oneTimeId);
                    }
                    else
                    {
                        CustomNot(message, senderName, oneTimeId);
                    }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CustomNotAPI25(String body, String string, int i) {
        int color =  Color.BLUE;
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationChann notificationChann = new NotificationChann(getBaseContext(), color, sound);
        Notification.Builder builder = notificationChann.getPLAXNot(string, body, pIntent, sound);
        notificationChann.getManager().notify(i, builder.build());
    }

    public void CustomNot(String body, String title, int id) {

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(title)
                .setContentText(body)
                .setSound(sound)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.appicon)
                .setOngoing(false)
                .setContentIntent(pIntent);
        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, builder.build());
    }

    void Call(Map<String, String> map) {
        type = map.get("nType");
        senderId = map.get("senderId");
        senderName = map.get("senderName");
        senderAva = map.get("senderAva");
        Mid = map.get("Mid");
        to = map.get("to");
        message = map.get("message");
        channelId = encryption.decryptOrNull(map.get("channelId"));
        callerId = encryption.decryptOrNull(map.get("callerId"));
        callerName = encryption.decryptOrNull(map.get("callerName"));
        callerAva = encryption.decryptOrNull(map.get("callerAva"));




        if (mAuth.getCurrentUser() != null) {

            if (mAuth.getCurrentUser().getUid().equals(to)) {

                if (type.equals("videoCall")) {
                    System.out.println("ddsss"+callerName+""+callerAva+" "+callerId);
                    Intent jumptocall = new Intent(conn, IncCallActivity.class);
                    jumptocall.putExtra("name", callerName);
                    jumptocall.putExtra("ava", callerAva);
                    jumptocall.putExtra("out", false);
                    jumptocall.putExtra("channel_id", channelId);
                    jumptocall.putExtra("id", callerId);
                    jumptocall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(jumptocall);
                } else {
                    Intent jumptocall = new Intent(conn, IncAudioActivity.class);
                    jumptocall.putExtra("name", callerName);
                    jumptocall.putExtra("ava", callerAva);
                    jumptocall.putExtra("out", false);
                    jumptocall.putExtra("channel_id", channelId);
                    jumptocall.putExtra("id", callerId);
                    jumptocall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(jumptocall);
                }
              /*  mcall.child(mAuth.getCurrentUser().getUid()).child(callerId).child(channelId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Usercalldata usercalldata = dataSnapshot.getValue(Usercalldata.class);
                            System.out.println("ddddddd"+usercalldata.isIncall());
                            //if (usercalldata.isIncall()) {

                        //        }
                        *//*else {
                                    final Map<String, Object> map = new HashMap<>();
                                    map.put("incall", false);
                                    mlogs.child(mAuth.getCurrentUser().getUid()).child(callerId).child(channelId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mlogs.child(callerId).child(mAuth.getCurrentUser().getUid()).child(channelId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                        }
                                    });
                                }*//*

                        } catch (NullPointerException e) {
                            final Map<String, Object> map = new HashMap<>();
                            map.put("incall", false);
                            mlogs.child(mAuth.getCurrentUser().getUid()).child(callerId).child(channelId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mlogs.child(callerId).child(mAuth.getCurrentUser().getUid()).child(channelId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    });
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
*/

            }
        }


    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */

    // [END on_new_token]


    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param /token The new token.
     */
    private void sendRegistrationToServer(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("device_token").setValue(refreshToken);
        }
    }


    /**
     * Handle time allotted to BroadcastReceivers.
     */
    /*private void handleNow(final String data) {
        Log.d(TAG, "Short lived task is done.");
        sendNotification(data);
    }*/


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param /messageBody FCM message body received.
     */
    /*private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Dr. PS Tiwari")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 , notificationBuilder.build());
    }*/

    private void sendOreoNotification(RemoteMessage remoteMessage){
        String user1 = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        if(ChatActivity.notificationRefId != null && user1.equals(ChatActivity.notificationRefId)){
            return;
        }
       // String phone=remoteMessage.getData().get("phone");
    //    String image=remoteMessage.getData().get("image");
        //String nameu=remoteMessage.getData().get("name");
        ////////

        RootRef.child("Users").child(user1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {

                            String user = remoteMessage.getData().get("user");
                            String icon = remoteMessage.getData().get("icon");
                            String title = remoteMessage.getData().get("title");
                            String body = remoteMessage.getData().get("body");

                            String nameu = dataSnapshot.child("name").getValue().toString();
                            String phone = dataSnapshot.child("phone").getValue().toString();
                            String image = dataSnapshot.child("image").getValue().toString();
                            System.out.println("dddddadddddddddf"+phone);
                            RemoteMessage.Notification notification = remoteMessage.getNotification();
                            int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
                            Intent intent = new Intent(conn, ChatActivity.class);
                            intent.putExtra("visit_user_id",user);
                            intent.putExtra("visit_phone",phone);
                            intent.putExtra("visit_image",image);
                            intent.putExtra("visit_user_name",nameu);
                            intent.putExtras(intent);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pendingIntent = PendingIntent.getActivity(conn, j, intent, PendingIntent.FLAG_ONE_SHOT);
                            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            OreoNotification oreoNotification = new OreoNotification(conn);
                            Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                                    defaultSound, icon);
                            int i = 0;
                            if (j > 0){
                                i = j;
                            }

                            oreoNotification.getManager().notify(i, builder.build());

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String user = remoteMessage.getData().get("user");
                            String icon = remoteMessage.getData().get("icon");
                            String title = remoteMessage.getData().get("title");
                            String body = remoteMessage.getData().get("body");
                            String nameu = dataSnapshot.child("name").getValue().toString();
                            String phone = dataSnapshot.child("phone").getValue().toString();
                            String image = "";
                            System.out.println("dddddadddddddddf"+phone);
                            RemoteMessage.Notification notification = remoteMessage.getNotification();
                            int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
                            Intent intent = new Intent(conn, ChatActivity.class);
                            intent.putExtra("visit_user_id",user);
                            intent.putExtra("visit_phone",phone);
                            intent.putExtra("visit_image",image);
                            intent.putExtra("visit_user_name",nameu);
                            intent.putExtras(intent);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pendingIntent = PendingIntent.getActivity(conn, j, intent, PendingIntent.FLAG_ONE_SHOT);
                            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            OreoNotification oreoNotification = new OreoNotification(conn);
                            Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                                    defaultSound, icon);
                            int i = 0;
                            if (j > 0){
                                i = j;
                            }

                            oreoNotification.getManager().notify(i, builder.build());

                        }
                        else
                        {
                            Toast.makeText(conn, "Please update your profile...", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        ////////

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user1 = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        if(ChatActivity.notificationRefId != null && user1.equals(ChatActivity.notificationRefId)){
            return;
        }

        RootRef.child("Users").child(user1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {


                            String user = remoteMessage.getData().get("user");
                            String icon = remoteMessage.getData().get("icon");
                            String title = remoteMessage.getData().get("title");
                            String body = remoteMessage.getData().get("body");
                            String nameu = dataSnapshot.child("name").getValue().toString();
                            String phone = dataSnapshot.child("phone").getValue().toString();
                            String image = dataSnapshot.child("image").getValue().toString();
                            System.out.println("dddddadddddddddf"+phone);

                            RemoteMessage.Notification notification = remoteMessage.getNotification();
                            int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
                            Intent intent = new Intent(conn, ChatActivity.class);
                            intent.putExtra("visit_user_id",user);
                            intent.putExtra("visit_phone",phone);
                            intent.putExtra("visit_image",image);
                            intent.putExtra("visit_user_name",nameu);
        /*Bundle bundle = new Bundle();
        bundle.putString("visit_user_id", user);
        bundle.putString("visit_phone",phone);*/
                            intent.putExtras(intent);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pendingIntent = PendingIntent.getActivity(conn, j, intent, PendingIntent.FLAG_ONE_SHOT);
                            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(conn)
                                    .setSmallIcon(Integer.parseInt(icon))
                                    .setContentTitle(title)
                                    .setContentText(body)
                                    .setAutoCancel(true)
                                    .setSound(defaultSound)
                                    .setContentIntent(pendingIntent);
                            NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                            int i = 0;
                            if (j > 0){
                                i = j;
                            }

                            noti.notify(i, builder.build());


                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String user = remoteMessage.getData().get("user");
                            String icon = remoteMessage.getData().get("icon");
                            String title = remoteMessage.getData().get("title");
                            String body = remoteMessage.getData().get("body");
                            String nameu = dataSnapshot.child("name").getValue().toString();
                            String phone = dataSnapshot.child("phone").getValue().toString();
                            String image = "";
                            System.out.println("dddddadddddddddf"+phone);

                            RemoteMessage.Notification notification = remoteMessage.getNotification();
                            int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
                            Intent intent = new Intent(conn, ChatActivity.class);
                            intent.putExtra("visit_user_id",user);
                            intent.putExtra("visit_phone",phone);
                            intent.putExtra("visit_image",image);
                            intent.putExtra("visit_user_name",nameu);
        /*Bundle bundle = new Bundle();
        bundle.putString("visit_user_id", user);
        bundle.putString("visit_phone",phone);*/
                            intent.putExtras(intent);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pendingIntent = PendingIntent.getActivity(conn, j, intent, PendingIntent.FLAG_ONE_SHOT);
                            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(conn)
                                    .setSmallIcon(Integer.parseInt(icon))
                                    .setContentTitle(title)
                                    .setContentText(body)
                                    .setAutoCancel(true)
                                    .setSound(defaultSound)
                                    .setContentIntent(pendingIntent);
                            NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                            int i = 0;
                            if (j > 0){
                                i = j;
                            }

                            noti.notify(i, builder.build());

                        }
                        else
                        {
                            Toast.makeText(conn, "Please update your profile...", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



    }
}