package momo.kikiplus.com.kbucket.net;

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
}
