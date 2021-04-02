package com.pstiwari.android.Notification;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pstiwari.android.ApiService;
import com.pstiwari.android.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendNotificationUtil {

    private ApiService apiService;


    public void sendNotifiaction(final Context c, final String receiverId, final String username, final String message, final String senderId,final String phone,String image,String nameu){
        apiService = Client.getClient("https://fcm.googleapis.com/").create(ApiService.class);
        final Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByKey().equalTo(receiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot sp : snapshot.getChildren()){
                    String devToken = sp.child("device_token").getValue().toString();
                    assert devToken != null;

                    System.out.println("xxxxxxdssssssss"+image+"ddf"+nameu);
                    Data data = new Data(senderId, R.drawable.appicon, username+": "+message, "New Message",
                            receiverId,"oldtype",phone,image,nameu);
                    Sender sender = new Sender(data, devToken);
                    //System.out.println("Sending notification");
                    //System.out.println("dev token ="+devToken);
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            //Toast.makeText(c, "Notification Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            //Toast.makeText(c, "Notification Sent!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                }
                            });
                    query.removeEventListener(this);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
