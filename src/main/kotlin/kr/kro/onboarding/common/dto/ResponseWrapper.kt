package kr.kro.onboarding.common.dto

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

@RestControllerAdvice("kr.kro.onboarding.api")
class ResponseWrapper : ResponseBodyAdvice<Any> {
    private val antPathMatcher = AntPathMatcher()

    // Swagger/OpenAPI 등 파일 리소스를 제외할 경로들
    private val swaggerPaths =
        listOf(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/swagger-config/**",
            "/api/docs/**",
        )

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean {
        // 디버그 로그
        println("[DEBUG] ResponseWrapper.supports() 호출됨: returnType=$returnType, converterType=$converterType")
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
        // 디버그 로그
        println("[DEBUG] ResponseWrapper.beforeBodyWrite() 진입")
        println("[DEBUG] 요청 path: ${request.uri.path}")
        println("[DEBUG] 현재 body: $body")

        val path = request.uri.path

        // Swagger나 정적 리소스 관련 요청이면 래핑(감싸기) 스킵
        if (swaggerPaths.any { antPathMatcher.match(it, path) }) {
            println("[DEBUG] $path 은(는) swaggerPaths에 매칭됨 → 래핑 스킵")
            return body
        }

        val servletResponse = (response as ServletServerHttpResponse).servletResponse
        val status = servletResponse.status
        val resolvedStatus = HttpStatus.resolve(status)

        // 2xx 응답이면 BaseResponse로 감싸기
        return if (resolvedStatus?.is2xxSuccessful == true) {
            println("[DEBUG] 상태 코드 $status → 2xx 응답. BaseResponse로 래핑")
            BaseResponse(success = true, code = status, data = body)
        } else {
            println("[DEBUG] 상태 코드 $status → 2xx 이 아님. 래핑 생략")
            body
        }
    }
}
