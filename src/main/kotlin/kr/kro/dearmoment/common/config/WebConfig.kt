package kr.kro.dearmoment.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import kr.kro.dearmoment.common.converter.CustomHttpMessageConverter
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val objectMapper: ObjectMapper,
) : WebMvcConfigurer {
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // 커스텀 컨버터 제거
        converters.add(HIGHEST_CONVERTER_PRIORITY, CustomHttpMessageConverter(objectMapper))
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/static/swagger-ui/")
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/static/swagger-ui/")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("OPTIONS", "GET", "POST", "PUT", "DELETE")
    }

    companion object {
        private const val HIGHEST_CONVERTER_PRIORITY = 0
    }
}
