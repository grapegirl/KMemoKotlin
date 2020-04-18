package momo.kikiplus.refactoring.net;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("mobile/update.jsp")
    /**
     * 공지 리스트 URL
     */
    Call<NoticeList> getUpdateList();

    @POST("mobile/version.jsp")
    /**
     * 버전 업데이트 URL
     */
    Call<Version> getVersion(@Query("version") String version);

    @GET("mobile/category.jsp")
    /**
     * 카데고리 목록 가져오기 URL
     */
    Call<CategoryList> getCateryList();

}
