package kr.kro.dearmoment.product.application.usecase.update

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.handler.ImageHandler
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.dto.request.AdditionalImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.SubImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateAdditionalImageAction
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateSubImageAction
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.option.ProductOptionUseCase
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime

class UpdateProductUseCaseTest : BehaviorSpec({

    // -- Mock 설정 --
    val productPersistencePort = mockk<ProductPersistencePort>()
    val imageHandler = mockk<ImageHandler>()
    val productOptionUseCase = mockk<ProductOptionUseCase>(relaxed = true)

    val useCase =
        UpdateProductUseCaseImpl(
            productPersistencePort = productPersistencePort,
            imageHandler = imageHandler,
            productOptionUseCase = productOptionUseCase,
        )

    // -- 더미 데이터 --
    val dummyExistingMainImage =
        Image(
            imageId = 101L,
            userId = 1L,
            parId = "",
            fileName = "existing_main.jpg",
            url = "http://example.com/existing_main.jpg",
        )
    val dummyExistingSubImage =
        Image(
            imageId = 200L,
            userId = 1L,
            parId = "",
            fileName = "existing_sub.jpg",
            url = "http://example.com/existing_sub.jpg",
        )
    val dummyExistingAdditionalImage =
        Image(
            imageId = 300L,
            userId = 1L,
            parId = "",
            fileName = "existing_add.jpg",
            url = "http://example.com/existing_add.jpg",
        )

    val existingProduct =
        Product(
            productId = 999L,
            userId = 1L,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Existing Product",
            description = "Existing Description",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF),
            cameraTypes = setOf(CameraType.FILM),
            retouchStyles = setOf(RetouchStyle.NATURAL),
            mainImage = dummyExistingMainImage,
            subImages =
            listOf(
                dummyExistingSubImage,
                dummyExistingSubImage,
                dummyExistingSubImage,
                dummyExistingSubImage,
            ),
            additionalImages = listOf(dummyExistingAdditionalImage),
            detailedInfo = "Existing Detailed Info",
            contactInfo = "Existing Contact",
            createdAt = LocalDateTime.now().minusDays(2),
            updatedAt = LocalDateTime.now().minusDays(1),
            options = emptyList(),
        )

    val updateRequest =
        UpdateProductRequest(
            productId = 999L,
            userId = 1L,
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "Updated Title",
            description = "Updated Description",
            availableSeasons = listOf("YEAR_2025_SECOND_HALF"),
            cameraTypes = listOf("DIGITAL"),
            retouchStyles = listOf("CALM"),
            mainImageFile =
            MockMultipartFile(
                "mainImageFile",
                "new_main.jpg",
                "image/jpeg",
                "new-main-content".toByteArray(),
            ),
            subImagesFinal =
            listOf(
                SubImageFinalRequest(UpdateSubImageAction.KEEP, 200L, null),
                SubImageFinalRequest(UpdateSubImageAction.DELETE, 200L, null),
                SubImageFinalRequest(
                    UpdateSubImageAction.UPLOAD,
                    null,
                    MockMultipartFile("sub3.jpg", "sub3.jpg", "image/jpeg", "sub3-content".toByteArray()),
                ),
                SubImageFinalRequest(
                    UpdateSubImageAction.UPLOAD,
                    null,
                    MockMultipartFile("sub4.jpg", "sub4.jpg", "image/jpeg", "sub4-content".toByteArray()),
                ),
            ),
            additionalImagesFinal =
            listOf(
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.DELETE, 300L, null),
                AdditionalImageFinalRequest(
                    UpdateAdditionalImageAction.UPLOAD,
                    null,
                    MockMultipartFile("add2.jpg", "add2.jpg", "image/jpeg", "add2-content".toByteArray()),
                ),
            ),
            detailedInfo = "Updated Detailed Info",
            contactInfo = "Updated Contact",
            options = emptyList(),
        )

    val dummyNewMainImage =
        Image(
            imageId = 201L,
            userId = 1L,
            parId = "",
            fileName = "new_main.jpg",
            url = "http://example.com/new_main.jpg",
        )
    val dummyNewSubImage1 =
        Image(
            imageId = 202L,
            userId = 1L,
            parId = "",
            fileName = "sub3.jpg",
            url = "http://example.com/sub3.jpg",
        )
    val dummyNewSubImage2 =
        Image(
            imageId = 203L,
            userId = 1L,
            parId = "",
            fileName = "sub4.jpg",
            url = "http://example.com/sub4.jpg",
        )
    val dummyNewAdditionalImage =
        Image(
            imageId = 400L,
            userId = 1L,
            parId = "",
            fileName = "add2.jpg",
            url = "http://example.com/add2.jpg",
        )

    // 서브 이미지 교체 시 필요한 더미들
    val baseUpdateRequest =
        updateRequest.copy(
            mainImageFile = null,
            additionalImagesFinal = emptyList(),
        )

    val mockSubFileA = MockMultipartFile("subA.jpg", "subA.jpg", "image/jpeg", "subA-content".toByteArray())
    val mockSubFileB = MockMultipartFile("subB.jpg", "subB.jpg", "image/jpeg", "subB-content".toByteArray())
    val mockSubFileC = MockMultipartFile("subC.jpg", "subC.jpg", "image/jpeg", "subC-content".toByteArray())
    val mockSubFileD = MockMultipartFile("subD.jpg", "subD.jpg", "image/jpeg", "subD-content".toByteArray())

    val dummyExistingSubImage1 =
        Image(
            imageId = 201L,
            userId = 1L,
            parId = "",
            fileName = "existing_sub1.jpg",
            url = "http://example.com/existing_sub1.jpg",
        )
    val dummyExistingSubImage2 =
        Image(
            imageId = 202L,
            userId = 1L,
            parId = "",
            fileName = "existing_sub2.jpg",
            url = "http://example.com/existing_sub2.jpg",
        )
    val dummyExistingSubImage3 =
        Image(
            imageId = 203L,
            userId = 1L,
            parId = "",
            fileName = "existing_sub3.jpg",
            url = "http://example.com/existing_sub3.jpg",
        )

    val dummyNewSubImageA =
        Image(
            imageId = 301L,
            userId = 1L,
            parId = "",
            fileName = "new_subA.jpg",
            url = "http://example.com/new_subA.jpg",
        )
    val dummyNewSubImageB =
        Image(
            imageId = 302L,
            userId = 1L,
            parId = "",
            fileName = "new_subB.jpg",
            url = "http://example.com/new_subB.jpg",
        )
    val dummyNewSubImageC =
        Image(
            imageId = 303L,
            userId = 1L,
            parId = "",
            fileName = "new_subC.jpg",
            url = "http://example.com/new_subC.jpg",
        )
    val dummyNewSubImageD =
        Image(
            imageId = 304L,
            userId = 1L,
            parId = "",
            fileName = "new_subD.jpg",
            url = "http://example.com/new_subD.jpg",
        )

    // -------------------------------------------------------------
    // 여기서부터 하나의 Given 블록 내부에 모든 시나리오를 담는다.
    // -------------------------------------------------------------
    Given("updateProduct 메서드") {

        When("존재하지 않는 상품 ID 요청 시") {
            every { productPersistencePort.findById(999L) } returns null

            Then("예외 발생") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.updateProduct(updateRequest)
                    }
                exception.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
            }
        }

        When("정상 요청 시") {
            // 실제 엔티티 인스턴스 생성 후 스파이로 감싸 실제 로직이 실행되도록 함
            val realEntity = ProductEntity.fromDomain(existingProduct)
            val spiedEntity = spyk(realEntity)

            // Companion 오브젝트 목킹
            mockkObject(ProductEntity.Companion)
            every { ProductEntity.fromDomain(existingProduct) } returns spiedEntity

            every { productPersistencePort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every { imageHandler.processSubImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyExistingSubImage,
                        dummyNewSubImage1,
                        dummyNewSubImage2,
                        dummyNewSubImage1,
                    )
            every { imageHandler.processAdditionalImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyNewAdditionalImage,
                    )
            every { productPersistencePort.save(any()) } returns
                    existingProduct.copy(
                        title = "Updated Title",
                        description = "Updated Description",
                        availableSeasons = setOf(ShootingSeason.YEAR_2025_SECOND_HALF),
                        cameraTypes = setOf(CameraType.DIGITAL),
                        retouchStyles = setOf(RetouchStyle.CALM),
                        mainImage = dummyNewMainImage,
                        subImages =
                        listOf(
                            dummyExistingSubImage,
                            dummyNewSubImage1,
                            dummyNewSubImage2,
                            dummyNewSubImage1,
                        ),
                        additionalImages = listOf(dummyNewAdditionalImage),
                        detailedInfo = "Updated Detailed Info",
                        contactInfo = "Updated Contact",
                    )

            val result = useCase.updateProduct(updateRequest)

            Then("결과 검증") {
                result.title shouldBe "Updated Title"
                result.availableSeasons shouldHaveSize 1
                result.retouchStyles.first() shouldBe "CALM"
                result.mainImage shouldBe "http://example.com/new_main.jpg"
            }

            Then("상호작용 검증") {
                verify(exactly = 1) { imageHandler.updateMainImage(any(), any(), any()) }
                verify(exactly = 1) { imageHandler.processSubImagesFinal(any(), any(), any()) }
                verify(exactly = 1) { productPersistencePort.save(any()) }
            }

            unmockkObject(ProductEntity.Companion)
        }

        // --------------------------------------------------------------
        // -- 아래부터 서브 이미지를 (1개/2개/3개/4개)만 교체하는 시나리오 --
        // --------------------------------------------------------------
        When("서브 이미지를 1개만 교체하고, 나머지 3개는 그대로 유지할 때") {
            val oneChangeRequest =
                baseUpdateRequest.copy(
                    subImagesFinal =
                    listOf(
                        SubImageFinalRequest(UpdateSubImageAction.KEEP, dummyExistingSubImage1.imageId, null),
                        SubImageFinalRequest(UpdateSubImageAction.KEEP, dummyExistingSubImage2.imageId, null),
                        SubImageFinalRequest(UpdateSubImageAction.KEEP, dummyExistingSubImage3.imageId, null),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileD),
                    ),
                )

            every { productPersistencePort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every { imageHandler.processSubImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyExistingSubImage1,
                        dummyExistingSubImage2,
                        dummyExistingSubImage3,
                        dummyNewSubImageD,
                    )
            every { imageHandler.processAdditionalImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyExistingAdditionalImage,
                    )

            mockkObject(ProductEntity.Companion)
            val spiedEntity = spyk(ProductEntity.fromDomain(existingProduct))
            every { ProductEntity.fromDomain(existingProduct) } returns spiedEntity

            every { productPersistencePort.save(any()) } answers {
                existingProduct.copy(
                    mainImage = dummyNewMainImage,
                    subImages =
                    listOf(
                        dummyExistingSubImage1,
                        dummyExistingSubImage2,
                        dummyExistingSubImage3,
                        dummyNewSubImageD,
                    ),
                    updatedAt = LocalDateTime.now(),
                )
            }

            val result = useCase.updateProduct(oneChangeRequest)

            Then("서브 이미지 4개 중 3개는 기존 그대로, 1개만 새로운 이미지로 교체된다") {
                result.subImages shouldHaveSize 4
                result.subImages.last() shouldBe dummyNewSubImageD.url
            }

            unmockkObject(ProductEntity.Companion)
        }

        When("서브 이미지를 2개만 교체하고, 나머지 2개는 그대로 유지할 때") {
            val twoChangeRequest =
                baseUpdateRequest.copy(
                    subImagesFinal =
                    listOf(
                        SubImageFinalRequest(UpdateSubImageAction.KEEP, dummyExistingSubImage1.imageId, null),
                        SubImageFinalRequest(UpdateSubImageAction.KEEP, dummyExistingSubImage2.imageId, null),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileC),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileD),
                    ),
                )

            every { productPersistencePort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every { imageHandler.processSubImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyExistingSubImage1,
                        dummyExistingSubImage2,
                        dummyNewSubImageC,
                        dummyNewSubImageD,
                    )
            every { imageHandler.processAdditionalImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyExistingAdditionalImage,
                    )

            mockkObject(ProductEntity.Companion)
            val spiedEntity = spyk(ProductEntity.fromDomain(existingProduct))
            every { ProductEntity.fromDomain(existingProduct) } returns spiedEntity

            every { productPersistencePort.save(any()) } answers {
                existingProduct.copy(
                    mainImage = dummyNewMainImage,
                    subImages =
                    listOf(
                        dummyExistingSubImage1,
                        dummyExistingSubImage2,
                        dummyNewSubImageC,
                        dummyNewSubImageD,
                    ),
                    updatedAt = LocalDateTime.now(),
                )
            }

            val result = useCase.updateProduct(twoChangeRequest)

            Then("서브 이미지 4개 중 2개는 기존 그대로, 2개가 새 파일로 업로드된다") {
                result.subImages[0] shouldBe dummyExistingSubImage1.url
                result.subImages[1] shouldBe dummyExistingSubImage2.url
                result.subImages[2] shouldBe dummyNewSubImageC.url
                result.subImages[3] shouldBe dummyNewSubImageD.url
            }

            unmockkObject(ProductEntity.Companion)
        }

        When("서브 이미지를 3개만 교체하고, 나머지 1개는 그대로 유지할 때") {
            val threeChangeRequest =
                baseUpdateRequest.copy(
                    subImagesFinal =
                    listOf(
                        SubImageFinalRequest(UpdateSubImageAction.KEEP, dummyExistingSubImage1.imageId, null),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileA),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileB),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileC),
                    ),
                )

            every { productPersistencePort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every { imageHandler.processSubImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyExistingSubImage1,
                        dummyNewSubImageA,
                        dummyNewSubImageB,
                        dummyNewSubImageC,
                    )
            every { imageHandler.processAdditionalImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyExistingAdditionalImage,
                    )

            mockkObject(ProductEntity.Companion)
            val spiedEntity = spyk(ProductEntity.fromDomain(existingProduct))
            every { ProductEntity.fromDomain(existingProduct) } returns spiedEntity

            every { productPersistencePort.save(any()) } answers {
                existingProduct.copy(
                    mainImage = dummyNewMainImage,
                    subImages =
                    listOf(
                        dummyExistingSubImage1,
                        dummyNewSubImageA,
                        dummyNewSubImageB,
                        dummyNewSubImageC,
                    ),
                    updatedAt = LocalDateTime.now(),
                )
            }

            val result = useCase.updateProduct(threeChangeRequest)

            Then("서브 이미지 4개 중 1개는 기존 그대로, 3개가 새로운 이미지로 교체된다") {
                result.subImages[0] shouldBe dummyExistingSubImage1.url
                result.subImages[1] shouldBe dummyNewSubImageA.url
                result.subImages[2] shouldBe dummyNewSubImageB.url
                result.subImages[3] shouldBe dummyNewSubImageC.url
            }

            unmockkObject(ProductEntity.Companion)
        }

        When("서브 이미지를 4개 모두 교체할 때") {
            val allChangeRequest =
                baseUpdateRequest.copy(
                    subImagesFinal =
                    listOf(
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileA),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileB),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileC),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockSubFileD),
                    ),
                )

            every { productPersistencePort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every { imageHandler.processSubImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyNewSubImageA,
                        dummyNewSubImageB,
                        dummyNewSubImageC,
                        dummyNewSubImageD,
                    )
            every { imageHandler.processAdditionalImagesFinal(any(), any(), any()) } returns
                    listOf(
                        dummyExistingAdditionalImage,
                    )

            mockkObject(ProductEntity.Companion)
            val spiedEntity = spyk(ProductEntity.fromDomain(existingProduct))
            every { ProductEntity.fromDomain(existingProduct) } returns spiedEntity

            every { productPersistencePort.save(any()) } answers {
                existingProduct.copy(
                    mainImage = dummyNewMainImage,
                    subImages =
                    listOf(
                        dummyNewSubImageA,
                        dummyNewSubImageB,
                        dummyNewSubImageC,
                        dummyNewSubImageD,
                    ),
                    updatedAt = LocalDateTime.now(),
                )
            }

            val result = useCase.updateProduct(allChangeRequest)

            Then("4개 모두 새로운 이미지로 교체된다") {
                result.subImages[0] shouldBe dummyNewSubImageA.url
                result.subImages[1] shouldBe dummyNewSubImageB.url
                result.subImages[2] shouldBe dummyNewSubImageC.url
                result.subImages[3] shouldBe dummyNewSubImageD.url
            }

            unmockkObject(ProductEntity.Companion)
        }
    }
})
