package kr.kro.dearmoment.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        val bearerAuthSchemeName = "bearerAuth"

        return OpenAPI()
            .servers(listOf(Server().url("https://dearmoment.o-r.kr")))
            .components(
                Components().addSecuritySchemes(
                    bearerAuthSchemeName,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"),
                ),
            )
            // 전역 SecurityRequirement 설정
            .addSecurityItem(SecurityRequirement().addList(bearerAuthSchemeName))
    }
}
