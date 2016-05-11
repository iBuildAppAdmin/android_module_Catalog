package com.ibuildapp.romanblack.CataloguePlugin.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class CatalogApiService {
    private  String baseUrl  =  "";

    public CatalogApiService(String domain){
        baseUrl = domain;
        if(!baseUrl.contains("http"))
            baseUrl = "http://"+baseUrl;

        baseUrl+='/';
    }

    public  CatalogApi getCatalogApi(){

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build().create(CatalogApi.class);
    }
}
