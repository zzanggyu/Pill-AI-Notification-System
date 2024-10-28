package com.example.finalpillapp.API;

import com.example.finalpillapp.MyPage.PersonalInfoActivity;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.RecognizePill.PillImageRequest;
import com.example.finalpillapp.legalnotice.LegalNoticeRequest;
import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // 기존 PillApp의 메서드들
    @Headers("Content-Type: application/json")
    @POST("analyze_pill")
    Call<ApiResponse<List<PillInfo>>> analyzePill(@Body PillImageRequest request);

    @POST("/legal-notice")
    Call<ApiResponse<Void>> sendLegalNotice(@Body LegalNoticeRequest request);

    @GET("/check-legal-notice")
    Call<ApiResponse<Void>> checkLegalNotice(@Query("userId") String userId);

    // 이름으로 검색하는 메서드 - PillInfo 반환
    @GET("/pills/searchByName")
    Call<ApiResponse<List<PillInfo>>> searchPillsByName(@Query("itemName") String itemName);

    // 증상으로 검색하는 메서드 - PillInfo 반환
    @GET("/pills/search")
    Call<ApiResponse<List<PillInfo>>> searchPillsBySymptom(@Query("symptom") String symptom, @Query("selectedSymptoms") List<String> selectedSymptoms);

    /*@POST("/pills/add")
    Call<ApiResponse<Void>> addPill(@Body AddPillRequest request);

    @POST("/pills/delete")
    Call<ApiResponse<Void>> deletePill(@Body DeletePillRequest request);*/

    @POST("/personal-info/save")
    Call<ApiResponse<Void>> savePersonalInfo(@Body PersonalInfoActivity request);

    /*@POST("/personal-info/reset")
    Call<ApiResponse<Void>> resetPersonalInfo(@Body UserIdRequest request);*/

    @GET("/getDrugInteractions")
    Call<ApiResponse<Void>> getDrugInteractions(@Query("drugItemName") String drugItemName);

    @GET("/personal-info")
    Call<ApiResponse<Void>> getPersonalInfo(@Query("userId") String userId);

    // 추가된 Search_DB_API의 메서드들
    @GET("/api/v1/pills/search")
    Call<ApiResponse<List<PillInfo>>> searchPills(
            @Query("symptom") String symptom,
            @Query("selectedSymptoms") List<String> selectedSymptoms
    );



    @POST("/api/v1/pills/add")
    Call<ResponseBody> addPill(@Body JsonObject pillJson);

    @GET("api/v1/pills/{itemSeq}")
    Call<PillInfo> getPillById(@Path("itemSeq") int itemSeq);
}
