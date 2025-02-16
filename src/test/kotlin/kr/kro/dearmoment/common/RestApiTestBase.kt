package kr.kro.dearmoment.common

import com.ninjasquad.springmockk.MockkBean
import kr.kro.dearmoment.TestEnvironment
import kr.kro.dearmoment.inquiry.adapter.input.web.author.AuthorInquiryRestAdapter
import kr.kro.dearmoment.inquiry.adapter.input.web.product.ProductInquiryRestAdapter
import kr.kro.dearmoment.inquiry.adapter.input.web.service.ServiceInquiryRestAdapter
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.like.adapter.input.web.LikeRestAdapter
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
// Controller 여기에 나열 ..
@WebMvcTest(
    controllers = [
        LikeRestAdapter::class,
        AuthorInquiryRestAdapter::class,
        ProductInquiryRestAdapter::class,
        ServiceInquiryRestAdapter::class,
    ],
)
@TestEnvironment
abstract class RestApiTestBase {
    lateinit var mockMvc: MockMvc

    @MockkBean
    protected lateinit var likeUseCase: LikeUseCase

    @MockkBean
    protected lateinit var createInquiryUseCase: CreateInquiryUseCase

    @MockkBean
    protected lateinit var removeInquiryUseCase: RemoveInquiryUseCase

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
                // .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity(MockSecurityFilter())) security 추가 이후
                .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true)) // UTF-8 문자 인코딩 필터 추가
                .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print()) // 결과 출력
                .build() // MockMvc 객체 빌드
    }
}
