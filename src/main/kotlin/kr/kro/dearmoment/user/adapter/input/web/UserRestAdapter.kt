package kr.kro.dearmoment.user.adapter.input.web

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kr.kro.dearmoment.security.JwtTokenProvider
import kr.kro.dearmoment.user.application.command.RegisterUserCommand
import kr.kro.dearmoment.user.application.dto.request.LoginUserRequest
import kr.kro.dearmoment.user.application.dto.request.RegisterUserRequest
import kr.kro.dearmoment.user.application.dto.request.UpdateUserNameRequest
import kr.kro.dearmoment.user.application.dto.response.LoginUserResponse
import kr.kro.dearmoment.user.application.dto.response.UserResponse
import kr.kro.dearmoment.user.application.port.input.RegisterUserUseCase
import kr.kro.dearmoment.user.application.service.UserProfileService
import kr.kro.dearmoment.user.security.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User API", description = "유저와 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
class UserRestAdapter(
    private val registerUserUseCase: RegisterUserUseCase,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userProfileService: UserProfileService,
) {
    @PostMapping("/signup")
    fun register(
        @RequestBody req: RegisterUserRequest,
    ): ResponseEntity<UserResponse> {
        val command =
            RegisterUserCommand(
                loginId = req.loginId,
                password = req.password,
                name = req.name,
            )
        val response = registerUserUseCase.register(command)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody req: LoginUserRequest,
    ): ResponseEntity<LoginUserResponse> {
        // 인증 요청 토큰 생성
        val authToken = UsernamePasswordAuthenticationToken(req.loginId, req.password)
        val authentication = authenticationManager.authenticate(authToken)
        // 인증 성공 시 SecurityContext에 저장
        SecurityContextHolder.getContext().authentication = authentication

        // CustomUserDetails에서 JWT 생성 (타입 캐스팅)
        val token = jwtTokenProvider.generateToken(authentication.principal as CustomUserDetails)
        return ResponseEntity.ok(LoginUserResponse(success = true, token = token))
    }

    @GetMapping()
    fun getProfile(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<UserResponse> {
        val userId =
            if (principal is CustomUserDetails) {
                principal.id
            } else {
                throw IllegalStateException("인증된 사용자 정보가 올바르지 않습니다.")
            }
        val user = userProfileService.getProfile(userId)
        // UserResponse 생성은 도메인 모델을 응답 DTO로 변환하는 로직입니다.
        val response = UserResponse.from(user)
        return ResponseEntity.ok(response)
    }

    @PatchMapping()
    fun updateName(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @RequestBody req: UpdateUserNameRequest,
    ): ResponseEntity<UserResponse> {
        val userId =
            if (principal is CustomUserDetails) {
                principal.id
            } else {
                throw IllegalStateException("인증된 사용자 정보가 올바르지 않습니다.")
            }
        val updatedUser = userProfileService.updateName(userId, req.name)
        val response = UserResponse.from(updatedUser)
        return ResponseEntity.ok(response)
    }
}
