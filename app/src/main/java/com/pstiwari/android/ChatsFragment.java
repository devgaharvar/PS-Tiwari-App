package com.pstiwari.android;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pstiwari.android.Model.ChatListModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.pstiwari.android.ContactsFragment.contactExists;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment{

    private View PrivateChatsView;
    private RecyclerView chatsList;
    private EditText search;
    private DatabaseReference ChatsRef, UsersRef, BlockRef;
    private FirebaseAuth mAuth;
    private String currentUserID = "";
    private String name;
    private List<Contacts> mContacts;
    ChatListAdapter chatListAdapter;
    List<ChatListModel> chatdata;
    List<String> useridss;
    private ValueEventListener chatlistner;
    private ValueEventListener mesglistner;
    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        chatdata=new ArrayList<>();
        mContacts = new ArrayList<>();
        useridss=new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        search =  PrivateChatsView.findViewById(R.id.search);
        try {
            currentUserID = mAuth.getCurrentUser().getUid();
        } catch (NullPointerException ignored) {
        }


        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        BlockRef = FirebaseDatabase.getInstance().getReference().child("Blocks");
        ChatsRef.keepSynced(true);
        UsersRef.keepSynced(true);


        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout(false);
        manager.setStackFromEnd(false);
        chatsList.setLayoutManager(manager);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (chatListAdapter!=null)
                {
                    chatListAdapter.getFilter().filter(s);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return PrivateChatsView;
    }



    @Override
    public void onStart() {
        super.onStart();
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
    /*    Query q = ChatsRef.orderByChild("key");
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(q, Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};
                        BlockRef.child(currentUserID).child(usersIDs).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    holder.itemView.setVisibility(View.GONE);
                                    setVisibility(false, holder.itemView);
                                } else {
                                    holder.itemView.setVisibility(View.VISIBLE);
                                    setVisibility(true, holder.itemView);
                                    getUserData(holder,usersIDs,retImage);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }


                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();
*/


        chatlistner=ChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                useridss.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    contactsModel user = snapshot.getValue(contactsModel.class);
                    final String userid=snapshot.getKey();
                    useridss.add(userid);
                    System.out.println("dddddddddddddd"+userid);
                }
                ChatsRef.removeEventListener(chatlistner);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { ChatsRef.removeEventListener(chatlistner);}});

        mesglistner=UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mContacts.clear();
                for (DataSnapshot sp : snapshot.getChildren()) {
                    final Contacts ct = sp.getValue(Contacts.class);
                    System.out.println("2222222222222222222222222222");
                    if (useridss.contains(ct.uid))
                    {
                        System.out.println("0000000000000000000000");
                        mContacts.add(ct);
                    }

                }
                Collections.sort(mContacts, Collections.reverseOrder());
                chatListAdapter=new ChatListAdapter(getActivity(),mContacts);
                chatsList.setAdapter(chatListAdapter);
                chatListAdapter.notifyDataSetChanged();
                UsersRef.removeEventListener(mesglistner);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                UsersRef.removeEventListener(mesglistner);
            }
        });


////////////////////////////////////////////////////////////////////////////////////////////////////
    }
    public void refreshApi(){
        //write the code here to refresh your Api
    }
    private void getUserData(@NonNull final ChatsViewHolder holder, final String usersIDs, final String[] retImage) {
        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
//                                    if(dataSnapshot.child("block").exists() && dataSnapshot.child("block"))
                    if (dataSnapshot.hasChild("image")) {
                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                        if(!retImage[0].isEmpty()) {
                            Picasso.get().load(retImage[0]).placeholder(R.drawable.dp2).into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.dp2);
                        }
                    } else {
                        holder.profileImage.setImageResource(R.drawable.dp2);
                    }

                    final String retName = dataSnapshot.child("name").getValue().toString();

                     /*   if(retName.equals("maria"))
                        {
                            holder.itemView.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(),"found",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            holder.itemView.setVisibility(View.GONE);
                        }*/


                    final String retStatus = dataSnapshot.child("status").getValue().toString();
                    holder.userName.setText(retName);

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

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                            chatIntent.putExtra("visit_user_id", usersIDs);
                            chatIntent.putExtra("visit_user_name", retName);
                            chatIntent.putExtra("visit_image", retImage[0]);
                            MainActivity.shouldSkipUpdateStatus = true;
                            startActivity(chatIntent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    ////////


///////////

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


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userStatus, userName;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
        }
    }
}
