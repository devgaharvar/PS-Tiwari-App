package com.pstiwari.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.pstiwari.android.call.PlaceCallActivity;
import com.pstiwari.android.calls.CallingActivity;
import com.pstiwari.android.calls.CallingActivityVideo;
import com.pstiwari.android.calls.calls;
import com.pstiwari.android.common.Utill;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import de.hdodenhof.circleimageview.CircleImageView;

public class LogcallAdaptor extends RecyclerView.Adapter<LogcallAdaptor.ViewHolder> {
    ArrayList<calls> CallLogList;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private Activity mContext ;
    String called = "";
    ValueEventListener blocklistner;
    private DatabaseReference BlockRef,UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID = "";
    public LogcallAdaptor(Activity context, ArrayList<calls> callLogList) {
        this.mContext = context;
        CallLogList = callLogList;
    }
    @NonNull
    @Override
    public LogcallAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EmojiManager.install(new IosEmojiProvider());
        View view = LayoutInflater.from(mContext).inflate( R.layout.call_row, parent, false);
        return new LogcallAdaptor.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final LogcallAdaptor.ViewHolder holder, int position) {


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        calls calls = CallLogList.get(position);
        BlockRef = FirebaseDatabase.getInstance().getReference().child("Blocks");
        System.out.println("Seconds: ");
        int tot_seconds = (int) calls.getDur();
        int hours = tot_seconds / 3600;
        int minutes = (tot_seconds % 3600) / 60;
        int seconds = tot_seconds % 60;
        String timeString = String.format("%02d:%02d:%02d ",hours,minutes, seconds);
        System.out.println(timeString);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        try {

            if (calls.getTo().equals(mAuth.getCurrentUser().getUid())) {
                UserRef.child(calls.getFrom()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()))
                        {
                            if ((dataSnapshot.hasChild("image")))
                            {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                if(!userImage.isEmpty()) {
                                    Picasso.get().load(userImage).placeholder(R.drawable.dp2).into(holder.profileImage);
                                } else {
                                    holder.profileImage.setImageResource(R.drawable.dp2);
                                }
                            }
                            else
                            {
                                holder.profileImage.setImageResource(R.drawable.dp2);
                            }
                            String userName = dataSnapshot.child("name").getValue().toString();

                            Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(calls.getFrom());
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot sp : snapshot.getChildren()) {
                                        Contacts ct = sp.getValue(Contacts.class);
                                        assert ct != null;
                                        System.out.println("ssafgg44444444"+ct.getPhone());
                                        if (!contactExists(mContext,ct.getPhone()).equals("")){
                                            holder.userName.setText(contactExists(mContext,ct.getPhone()));
                                        }else {
                                            holder.userName.setText(ct.getPhone());
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

/*

            if(!calls.getAva().isEmpty()) {
                Picasso.get().load(calls.getAva()).placeholder(R.drawable.dp2).into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.dp2);
            }
            Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(calls.getTo());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        Contacts ct = sp.getValue(Contacts.class);
                        assert ct != null;
                        if (contactExists(mContext,ct.getPhone())){
                            holder.userName.setText(calls.getName());
                        }else {
                            holder.userName.setText(ct.getPhone());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });*/
            }
            else {
                UserRef.child(calls.getTo()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()))
                        {
                            if ((dataSnapshot.hasChild("image")))
                            {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                if(!userImage.isEmpty()) {
                                    Picasso.get().load(userImage).placeholder(R.drawable.dp2).into(holder.profileImage);
                                } else {
                                    holder.profileImage.setImageResource(R.drawable.dp2);
                                }
                            }
                            else
                            {
                                holder.profileImage.setImageResource(R.drawable.dp2);
                            }

                            String userName = dataSnapshot.child("name").getValue().toString();

                            Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(calls.getTo());
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot sp : snapshot.getChildren()) {
                                        Contacts ct = sp.getValue(Contacts.class);
                                        assert ct != null;
                                        System.out.println("2ssssssssssssc"+ct.getPhone());
                                        if (!contactExists(mContext,ct.getPhone()).equals("")){
                                            holder.userName.setText(contactExists(mContext,ct.getPhone()));
                                        }else {
                                            holder.userName.setText(ct.getPhone());
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

        } catch (Exception e) {
            e.printStackTrace();
        }




        holder.time.setText(getTimeAgo(calls.getTime(), mContext));

        if (mAuth.getCurrentUser() != null)
        {
            try {
                if (calls.getFrom().equals(mAuth.getCurrentUser().getUid())) {
                    holder.state.setImageResource(R.drawable.ic_outc);
                }
                else {
                    try {
                        if (calls.getDur() == 0)
                            holder.state.setImageResource(R.drawable.ic_miss);

                        else
                            holder.state.setImageResource(R.drawable.ic_in);
                    } catch (NullPointerException e) {
                        holder.state.setImageResource(0);
                    }
                }
            }
            catch (NullPointerException e)
            {

            }
        }

        holder.audiocall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!calls.getFrom().equals(mAuth.getCurrentUser().getUid())) {
                    called = calls.getFrom();
                }
                else {
                    called = calls.getTo();
                }
                blocklistner=BlockRef.child(currentUserID).child(calls.getTo()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                String status = snapshot.child("status").getValue().toString();
                                System.out.println("sssssssassssssss"+status);
                                if (status.equals("false"))
                                {
                                    Toast.makeText(mContext, "you have blocked this user can't make call", Toast.LENGTH_SHORT).show();
                                    BlockRef.child(currentUserID).child(calls.getTo()).removeEventListener(blocklistner);
                                }
                                else if (status.equals("true"))
                                {
                                    Toast.makeText(mContext, "This user has blocked you can't make call", Toast.LENGTH_SHORT).show();
                                    BlockRef.child(currentUserID).child(calls.getTo()).removeEventListener(blocklistner);
                                }
                            }
                            else
                                {
                                String callid = mAuth.getCurrentUser().getUid()+ System.currentTimeMillis();
                                Intent jumptocall = new Intent(mContext, CallingActivity.class);
                                jumptocall.putExtra("name", calls.getName());
                                jumptocall.putExtra("ava", calls.getAva());
                                jumptocall.putExtra("out", true);
                                jumptocall.putExtra("history","true");
                                jumptocall.putExtra("channel_id", callid);
                                jumptocall.putExtra("UserId", called);
                                jumptocall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(jumptocall);
                                    BlockRef.child(currentUserID).child(calls.getTo()).removeEventListener(blocklistner);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            BlockRef.child(currentUserID).child(calls.getTo()).removeEventListener(blocklistner);
                        }
                    });
                }

        });

        holder.videocall.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!calls.getFrom().equals(mAuth.getCurrentUser().getUid())) {
                    called = calls.getFrom();
                }
                else {
                    called = calls.getTo();
                }
                    blocklistner=BlockRef.child(currentUserID).child(calls.getTo()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                String status = snapshot.child("status").getValue().toString();
                                System.out.println("sssssssassssssss"+status);
                                if (status.equals("false"))
                                {
                                    Toast.makeText(mContext, "you have blocked this user can't make call", Toast.LENGTH_SHORT).show();
                                    BlockRef.child(currentUserID).child(calls.getTo()).removeEventListener(blocklistner);
                                }
                                else if (status.equals("true"))
                                {
                                    Toast.makeText(mContext, "This user has blocked you can't make call", Toast.LENGTH_SHORT).show();
                                    BlockRef.child(currentUserID).child(calls.getTo()).removeEventListener(blocklistner);
                                }

                            }
                            else {

                                String callid = mAuth.getCurrentUser().getUid() + System.currentTimeMillis();
                                Intent jumptocall = new Intent(mContext, CallingActivityVideo.class);
                                jumptocall.putExtra("UserId", called);
                                jumptocall.putExtra("channel_id", callid);
                                jumptocall.putExtra("ava", calls.getAva());
                                jumptocall.putExtra("name", calls.getName());
                                jumptocall.putExtra("history","true");
                                jumptocall.putExtra("id", called);
                                jumptocall.putExtra("out", true);
                                mContext.startActivity(jumptocall);
                                BlockRef.child(currentUserID).child(calls.getTo()).removeEventListener(blocklistner);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            BlockRef.child(currentUserID).child(calls.getTo()).removeEventListener(blocklistner);
                        }
                    });

                }

        } );



    }
    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }
        long now = System.currentTimeMillis();


        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return ctx.getResources().getString(R.string.now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return ctx.getResources().getString(R.string.minute);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " " + ctx.getResources().getString(R.string.min_ago);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return ctx.getResources().getString(R.string.hour);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " " + ctx.getResources().getString(R.string.hours);
        } else if (diff < 48 * HOUR_MILLIS) {
            return ctx.getResources().getString(R.string.yesterday);
        } else {
            return diff / DAY_MILLIS + " " + ctx.getResources().getString(R.string.days);
        }
    }

    public void setVisibility(boolean isVisible, View itemView) {
        RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        if (isVisible) {
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            param.width = LinearLayout.LayoutParams.MATCH_PARENT;
            itemView.setVisibility(View.VISIBLE);
        } else {
            itemView.setVisibility(View.GONE);
            param.height = 0;
            param.width = 0;
        }
        itemView.setLayoutParams(param);
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
    public int getItemCount() {
        return CallLogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView profileImage;
        ImageView videocall,audiocall,state;
        EmojiTextView userName,time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById( R.id.user_image);
            time = itemView.findViewById( R.id.time);
            userName = itemView.findViewById( R.id.userName);
            audiocall = itemView.findViewById( R.id.audiocall);
            videocall = itemView.findViewById( R.id.videocall);
            state = itemView.findViewById(R.id.state);
        }
    }
}