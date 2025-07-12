package kr.kro.dearmoment.common.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Oracle 은 빈 문자열("")을 NULL 로 저장한다.
 * - DB 에 저장할 때: 공백·빈 문자열이면 NULL 로 저장
 * - DB 에서 읽어올 때: NULL 이면 " " 한 칸으로 치환
 */
@Converter(autoApply = false)
class BlankToSpaceConverter : AttributeConverter<String, String> {
    // 저장 시: null·빈 문자열이면 " " 한 칸으로 채움
    override fun convertToDatabaseColumn(attribute: String?): String = attribute?.takeIf { it.isNotBlank() } ?: " "

    // 조회 시: null 이 내려오는 일은 없지만 혹시 모를 상황 대비
    override fun convertToEntityAttribute(dbData: String?): String = dbData?.ifBlank { " " } ?: " "
}
