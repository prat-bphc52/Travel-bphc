package com.crux.pratd.travelbphc;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by pratd on 25-02-2018.
 */

public interface ApiInterface {

    @POST("request")
    Call<JsonObject> sendReq(@Body JsonObject json);

    @POST("accept")
    Call<JsonObject> acceptReq(@Body JsonObject json);

    @POST("leave")
    Call<JsonObject> leavePlan(@Body JsonObject json);
}
