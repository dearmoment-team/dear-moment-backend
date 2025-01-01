package kr.kro.onboarding.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import kr.kro.onboarding.common.converter.CustomHttpMessageConverter
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(val objectMapper: ObjectMapper) : WebMvcConfigurer {
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // 커스텀 컨버터 제거
        // converters.add(HIGHEST_CONVERTER_PRIORITY, CustomHttpMessageConverter(objectMapper))
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/static/swagger-ui/")
    }
}
