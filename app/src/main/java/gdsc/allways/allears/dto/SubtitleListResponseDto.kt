package gdsc.allways.allears.dto
import java.util.*
import com.google.gson.annotations.SerializedName

data class SubtitleListResponseDto(
    val subtitleResponseDtoList: List<SubtitleResponseDto>
)

data class SubtitleResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("createdDate")
    val createdDate: String,
    @SerializedName("createdTime")
    val createdTime: String,
    @SerializedName("subtitleText")
    val subtitleText: String
)
