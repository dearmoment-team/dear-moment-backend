package kr.kro.dearmoment.common.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

class CustomHttpMessageConverter(
    objectMapper: ObjectMapper,
) : MappingJackson2HttpMessageConverter(objectMapper) {
    init {
        objectMapper.registerModule(kotlinModule())
        supportedMediaTypes = listOf(MediaType.APPLICATION_JSON)
        setObjectMapper(objectMapper)
    }

    override fun canWrite(mediaType: MediaType?): Boolean = super.canWrite(mediaType)
}
