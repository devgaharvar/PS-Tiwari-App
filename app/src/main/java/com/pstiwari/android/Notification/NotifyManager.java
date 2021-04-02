package com.pstiwari.android.Notification;

import android.util.Log;

import com.pstiwari.android.calls.Global;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class NotifyManager {
    public static void MessageNotificationToUser(String token, String isAndroid, Message message) {
        message.setnType("message");
        if (isAndroid.equals(Global.Device_Type_Android)) {
            Log.wtf("bakrr","androidSend");
            RootModel_Android<Message> root = new RootModel_Android<>(token, message);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendAndroidMessageNotification(root);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });
        } else {
            Log.wtf("bakrr","iOsSend");
            RootModel_iOS<Message> root = new RootModel_iOS<>(token, message);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendiOSMessageNotification(root);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });

        }
    }

    public static void MessageGroupNotificationToUser(String token, String isAndroid, Message message) {
        message.setnType("messageGroup");
        if (isAndroid.equals(Global.Device_Type_Android)) {
            RootModel_Android<Message> root = new RootModel_Android<>(token, message);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendAndroidMessageNotification(root);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });
        } else {
            RootModel_iOS<Message> root = new RootModel_iOS<>(token, message);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendiOSMessageGroupNotification(root);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });

        }
    }


    public static void VoiceCallNotificationToUser(String token, String isAndroid, CallNotification callNotification) {
        callNotification.setnType("voiceCall");
        if (isAndroid.equals(Global.Device_Type_Android)) {
            RootModel_Android<CallNotification> root = new RootModel_Android<>(token, callNotification);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendAndroidVoiceCallNotification(root);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });
        } else {
            RootModel_iOS<CallNotification> root = new RootModel_iOS<>(token, callNotification);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendiOSVoiceCallNotification(root);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });

        }
    }

    public static void VideoCallNotificationToUser(String token, String isAndroid, CallNotification callNotification) {
        callNotification.setnType("videoCall");
        if (isAndroid.equals(Global.Device_Type_Android)) {
            RootModel_Android<CallNotification> root = new RootModel_Android<>(token, callNotification);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendAndroidVoiceCallNotification(root);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });
        } else {
            RootModel_iOS<CallNotification> root = new RootModel_iOS<>(token, callNotification);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendiOSVoiceCallNotification(root);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });

        }
    }
    public interface listeners{
        void onResponse();
        void onFailure();
    }
}
