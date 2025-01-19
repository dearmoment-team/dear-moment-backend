import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.onboarding.common.dto.BaseResponse
import kr.kro.onboarding.common.dto.ResponseWrapper
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class ResponseWrapperTest : StringSpec({

    val responseWrapper = ResponseWrapper()
    val converter: Class<out HttpMessageConverter<*>> = MappingJackson2HttpMessageConverter::class.java

    "API 호출 시 응답 상태가 2xx 라면 BaseResponse.success()를 반환한다." {
        val body = mapOf("key" to "value")
        val method =
            ResponseWrapper::class.java.getMethod(
                "beforeBodyWrite",
                Any::class.java,
                MethodParameter::class.java,
                MediaType::class.java,
                Class::class.java,
                ServerHttpRequest::class.java,
                ServerHttpResponse::class.java,
            )
        val methodParameter = MethodParameter(method, 0)
        val mediaType: MediaType = MediaType.APPLICATION_JSON

        val mockResponse = MockHttpServletResponse()
        mockResponse.status = HttpStatus.OK.value() // 2xx 상태 코드

        val wrappedResponse =
            responseWrapper
                .beforeBodyWrite(
                    body,
                    methodParameter,
                    mediaType,
                    converter,
                    ServletServerHttpRequest(MockHttpServletRequest()),
                    ServletServerHttpResponse(mockResponse),
                )

        wrappedResponse shouldBe BaseResponse.success(data = body)
    }

    "API 호출 시 응답 상태가 2xx 아니라면 BaseResponse.error()를 반환한다." {
        val body = mapOf("error" to "Bad Request")
        val method =
            ResponseWrapper::class.java.getMethod(
                "beforeBodyWrite",
                Any::class.java,
                MethodParameter::class.java,
                MediaType::class.java,
                Class::class.java,
                ServerHttpRequest::class.java,
                ServerHttpResponse::class.java,
            )
        val methodParameter = MethodParameter(method, 0)
        val mediaType: MediaType = MediaType.APPLICATION_JSON

        val mockResponse = MockHttpServletResponse()
        mockResponse.status = HttpStatus.BAD_REQUEST.value() // 4xx 상태 코드

        val wrappedResponse =
            responseWrapper
                .beforeBodyWrite(
                    body,
                    methodParameter,
                    mediaType,
                    converter,
                    ServletServerHttpRequest(MockHttpServletRequest()),
                    ServletServerHttpResponse(mockResponse),
                )

        wrappedResponse shouldBe BaseResponse.error(code = mockResponse.status, data = null)
    }
})
