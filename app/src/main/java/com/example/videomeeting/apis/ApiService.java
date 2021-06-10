package com.example.videomeeting.apis;

import com.example.videomeeting.utils.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    /**
     * Send the call FCM push notification through the remote server
     * @param remoteBody Notification data string
     */
    @Headers({"Authorization: key=" + Constants.REMOTE_SERVER_KEY, "Content-Type:application/json"})
    @POST("send")
    Call<String> sendRemoteMessage(
            @Body String remoteBody
    );

    /**
     * Send the message FCM push notification through the remote server
     * @param notification Notification data string
     */
    @Headers({"Authorization: key=" + Constants.REMOTE_SERVER_KEY, "Content-Type:application/json"})
    @POST("send")
    Call<ResponseBody> sendNotification(
            @Body String notification
    );
}
