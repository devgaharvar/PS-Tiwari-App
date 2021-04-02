package com.pstiwari.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.pstiwari.android.Notification.SendNotificationUtil;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ForwardContactsList extends AppCompatActivity {

    private RecyclerView FindFriendsRecyclerList;
    List<String> mIdList;
    private DatabaseReference UsersRef;
    private DatabaseReference BlockRef;

    private List<Contacts> mContacts;
    private ContactAdapter1 contactAdapter;
    private EditText search;
    private ValueEventListener contactlistner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_contacts_list);
        search =  findViewById(R.id.search);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        BlockRef = FirebaseDatabase.getInstance().getReference().child("Blocks");
        UsersRef.keepSynced(true);
        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.contacts_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mContacts = new ArrayList<>();
        mIdList=new ArrayList<>();
        System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy"+mContacts.size());
        getContacts();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (contactAdapter != null)
                    contactAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void getContacts() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        contactlistner=UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mContacts.clear();
                mIdList.clear();
                for (DataSnapshot sp : snapshot.getChildren()) {
                    final Contacts ct = sp.getValue(Contacts.class);
                    if (ct != null && firebaseUser != null && ct.getUid() != null && firebaseUser.getUid() != null) {
                        if (!ct.getUid().equals(firebaseUser.getUid())) {
                            if (contactExists(ForwardContactsList.this, ct.getPhone())) {
                                mContacts.add(ct);
                                mIdList.add(ct.getUid());
                                contactAdapter = new ContactAdapter1(ForwardContactsList.this, mContacts);
                                FindFriendsRecyclerList.setAdapter(contactAdapter);
                            }

                        }
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                UsersRef.removeEventListener(contactlistner);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk"+mContacts.size());
        /*
        final FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UsersRef, Contacts.class)
                        .build();




        final FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.dp2).into(holder.profileImage);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(position).getKey();

                                Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };



        FindFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();

        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for(int i=0;i<adapter.getSnapshots().size();i++){
                        if(!contactExists(getActivity(),adapter.getSnapshots().get(i).getPhone())){
                            //options.getSnapshots().remove(i);
                        }
                    }
                    FindFriendsRecyclerList.getAdapter().notifyDataSetChanged();
                }
                else{
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
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

/*
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }*/


    public class ContactAdapter1 extends RecyclerView.Adapter<ContactAdapter1.ViewHolder> implements Filterable {

        private Activity mContext;
        private List<Contacts> mContacts;
        private List<Contacts> contactListFiltered;

        DatabaseReference RootRef;
        public ContactAdapter1(Activity c, List<Contacts> contacts) {
            this.mContext = c;
            this.mContacts = contacts;
            this.contactListFiltered = contacts;
        }

        @NotNull
        @Override
        public ContactAdapter1.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.contact_csutome_list, parent, false);
            return new ContactAdapter1.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull ContactAdapter1.ViewHolder holder, int position) {
            final Contacts contact = contactListFiltered.get(position);
            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            holder.userName.setText(contact.getName());
            holder.userStatus.setText(contact.getStatus());
            if(!contact.getImage().isEmpty()) {
                Picasso.get().load(contact.getImage()).placeholder(R.drawable.dp2).into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.dp2);
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String visit_user_id = contact.getUid();
                    String phone=contact.getPhone();
                  /*  Intent profileIntent = new Intent(ForwardContactsList.this, ProfileActivity.class);
                    MainActivity.shouldSkipUpdateStatus = true;
                    profileIntent.putExtra("visit_user_id", visit_user_id);
                    profileIntent.putExtra("visit_phone",phone);
                    startActivity(profileIntent);*/
                    Messages userMessagesList= (Messages) getIntent().getSerializableExtra("messagelist");
                    System.out.println("dddddddddds"+userMessagesList.getMessage()+"df");
                     forwardMessage(userMessagesList,visit_user_id,firebaseUser.getUid(),phone);
                }
            });
        }
        private void forwardMessage(final Messages messages, final String messageReceiverID, final String messageSenderId,String phone) {
            RootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
            ContactsRef.keepSynced(true);

            ContactsRef.child(messageSenderId).child(messageReceiverID)
                    .child("Contacts").setValue("Saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //contact added
                            }
                        }
                    });

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderId).child(messageReceiverID).push();

            final String messagePushID = userMessageKeyRef.getKey();

            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            String saveCurrentDate = currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            String saveCurrentTime = currentTime.format(calendar.getTime());

            Map messageFileBody = new HashMap();
            messageFileBody.put("message", messages.getMessage());
            messageFileBody.put("name", messages.getName());
            messageFileBody.put("type", messages.getType());
            messageFileBody.put("from", messageSenderId);
            messageFileBody.put("to", messageReceiverID);
            messageFileBody.put("messageID", messagePushID);
            messageFileBody.put("time", saveCurrentTime);
            messageFileBody.put("date", saveCurrentDate);
            messageFileBody.put("isseen", false);

            final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverID;
            final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderId;


            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(messageSenderRef + "/" + messagePushID, messageFileBody);
            messageBodyDetail.put(messageReceiverRef + "/" + messagePushID, messageFileBody);

            setUserLastMessage(messageFileBody,messagePushID, messageReceiverID, messageSenderId);

            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                     String myUserName=getIntent().getExtras().get("username").toString();
                        System.out.println("saaaaaaaaaaaaaaad"+myUserName+phone);
                        String messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
                        String nameu=getIntent().getExtras().get("visit_user_name").toString();
                        Toast.makeText(mContext, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                        new SendNotificationUtil().sendNotifiaction(mContext, messageReceiverID,
                                myUserName, messages.getType().equals("text") ? messages.getMessage() : "File", messageSenderId,phone,messageReceiverImage,nameu);
                    } else {
                        Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        private void setUserLastMessage(Map lastMessage, String messagePushID, final String forwardReceiverId, final String forwardSenderId ) {
            RootRef.child("Users").child(forwardSenderId).child("lastMessage").child(forwardReceiverId).setValue(lastMessage);
            RootRef.child("Users").child(forwardReceiverId).child("lastMessage").child(forwardSenderId).setValue(lastMessage);
            RootRef.child("Contacts").child(forwardSenderId).child(forwardReceiverId).child("key").setValue(messagePushID);
            RootRef.child("Contacts").child(forwardReceiverId).child(forwardSenderId).child("key").setValue(messagePushID);
        }

        @Override
        public int getItemCount() {
            return contactListFiltered.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView userName, userStatus;
            public CircleImageView profileImage;

            public ViewHolder(View itemView) {
                super(itemView);

                userName = itemView.findViewById(R.id.user_profile_name);
                userStatus = itemView.findViewById(R.id.user_status);
                profileImage = itemView.findViewById(R.id.users_profile_image);
            }
        }
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {

                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        contactListFiltered = mContacts;
                    } else {
                        List<Contacts> filteredList = new ArrayList<>();
                        for (Contacts row : mContacts) {

                            if (row.name.toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }

                        contactListFiltered = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = contactListFiltered;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                    contactListFiltered = (ArrayList<Contacts>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }

    }

}