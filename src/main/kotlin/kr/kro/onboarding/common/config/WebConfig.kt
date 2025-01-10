package kr.kro.onboarding.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import kr.kro.onboarding.common.converter.CustomHttpMessageConverter
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val objectMapper: ObjectMapper,
) : WebMvcConfigurer {
    companion object {
        private const val HIGHEST_CONVERTER_PRIORITY = 0
    }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // 커스텀 컨버터 추가
        println("[DEBUG] WebConfig.extendMessageConverters() 호출됨")
        converters.add(HIGHEST_CONVERTER_PRIORITY, CustomHttpMessageConverter(objectMapper))
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        println("[DEBUG] WebConfig.addResourceHandlers() 호출됨")

        // "/static/**" → classpath:/static/
        println("[DEBUG] '/static/**' → 'classpath:/static/' 매핑 추가")
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")

        // "/swagger-ui.html" → classpath:/static/swagger-ui/
        println("[DEBUG] 'swagger-ui.html' → 'classpath:/static/swagger-ui/' 매핑 추가")
        registry.addResourceHandler("/swagger-ui.html")
            .addResourceLocations("classpath:/static/swagger-ui/")

        // "/swagger-ui/**" → classpath:/static/swagger-ui/
        println("[DEBUG] '/swagger-ui/**' → 'classpath:/static/swagger-ui/' 매핑 추가")
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/static/swagger-ui/")
    }
}
