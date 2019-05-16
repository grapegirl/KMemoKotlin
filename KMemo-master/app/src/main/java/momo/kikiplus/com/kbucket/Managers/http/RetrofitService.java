package momo.kikiplus.com.kbucket.Managers.http;

import momo.kikiplus.com.kbucket.Data.NoticeList;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RetrofitService {

    @GET("mobile/update.jsp")
    Call<NoticeList> getUpdateList();
}
