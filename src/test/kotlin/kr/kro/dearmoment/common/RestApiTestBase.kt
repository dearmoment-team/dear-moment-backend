package kr.kro.dearmoment.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import kr.kro.dearmoment.TestEnvironment
import kr.kro.dearmoment.inquiry.adapter.input.web.ProductOptionInquiryRestAdapter
import kr.kro.dearmoment.inquiry.adapter.input.web.ServiceInquiryRestAdapter
import kr.kro.dearmoment.inquiry.adapter.input.web.StudioInquiryRestAdapter
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.like.adapter.input.web.LikeRestAdapter
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.service.LikeQueryService
import kr.kro.dearmoment.product.adapter.input.web.ProductRestAdapter
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import kr.kro.dearmoment.product.application.usecase.get.GetProductUseCase
import kr.kro.dearmoment.product.application.usecase.search.ProductSearchUseCase
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import kr.kro.dearmoment.studio.adapter.input.StudioRestAdapter
import kr.kro.dearmoment.studio.application.port.input.DeleteStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.GetStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.ModifyStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.RegisterStudioUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

@WebMvcTest(
    controllers = [
        LikeRestAdapter::class,
        StudioInquiryRestAdapter::class,
        ProductOptionInquiryRestAdapter::class,
        ServiceInquiryRestAdapter::class,
        StudioRestAdapter::class,
        ProductRestAdapter::class,
    ]
)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
@TestEnvironment
abstract class RestApiTestBase {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var createProductUseCase: CreateProductUseCase

    @MockitoBean
    lateinit var updateProductUseCase: UpdateProductUseCase

    @MockitoBean
    lateinit var deleteProductUseCase: DeleteProductUseCase

    @MockitoBean
    lateinit var getProductUseCase: GetProductUseCase

    @MockitoBean
    lateinit var productSearchUseCase: ProductSearchUseCase

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

    @BeforeEach
    internal fun setUp(context: WebApplicationContext, provider: RestDocumentationContextProvider) {
        val configurer = MockMvcRestDocumentation.documentationConfiguration(provider)
            .operationPreprocessors()
            .withRequestDefaults(Preprocessors.prettyPrint())
            .withResponseDefaults(Preprocessors.prettyPrint())

        val builder = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(configurer)

        mockMvc = builder
            .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
            .build()
    }
}
