package gdsc.allways.allears.dto

import com.google.gson.annotations.SerializedName

data class SubtitleCreateRequestDto(
    @SerializedName("identity")
    val identity: String,
    @SerializedName("subtitleText")
    val subtitleText: String
)
