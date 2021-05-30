package com.example.videomeeting.apis;

import com.example.videomeeting.utils.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers({"Authorization: key=" + Constants.REMOTE_SERVER_KEY, "Content-Type:application/json"})
    @POST("send")
    Call<String> sendRemoteMessage(
            @Body String remoteBody
    );

    @Headers({"Authorization: key=" + Constants.REMOTE_SERVER_KEY, "Content-Type:application/json"})
    @POST("send")
    Call<ResponseBody> sendNotification(
            @Body String notification
    );
}
