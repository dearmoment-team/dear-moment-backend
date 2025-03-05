package kr.kro.dearmoment.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import kr.kro.dearmoment.common.converter.CustomHttpMessageConverter
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.StandardCharsets

@Configuration
class WebConfig(
    private val objectMapper: ObjectMapper,
) : WebMvcConfigurer {
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // StringHttpMessageConverter의 기본 charset을 UTF-8로 변경
        if (converters.size > 1 && converters[1] is StringHttpMessageConverter) {
            (converters[1] as StringHttpMessageConverter).defaultCharset = StandardCharsets.UTF_8
        }
        // ObjectMapper에 kotlinModule 등록 (Kotlin 데이터 클래스 지원)
        objectMapper.registerModule(kotlinModule())
        // 기존의 MappingJackson2HttpMessageConverter 앞에 CustomHttpMessageConverter 추가 (index 6에 삽입)
        if (converters.size >= 7) {
            converters.add(6, CustomHttpMessageConverter(objectMapper))
        } else {
            // 만약 index 6이 존재하지 않는다면 리스트 마지막에 추가
            converters.add(CustomHttpMessageConverter(objectMapper))
        }
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/static/swagger-ui/")
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/static/swagger-ui/")
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("OPTIONS", "GET", "POST", "PUT", "DELETE")
    }
}
