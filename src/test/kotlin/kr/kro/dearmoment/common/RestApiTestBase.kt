package kr.kro.dearmoment.common

import com.ninjasquad.springmockk.MockkBean
import kr.kro.dearmoment.TestEnvironment
import kr.kro.dearmoment.inquiry.adapter.input.web.ProductOptionInquiryRestAdapter
import kr.kro.dearmoment.inquiry.adapter.input.web.ServiceInquiryRestAdapter
import kr.kro.dearmoment.inquiry.adapter.input.web.StudioInquiryRestAdapter
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.like.adapter.input.web.ProductLikeRestAdapter
import kr.kro.dearmoment.like.adapter.input.web.ProductOptionLikeRestAdapter
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.service.LikeQueryService
import kr.kro.dearmoment.product.adapter.input.web.ProductRestAdapter
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductOptionUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import kr.kro.dearmoment.product.application.usecase.get.GetProductUseCase
import kr.kro.dearmoment.product.application.usecase.search.ProductSearchUseCase
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import kr.kro.dearmoment.studio.adapter.input.StudioRestAdapter
import kr.kro.dearmoment.studio.application.port.input.DeleteStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.GetStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.ModifyStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.RegisterStudioUseCase
import kr.kro.dearmoment.user.domain.User
import kr.kro.dearmoment.user.security.CustomUserDetails
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import java.time.LocalDateTime
import java.util.UUID

@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
// Controller 여기에 나열 ..
@WebMvcTest(
    controllers = [
        ProductLikeRestAdapter::class,
        ProductOptionLikeRestAdapter::class,
        StudioInquiryRestAdapter::class,
        ProductOptionInquiryRestAdapter::class,
        ServiceInquiryRestAdapter::class,
        StudioRestAdapter::class,
        ProductRestAdapter::class,
    ],
)
@TestEnvironment
@WithMockUser(roles = ["USER", "ARTIST"])
abstract class RestApiTestBase {
    lateinit var mockMvc: MockMvc

    @MockkBean
    protected lateinit var likeUseCase: LikeUseCase

    @MockkBean
    protected lateinit var likeQueryService: LikeQueryService

    @MockkBean
    protected lateinit var createInquiryUseCase: CreateInquiryUseCase

    @MockkBean
    protected lateinit var removeInquiryUseCase: RemoveInquiryUseCase

    @MockkBean
    protected lateinit var getInquiryUseCase: GetInquiryUseCase

    @MockkBean
    protected lateinit var registerStudioUseCase: RegisterStudioUseCase

    @MockkBean
    protected lateinit var getStudioUseCase: GetStudioUseCase

    @MockkBean
    protected lateinit var modifyStudioUseCase: ModifyStudioUseCase

    @MockkBean
    protected lateinit var deleteStudioUseCase: DeleteStudioUseCase

    @MockkBean
    lateinit var createProductUseCase: CreateProductUseCase

    @MockkBean
    lateinit var updateProductUseCase: UpdateProductUseCase

    @MockkBean
    lateinit var deleteProductUseCase: DeleteProductUseCase

    @MockkBean
    lateinit var getProductUseCase: GetProductUseCase

    @MockkBean
    lateinit var productSearchUseCase: ProductSearchUseCase

    @MockkBean
    lateinit var deleteProductOptionUseCase: DeleteProductOptionUseCase

    val userId = UUID.randomUUID()

    /**
     * RestDocs 문서화를 위한 `MockMvc` 객체를 설정하는 함수입니다.
     * Spring WebApplicationContext와 RestDocumentationContextProvider를 사용하여 MockMvc를 초기화하고,
     * 요청 및 응답을 예쁘게 출력하도록 설정합니다.
     *
     * @param context `WebApplicationContext` 객체
     * @param provider `RestDocumentationContextProvider` 객체
     * @return 설정된 `MockMvc` 객체
     */
    @BeforeEach
    internal fun setUp(
        context: WebApplicationContext,
        provider: RestDocumentationContextProvider,
    ) {
        val user =
            User(
                id = userId,
                loginId = "user@email.com",
                password = "password",
                name = "user",
                createdAt = LocalDateTime.now(),
            )
        val userDetails = CustomUserDetails(user)

        mockMvc =
            MockMvcBuilders
                // 웹 애플리케이션 컨텍스트 설정
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(
                    // RestDocs 설정
                    MockMvcRestDocumentation.documentationConfiguration(provider)
                        .operationPreprocessors()
                        // 요청 예쁘게 포맷
                        .withRequestDefaults(Preprocessors.prettyPrint())
                        // 응답 예쁘게 포맷
                        .withResponseDefaults(Preprocessors.prettyPrint()),
                )
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true)) // UTF-8 문자 인코딩 필터 추가
                .defaultRequest<DefaultMockMvcBuilder>(
                    RestDocumentationRequestBuilders.post("/**")
                        .with(csrf())
                        .with(user(userDetails)),
                )
                .defaultRequest<DefaultMockMvcBuilder>(
                    RestDocumentationRequestBuilders.get("/**")
                        .with(csrf())
                        .with(user(userDetails)),
                )
                .defaultRequest<DefaultMockMvcBuilder>(
                    RestDocumentationRequestBuilders.patch("/**")
                        .with(csrf())
                        .with(user(userDetails)),
                ) // CSRF 적용
                .defaultRequest<DefaultMockMvcBuilder>(RestDocumentationRequestBuilders.delete("/**").with(csrf()).with(user(userDetails))) // CSRF 적용
                .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print()) // 결과 출력
                .build() // MockMvc 객체 빌드
    }

    /**
     * @AuthenticationPrincipal 주석에서 사용할 UUID를 가진 사용자로 요청에 인증 정보를 추가합니다.
     * @param userId 인증된 사용자의 UUID
     * @param request 원본 요청 빌더
     * @return 인증 정보가 추가된 요청 빌더
     */
    protected fun withAuthenticatedUser(
        userId: UUID,
        request: MockHttpServletRequestBuilder,
    ): MockHttpServletRequestBuilder {
        return request.with(user(TestUserDetails(userId)))
    }

    /**
     * @AuthenticationPrincipal(expression = "id")에서 사용할 id 속성을 제공하는 테스트용 UserDetails 구현
     */
    class TestUserDetails(private val userId: UUID) : UserDetails {
        override fun getAuthorities(): Collection<GrantedAuthority> =
            listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("STUDIO"))

        override fun getPassword(): String = "password"

        override fun getUsername(): String = userId.toString()

        override fun isAccountNonExpired(): Boolean = true

        override fun isAccountNonLocked(): Boolean = true

        override fun isCredentialsNonExpired(): Boolean = true

        override fun isEnabled(): Boolean = true

        fun getId(): UUID = userId
    }
}
