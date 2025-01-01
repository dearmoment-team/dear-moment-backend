package kr.kro.onboarding.common.config

import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("BoardGame API")
                    .description("BoardGame API documentation with OpenAPI 3.0")
                    .version("1.0")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
            )
    }
}
