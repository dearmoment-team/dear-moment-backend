package kr.kro.dearmoment.common.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import java.nio.charset.StandardCharsets

class CustomHttpMessageConverter(
    objectMapper: ObjectMapper,
) : MappingJackson2HttpMessageConverter(objectMapper) {
    init {
        // Kotlin 데이터 클래스 지원: Swagger 오류 해결에도 도움이 됨
        objectMapper.registerModule(kotlinModule())
        // 지원하는 미디어 타입을 JSON으로 한정
        supportedMediaTypes = listOf(MediaType.APPLICATION_JSON)
        setObjectMapper(objectMapper)
    }

    override fun canWrite(mediaType: MediaType?): Boolean = super.canWrite(mediaType)

    override fun writeInternal(
        `object`: Any,
        outputMessage: HttpOutputMessage,
    ) {
        // /v3/api-docs처럼 String 타입이면 그대로 출력하도록 처리
        if (`object` is String) {
            val bytes = `object`.toByteArray(StandardCharsets.UTF_8)
            outputMessage.body.write(bytes)
        } else {
            super.writeInternal(`object`, outputMessage)
        }
    }
}
