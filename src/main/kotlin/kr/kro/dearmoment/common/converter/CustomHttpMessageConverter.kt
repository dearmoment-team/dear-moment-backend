package kr.kro.dearmoment.common.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

class CustomHttpMessageConverter(
    objectMapper: ObjectMapper,
) : MappingJackson2HttpMessageConverter(objectMapper) {
    init {
        // 지원하는 미디어 타입 확장 - multipart 내부 JSON 처리를 위해 octet-stream 포함
        supportedMediaTypes =
            listOf(
                MediaType.APPLICATION_JSON,
                MediaType("application", "*+json"),
                MediaType.APPLICATION_OCTET_STREAM, // multipart 내부 JSON 처리용
            )
    }

    override fun supports(clazz: Class<*>): Boolean {
        // 1. 기본 패키지 처리: 우리 애플리케이션 패키지는 무조건 처리
        if (clazz.`package`?.name?.startsWith("kr.kro.dearmoment") == true) {
            return true
        }

        // 2. 스웨거 관련 클래스도 처리 가능하도록 추가 확인
        // 스웨거와의 호환성을 위해 기본 타입도 처리
        return isBasicType(clazz)
    }

    // 기본 타입 확인 (스웨거가 주로 사용하는 타입들)
    private fun isBasicType(clazz: Class<*>): Boolean {
        return clazz == String::class.java ||
            clazz == Int::class.java || clazz == Integer::class.java ||
            clazz == Long::class.java ||
            clazz == Boolean::class.java ||
            clazz == Double::class.java ||
            clazz == Float::class.java ||
            Map::class.java.isAssignableFrom(clazz) ||
            Collection::class.java.isAssignableFrom(clazz)
    }
}
