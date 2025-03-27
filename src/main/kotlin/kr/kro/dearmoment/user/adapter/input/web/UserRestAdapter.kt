package kr.kro.dearmoment.user.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
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
import java.util.UUID

@Tag(name = "User API", description = "유저와 관련된 API")
@RestController
@RequestMapping("/api/users")
class UserRestAdapter(
    private val registerUserUseCase: RegisterUserUseCase,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userProfileService: UserProfileService,
) {
    @Operation(summary = "(작가/스튜디오)회원가입", description = "새로운(작가/스튜디오) 유저를 회원가입합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "회원가입 성공",
                content = [Content(schema = Schema(implementation = UserResponse::class))],
            ),
        ],
    )
    @PostMapping("/signup")
    fun register(
        @Parameter(description = "회원가입 요청 정보", required = true)
        @RequestBody req: RegisterUserRequest,
    ): UserResponse {
        val command =
            RegisterUserCommand(
                loginId = req.loginId,
                password = req.password,
                name = req.name,
            )
        return registerUserUseCase.register(command)
    }

    @Operation(summary = "(작가/스튜디오)로그인", description = "아이디/비밀번호(작가/스튜디오) 로그인 처리 후 JWT 토큰을 발급합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [Content(schema = Schema(implementation = LoginUserResponse::class))],
            ),
        ],
    )
    @PostMapping("/login")
    fun login(
        @Parameter(description = "(작가/스튜디오) 로그인 요청 정보", required = true)
        @RequestBody req: LoginUserRequest,
        response: HttpServletResponse,
    ): LoginUserResponse {
        val authToken = UsernamePasswordAuthenticationToken(req.loginId, req.password)
        val authentication = authenticationManager.authenticate(authToken)
        SecurityContextHolder.getContext().authentication = authentication

        val token = jwtTokenProvider.generateToken(authentication.principal as CustomUserDetails)
        response.setHeader("Authorization", "Bearer $token")
        return LoginUserResponse(success = true)
    }

    @Operation(summary = "프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "프로필 조회 성공",
                content = [Content(schema = Schema(implementation = UserResponse::class))],
            ),
        ],
    )
    @GetMapping
    fun getProfile(
        @Parameter(description = "인증 후 principal.id에서 가져온 사용자 UUID", required = false)
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): UserResponse {
        val user = userProfileService.getProfile(userId)
        return UserResponse.from(user)
    }

    @Operation(summary = "이름 변경", description = "(초기 MVP 제거 예정) 로그인한 사용자의 이름을 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "이름 변경 성공",
                content = [Content(schema = Schema(implementation = UserResponse::class))],
            ),
        ],
    )
    @PatchMapping
    fun updateName(
        @Parameter(description = "인증 후 principal.id에서 가져온 사용자 UUID", required = false)
        @AuthenticationPrincipal(expression = "id") userId: UUID,
        @Parameter(description = "이름 변경 요청 DTO", required = true)
        @RequestBody req: UpdateUserNameRequest,
    ): UserResponse {
        val updatedUser = userProfileService.updateName(userId, req.name)
        return UserResponse.from(updatedUser)
    }
}
