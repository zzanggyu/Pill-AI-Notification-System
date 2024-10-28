package com.example.finalpillapp.API;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://58.122.4.162:5001/api/v1/";

    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // 로그 인터셉터 추가 (디버그용)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // OkHttpClient에 타임아웃 설정과 로그 인터셉터 추가
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)  // 연결 타임아웃: 60초
                    .writeTimeout(5, TimeUnit.SECONDS)    // 쓰기 타임아웃: 60초
                    .readTimeout(5, TimeUnit.SECONDS)     // 읽기 타임아웃: 60초
                    .addInterceptor(loggingInterceptor)    // 로그 인터셉터 추가
                    .build();

            // OkHttpClient를 Retrofit에 추가
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)  // OkHttpClient 설정 추가
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // API 서비스 인스턴스를 반환하는 메서드 추가
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
}
