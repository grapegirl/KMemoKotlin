package momo.kikiplus.com.kbucket.action.net

import momo.kikiplus.com.kbucket.data.finally.NetworkConst
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetRetrofit {

    // 파싱등록
    val service: RetrofitService
        get() {

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


            val retrofit = Retrofit.Builder()
                    .baseUrl(NetworkConst.KBUCKET_SERVER_IP)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            return retrofit.create(RetrofitService::class.java)
        }

    companion object {

        val instance = NetRetrofit()
    }

}
