package kr.kro.dearmoment.common

import com.fasterxml.jackson.databind.ObjectMapper
import kr.kro.dearmoment.common.dto.ResponseWrapper
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductOptionUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import kr.kro.dearmoment.product.application.usecase.get.GetProductUseCase
import kr.kro.dearmoment.product.application.usecase.search.ProductSearchUseCase
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc

/**
 * 여러 테스트 클래스에서 공통되는 MockMvc, ObjectMapper, @MockitoBean 등을
 * 한 곳에서 정의하여 상속받을 수 있도록 하는 추상 클래스 예시입니다.
 */
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(ResponseWrapper::class)
@ExtendWith(RestDocumentationExtension::class)
@TestPropertySource(properties = ["spring.config.location=classpath:application-test.yml"])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class MockBaseApiTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockitoBean
    protected lateinit var createProductUseCase: CreateProductUseCase

    @MockitoBean
    protected lateinit var updateProductUseCase: UpdateProductUseCase

    @MockitoBean
    protected lateinit var deleteProductUseCase: DeleteProductUseCase

    @MockitoBean
    protected lateinit var getProductUseCase: GetProductUseCase

    @MockitoBean
    protected lateinit var productSearchUseCase: ProductSearchUseCase

    @MockitoBean
    protected lateinit var deleteProductOptionUseCase: DeleteProductOptionUseCase

    protected fun apiJsonHeaders() = mapOf("Content-Type" to MediaType.APPLICATION_JSON_VALUE)
}
