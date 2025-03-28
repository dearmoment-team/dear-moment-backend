package kr.kro.dearmoment.security

import kr.kro.dearmoment.user.security.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager = authConfig.authenticationManager

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { authz ->
                authz.requestMatchers(
                    "/api/users/signup",
                    "/api/users/login",
                    "/",
                    "/swagger-ui/**",
                    "/v3/**",
                    "/oauth/kakao/callback",
                ).permitAll()
                    // 스튜디오 조회 - 비회원도 가능
                    .requestMatchers(HttpMethod.GET, "/api/products/main").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/*").permitAll()
                    // 1. 스튜디오 문의
                    .requestMatchers("/api/inquiries/studios/**").hasRole("USER")
                    // 2. 스튜디오
                    .requestMatchers(HttpMethod.PUT, "/api/studios/*").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/studios/*").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/studios").permitAll()
                    // 3. 서비스 문의
                    .requestMatchers("/api/inquiries/services").hasRole("USER")
                    // 4. 상품 옵션 문의
                    .requestMatchers("/api/inquiries/product-options/**").hasRole("USER")
                    // 5. /api/likes/** → ROLE_USER
                    .requestMatchers("/api/likes/**").hasRole("USER")
                    // 6. /api/products
                    .requestMatchers(HttpMethod.DELETE, "/api/products/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/products")permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/api/products/**").permitAll()
                    // 그 외 경로는 인증만 되어 있으면 접근 가능
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
