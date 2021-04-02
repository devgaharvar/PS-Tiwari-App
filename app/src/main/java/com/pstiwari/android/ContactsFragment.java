package com.pstiwari.android;


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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView FindFriendsRecyclerList;

    private DatabaseReference UsersRef;
    private DatabaseReference BlockRef;

    private List<Contacts> mContacts;
    private ContactAdapter contactAdapter;
    private EditText search;
    private ValueEventListener contactlistner;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        search =  ContactsView.findViewById(R.id.search);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        BlockRef = FirebaseDatabase.getInstance().getReference().child("Blocks");
        UsersRef.keepSynced(true);
        FindFriendsRecyclerList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
        mContacts = new ArrayList<>();
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
        return ContactsView;
    }

    private void getContacts() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        contactlistner=UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mContacts.clear();
                for (DataSnapshot sp : snapshot.getChildren()) {
                    final Contacts ct = sp.getValue(Contacts.class);
                    if (ct != null && firebaseUser != null && ct.getUid() != null && firebaseUser.getUid() != null) {
                        if (!ct.getUid().equals(firebaseUser.getUid())) {
                            if (contactExists(getActivity(), ct.getPhone())) {
                                            mContacts.add(ct);
                                            contactAdapter = new ContactAdapter(getContext(), mContacts);
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


    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements Filterable {

        private Context mContext;
        private List<Contacts> mContacts;
        private List<Contacts> contactListFiltered;


        public ContactAdapter(Context c, List<Contacts> contacts) {
            this.mContext = c;
            this.mContacts = contacts;
            this.contactListFiltered = contacts;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.contact_csutome_list, parent, false);
            return new ContactAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            final Contacts contact = contactListFiltered.get(position);
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
                    Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                    MainActivity.shouldSkipUpdateStatus = true;
                    profileIntent.putExtra("visit_user_id", visit_user_id);
                    profileIntent.putExtra("visit_phone",phone);
                    startActivity(profileIntent);
                }
            });
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
