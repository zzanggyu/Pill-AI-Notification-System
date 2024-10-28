package com.example.finalpillapp.API;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

public class RetrofitClientInstance {
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit;
    public static final String BASE_URL = "http://121.132.196.27:5001/api/v1/";

    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                Log.d(TAG, "API Log: " + message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            Log.d(TAG, "Sending request to: " + request.url());

                            Response response = chain.proceed(request);

                            // 응답 바디를 안전하게 처리
                            ResponseBody originalBody = response.body();
                            if (originalBody == null) {
                                return response;
                            }

                            // 응답 바디를 문자열로 읽기
                            String bodyString = originalBody.string();

                            // 새로운 응답 바디 생성
                            ResponseBody newBody = ResponseBody.create(
                                    originalBody.contentType(),
                                    bodyString
                            );

                            // 새로운 응답 생성
                            return response.newBuilder()
                                    .body(newBody)
                                    .build();
                        }
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }

    public static <T> void logApiResponse(retrofit2.Response<T> response, String apiName) {
        if (response.isSuccessful()) {
            Log.d(TAG, String.format(Locale.US, "%s API call successful: %s",
                    apiName, response.body()));
        } else {
            try {
                String errorBody = response.errorBody() != null ?
                        response.errorBody().string() : "No error body";
                Log.e(TAG, String.format(Locale.US, "%s API call failed: %d, Error: %s",
                        apiName, response.code(), errorBody));
            } catch (IOException e) {
                Log.e(TAG, String.format(Locale.US, "%s API call failed: %d, Couldn't read error body",
                        apiName, response.code()), e);
            }
        }
    }
}