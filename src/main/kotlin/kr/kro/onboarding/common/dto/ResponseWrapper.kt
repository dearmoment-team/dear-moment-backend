package kr.kro.onboarding.common.dto

import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice("kr.kro.onboarding.api")
class ResponseWrapper : ResponseBodyAdvice<Any> {
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean {
        return MappingJackson2HttpMessageConverter::class.java.isAssignableFrom(converterType)
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        val servletResponse = (response as ServletServerHttpResponse).servletResponse
        val status = servletResponse.status
        val resolvedStatus = HttpStatus.resolve(status)

        return if (resolvedStatus?.is2xxSuccessful == true) {
            BaseResponse.success(data = body)
        } else {
            print("Not wrapping response due to non-2xx status\n")
            BaseResponse.error(code = status)
        }
    }
}
