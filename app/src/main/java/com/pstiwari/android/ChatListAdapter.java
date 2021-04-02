package com.pstiwari.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pstiwari.android.Model.ChatListModel;
import com.pstiwari.android.Model.OnRecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> implements Filterable {

    private Activity mContext;
    CallsFragment fragment;
    private List<Contacts> chatdata;
    private List<Contacts> mContacts;
    List<Messages> messages;
    OnRecyclerViewItemClickListener listener;
    private double totalBal;
    double totalBl;
    private String senderMessageId, receiverMessageId;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private String currentUserID = "";
    private DatabaseReference ChatsRef, UsersRef, BlockRef, messgaRe;
    public ChatListAdapter(Activity mContext, List<Contacts> chatdata) {
        this.mContext = mContext;
        this.chatdata = chatdata;
        this.mContacts = chatdata;
    }

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.users_display_layout, parent, false);
        return new ChatListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListAdapter.ViewHolder holder, final int position) {
        Collections.sort(mContacts, Collections.reverseOrder());
        final Contacts chatListModel = chatdata.get(position);
        messages = new ArrayList<>();
        String phon = chatListModel.getPhone();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        senderMessageId = currentUserID;

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        messgaRe = FirebaseDatabase.getInstance().getReference().child("Messages");

        BlockRef = FirebaseDatabase.getInstance().getReference().child("Blocks");

    /*    BlockRef.child(currentUserID).child(chatListModel.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    holder.itemView.setVisibility(View.GONE);
                    setVisibility(false, holder.itemView);
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    setVisibility(true, holder.itemView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        UsersRef.child(chatListModel.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
//                                    if(dataSnapshot.child("block").exists() && dataSnapshot.child("block"))
                    if (dataSnapshot.child("userState").hasChild("state")) {
                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                        if (state.equals("online")) {
                            holder.userStatus.setText("online");
                        } else if (state.equals("offline")) {
                            holder.userStatus.setText("Last Seen: " + date + " " + time);
                        }
                    } else {
                        holder.userStatus.setText("offline");
                    }
                    if (dataSnapshot.child("lastMessage").exists()) {
                        if (dataSnapshot.child("lastMessage").child(currentUserID).exists()) {
                            if (dataSnapshot.child("lastMessage").child(currentUserID).child("messageID").exists()) {
                                Messages message = dataSnapshot.child("lastMessage").child(currentUserID).getValue(Messages.class);
                                if (message.getType().equals("text")) {
                                    holder.userStatus.setText(message.getMessage());
                                } else {
                                    holder.userStatus.setText(message.getType());
                                }
                                if (message.getIsseen() == false && !message.getFrom().equals(currentUserID)) {
                                    holder.userStatus.setTextColor(Color.parseColor("#005048"));
                                }
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        try {
            messgaRe.child(currentUserID).child("Receive").child(chatListModel.uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    messages.clear();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Messages messages2 = snapshot.getValue(Messages.class);
                            if (messages2.getIsseen() == false) {
                                messages.add(messages2);
                            }
                        }

                        if (messages.size() == 0) {
                            holder.tvcounter.setText("");
                        } else {
                            holder.tvcounter.setText("" + messages.size());
                        }
                        System.out.println("kkkkkkkkkkddddddd" + messages.size());

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!chatListModel.getImage().isEmpty()) {
            Picasso.get().load(chatListModel.getImage()).placeholder(R.drawable.dp2).into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.dp2);
        }

        if (!contactExists(mContext, phon).equals("")) {
            holder.userName.setText(contactExists(mContext, phon));

        } else {
            holder.userName.setText(chatListModel.getPhone());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(mContext, ChatActivity.class);
                chatIntent.putExtra("visit_user_id", chatListModel.uid);
                chatIntent.putExtra("visit_user_name", chatListModel.name);
                chatIntent.putExtra("visit_image", chatListModel.image);
                chatIntent.putExtra("visit_phone", chatListModel.phone);
                MainActivity.shouldSkipUpdateStatus = true;
                mContext.startActivity(chatIntent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dial = new AlertDialog.Builder(v.getContext());
                dial.setMessage("Are you sure you want to delete this Chat?").setCancelable(false).
                        setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                receiverMessageId = chatListModel.getUid();
                                clearAllChat(senderMessageId, receiverMessageId);
                               // chatdata.remove(position);
                                Toast.makeText(mContext, "Chat deleted", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });
                AlertDialog alert = dial.create();
                alert.setTitle("Deleting Chat");
                alert.show();
                return true;
            }
        });
    }
    private void clearAllChat(final String senderMsgId, final String receiverMsgId) {
//        deleteAllMessage();
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages").child(senderMsgId).child(receiverMsgId).removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        RootRef.child("Contacts").child(senderMsgId).child(receiverMsgId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //done
                                Intent intent = new Intent(mContext, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mContext.startActivity(intent);

                            }
                        });
                    }
                });


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
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    chatdata = mContacts;
                } else {
                    List<Contacts> filteredList = new ArrayList<>();
                    for (Contacts row : mContacts) {

                        if (row.name.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    chatdata = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = chatdata;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                chatdata = (ArrayList<Contacts>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
    @Override
    public int getItemCount() {
        return chatdata.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userStatus, userName, tvcounter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
            tvcounter = itemView.findViewById(R.id.tvcounter);

        }


    }


}