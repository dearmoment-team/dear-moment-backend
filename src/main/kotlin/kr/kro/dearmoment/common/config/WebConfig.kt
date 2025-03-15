package kr.kro.dearmoment.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import kr.kro.dearmoment.common.converter.CustomHttpMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.support.StandardServletMultipartResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.StandardCharsets

@Configuration
class WebConfig(
    private val objectMapper: ObjectMapper,
) : WebMvcConfigurer {

    @Bean
    fun multipartResolver(): MultipartResolver {
        return StandardServletMultipartResolver()
    }

    // 기존의 기본 컨버터들을 유지하면서 확장하는 방식으로 설정
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // 1. StringHttpMessageConverter의 기본 문자셋을 UTF-8로 설정
        converters.filterIsInstance<StringHttpMessageConverter>()
            .forEach { it.defaultCharset = StandardCharsets.UTF_8 }

        // 2. ObjectMapper에 Kotlin 모듈 등록
        objectMapper.registerModule(kotlinModule())

        // 3. JSON 처리를 위한 커스텀 컨버터 설정
        val jsonConverter = CustomHttpMessageConverter(objectMapper)
        // 멀티파트 내부의 JSON도 처리할 수 있도록 application/octet-stream 지원 추가
        val supportedMediaTypes = jsonConverter.supportedMediaTypes.toMutableList()
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM)
        jsonConverter.supportedMediaTypes = supportedMediaTypes

        // 기존 Jackson 컨버터를 찾고 교체
        val jacksonIndex = converters.indexOfFirst { it is MappingJackson2HttpMessageConverter }
        if (jacksonIndex != -1) {
            converters[jacksonIndex] = jsonConverter
        } else {
            converters.add(jsonConverter)
        }

        // 4. Form 데이터 처리 컨버터 강화
        converters.filterIsInstance<AllEncompassingFormHttpMessageConverter>().forEach { formConverter ->
            // FormHttpMessageConverter에 JSON 파트 처리 추가
            formConverter.addPartConverter(jsonConverter)
        }
    }

    // Swagger UI 리소스 핸들러 설정
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
            .resourceChain(false)
    }

    // CORS 설정
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("https://dearmoment.kro.kr")
            .allowedMethods("OPTIONS", "GET", "POST", "PUT", "DELETE")
            .allowCredentials(true)
            .maxAge(3600)
    }
}