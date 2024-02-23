// SubtitleService.kt

package gdsc.allways.allears.presentation.subtitles

import gdsc.allways.allears.dto.SubtitleCreateRequestDto
import gdsc.allways.allears.dto.SubtitleListResponseDto
import gdsc.allways.allears.dto.SubtitleResponseDto
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SubtitleService {

    // POST 요청으로 새로운 자막 생성
    @POST("/subtitle")
    fun createSubtitle(@Query("identity") identity: String, @Body subtitleText: String): Call<SubtitleCreateRequestDto>

    // GET 요청으로 모든 자막 리스트 반환
    @GET("/subtitle")
    fun getAllSubtitles(@Query("identity") identity: String): Call<SubtitleListResponseDto>

    // GET 요청으로 특정 자막 가져오기
    @GET("/subtitle/{id}")
    fun getSubtitleById(@Path("id") id: Long, @Query("identity") identity: String): Call<SubtitleResponseDto>

    companion object {
        private const val BASE_URL = "http://35.216.80.118:8080"

        fun create(): SubtitleService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(SubtitleService::class.java)
        }
    }
}
