package com.pstiwari.android.Notification;


import com.pstiwari.android.calls.Global;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {

    @Headers({"Authorization: key=" + Global.Server_Key, "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendAndroidMessageNotification(@Body RootModel_Android<Message> root);
    @Headers({"Authorization: key=" + Global.Server_Key, "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendAndroidMessageGroupNotification(@Body RootModel_Android<Message> root);
    @Headers({"Authorization: key=" + Global.Server_Key, "Content-Type:application/json"})

    @POST("fcm/send")
    Call<ResponseBody> sendAndroidVoiceCallNotification(@Body RootModel_Android<CallNotification> root);
    @Headers({"Authorization: key=" + Global.Server_Key, "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendAndroidVideoCallNotification(@Body RootModel_Android<CallNotification> root);
    @Headers({"Authorization: key=" + Global.Server_Key, "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendiOSMessageNotification(@Body RootModel_iOS<Message> root);
    @Headers({"Authorization: key=" + Global.Server_Key, "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendiOSMessageGroupNotification(@Body RootModel_iOS<Message> root);
    @Headers({"Authorization: key=" + Global.Server_Key, "Content-Type:application/json"})

    @POST("fcm/send")
    Call<ResponseBody> sendiOSVoiceCallNotification(@Body RootModel_iOS<CallNotification> root);
    @Headers({"Authorization: key=" + Global.Server_Key, "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendiOSVideoCallNotification(@Body RootModel_iOS<CallNotification> root);


}

