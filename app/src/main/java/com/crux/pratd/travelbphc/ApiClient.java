package com.crux.pratd.travelbphc;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pratd on 29-03-2018.
 */

class ApiClient {
    private static final String baseurl = "https://us-central1-travelbphc.cloudfunctions.net/app/";
    private static Retrofit retrofit = null;

    static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(baseurl).
                    addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
