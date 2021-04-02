package com.pstiwari.android.calls;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.annotations.NotNull;
import com.pstiwari.android.Contacts;
import com.pstiwari.android.ContactsFragment;
import com.pstiwari.android.MainActivity;
import com.pstiwari.android.ProfileActivity;
import com.pstiwari.android.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactnewAdpter extends RecyclerView.Adapter<ContactnewAdpter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<Contacts> mContacts;
    private List<Contacts> contactListFiltered;


    public ContactnewAdpter(Context c, List<Contacts> contacts) {
        this.mContext = c;
        this.mContacts = contacts;
        this.contactListFiltered = contacts;
    }

    @NotNull
    @Override
    public ContactnewAdpter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.contact_csutome_list, parent, false);
        return new ContactnewAdpter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ContactnewAdpter.ViewHolder holder, int position) {
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
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                MainActivity.shouldSkipUpdateStatus = true;
                profileIntent.putExtra("visit_user_id", visit_user_id);
                profileIntent.putExtra("visit_phone",phone);
                mContext.startActivity(profileIntent);
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
