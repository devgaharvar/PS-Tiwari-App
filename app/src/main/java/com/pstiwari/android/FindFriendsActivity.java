package com.pstiwari.android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private EditText searchEdt;
    private FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter = null;
    private DatabaseReference UsersRef,BlockRef;
    private String namefilter = "";
    private String currentUserID = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mAuth = FirebaseAuth.getInstance();
        try {
            currentUserID = mAuth.getCurrentUser().getUid();
        } catch (NullPointerException ignored) {
        }

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        BlockRef = FirebaseDatabase.getInstance().getReference().child("Blocks");
        UsersRef.keepSynced(true);
        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        searchEdt = (EditText) findViewById(R.id.search);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1 ) {
                    namefilter = "";
                } else {
                    namefilter = s.toString();
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public boolean contactExists(Activity _activity, String number) {
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
    protected void onStart() {
        super.onStart();

        findContacts();

    }

    private void findContacts() {

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UsersRef, Contacts.class)
                        .build();

//        if (!s.isEmpty()) {
//                Query q = UsersRef.orderByChild("name").startAt(s).endAt("~");
//         options = new FirebaseRecyclerOptions.Builder<Contacts>()
//                            .setQuery( q, Contacts.class)
//                            .build();
//        }

        adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FindFriendsViewHolder holder, final int position, @NonNull final Contacts model) {

                BlockRef.child(currentUserID).child(model.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            holder.itemView.setVisibility(View.GONE);
                            setVisibility(false, holder.itemView);
                        } else {
                            holder.itemView.setVisibility(View.VISIBLE);
                            setVisibility(true, holder.itemView);
                            getUserData(holder, position, model);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            private void getUserData(@NonNull FindFriendsViewHolder holder, final int position, @NonNull final Contacts model) {
                if (!namefilter.isEmpty() && model.name.toLowerCase().contains(namefilter.toLowerCase())) {
                    if (contactExists(FindFriendsActivity.this, model.phone)) {
                        holder.itemView.setVisibility(View.VISIBLE);
                        setVisibility(true, holder.itemView);

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        if (!model.getImage().isEmpty()) {
                            Picasso.get().load(model.getImage()).placeholder(R.drawable.dp2).into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.dp2);
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(position).getKey();


                                String visit_user_phone=model.getPhone();

                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                profileIntent.putExtra("visit_phone", visit_user_phone);
                                startActivity(profileIntent);
                            }
                        });
                    } else {
                        holder.itemView.setVisibility(View.GONE);
                        setVisibility(false, holder.itemView);
                    }
                }else if (namefilter.isEmpty()){
                    if (contactExists(FindFriendsActivity.this, model.phone)) {
                        holder.itemView.setVisibility(View.VISIBLE);
                        setVisibility(true, holder.itemView);

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        if(!model.getImage().isEmpty()) {
                            Picasso.get().load(model.getImage()).placeholder(R.drawable.dp2).into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.dp2);
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(position).getKey();
                                String visit_user_phone=model.getPhone();
                                System.out.println("visssssssssssssssssssss"+visit_user_phone);
                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                profileIntent.putExtra("visit_phone", visit_user_phone);
                                startActivity(profileIntent);
                            }
                        });
                    } else {
                        holder.itemView.setVisibility(View.GONE);
                        setVisibility(false, holder.itemView);
                    }
                }else {
                    holder.itemView.setVisibility(View.GONE);
                    setVisibility(false, holder.itemView);
                }
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
    }




    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        RelativeLayout main_layout;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            main_layout = itemView.findViewById(R.id.main_layout);
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


}
