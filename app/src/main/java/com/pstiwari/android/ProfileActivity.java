package com.pstiwari.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String senderUserID, receiverUserID;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton;

    String phone;
    private DatabaseReference UserRef, ContactsRef;
    private FirebaseAuth mAuth;

    private String muserImage = "default_image";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        UserRef.keepSynced(true);
        ContactsRef.keepSynced(true);
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();


        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        phone=getIntent().getExtras().get("visit_phone").toString();

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists())  &&  (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    muserImage = userImage;
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();
                    if(!userImage.isEmpty()) {
                        Picasso.get().load(userImage).placeholder(R.drawable.dp2).into(userProfileImage);
                    } else {
                        userProfileImage.setImageResource(R.drawable.dp2);
                    }
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userstatus);

                    sendMessage();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userstatus);

                    sendMessage();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        if (!senderUserID.equals(receiverUserID))
        {
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {




                    Intent chatIntent = new Intent(ProfileActivity.this, ChatActivity.class);
                    chatIntent.putExtra("visit_user_id",receiverUserID);
                    chatIntent.putExtra("visit_user_name",userProfileName.getText().toString());
                    chatIntent.putExtra("visit_image",muserImage);
                    chatIntent.putExtra("visit_phone",phone);
                    chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(chatIntent);
                    ProfileActivity.this.finish();

                    ////
               /*     ContactsRef.child(senderUserID).child(receiverUserID)
                            .child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        ContactsRef.child(receiverUserID).child(senderUserID)
                                                .child("Contacts").setValue("Saved")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            Intent chatIntent = new Intent(ProfileActivity.this, ChatActivity.class);
                                                            chatIntent.putExtra("visit_user_id",receiverUserID);
                                                            chatIntent.putExtra("visit_user_name",userProfileName.getText().toString());
                                                            chatIntent.putExtra("visit_image",muserImage);
                                                            chatIntent.putExtra("visit_phone",phone);
                                                            chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(chatIntent);
                                                            ProfileActivity.this.finish();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
*/
                }
            });
        }
        else
        {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

}