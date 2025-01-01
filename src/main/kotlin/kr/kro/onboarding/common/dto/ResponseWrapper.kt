package kr.kro.onboarding.common.dto

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RestControllerAdvice
class ResponseWrapper : ResponseBodyAdvice<Any> {

    private val antPathMatcher = AntPathMatcher()

    // Swagger/OpenAPI 관련 엔드포인트 패턴 리스트
    private val swaggerPaths = listOf(
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/swagger-config/**",
        "/api/docs/**"
    )

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        // JSON 컨버터인지 확인
        val isJsonConverter = MappingJackson2HttpMessageConverter::class.java.isAssignableFrom(converterType)
        print("Is JSON Converter: $isJsonConverter\n")
        if (!isJsonConverter) {
            return false
        }

        // 현재 요청의 HttpServletRequest 가져오기
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        if (requestAttributes == null) {
            print("RequestAttributes is null\n")
            return false
        }

        val request = requestAttributes.request
        if (request == null) {
            print("HttpServletRequest is null\n")
            return false
        }

        val uri = request.requestURI
        print("Request URI: $uri\n")

        // Swagger/OpenAPI 관련 엔드포인트인지 확인
        val isSwaggerPath = swaggerPaths.any { pattern ->
            antPathMatcher.match(pattern, uri)
        }
        print("Is Swagger Path: $isSwaggerPath\n")

        val shouldWrap = !isSwaggerPath
        print("Should Wrap: $shouldWrap\n")

        return shouldWrap
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        val servletResponse = (response as ServletServerHttpResponse).servletResponse
        val status = servletResponse.status
        val resolvedStatus = HttpStatus.resolve(status)

        print("HTTP Status: $status, Resolved Status: $resolvedStatus\n")

        return if (resolvedStatus?.is2xxSuccessful == true) {
            print("Wrapping response with success=true\n")
            BaseResponse(success = true, code = status, data = body)
        } else {
            print("Not wrapping response due to non-2xx status\n")
            body
        }
    }
}
