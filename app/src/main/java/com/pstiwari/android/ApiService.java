package com.pstiwari.android;

import com.pstiwari.android.Notification.MyResponse;
import com.pstiwari.android.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA5eoEY_Q:APA91bHmLZvmMnZfuvQbi61erb2zOoI1XsYAT9sJlvW9NSB6vKoz06Z-KL3edksShSeHSYvsu-Oh56pf_uCxpH6TZqLkGNuLUQ9dce5YEehQxFpY4Vzg-3xI_kiNjXbDV4KPHqu3uD85"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
