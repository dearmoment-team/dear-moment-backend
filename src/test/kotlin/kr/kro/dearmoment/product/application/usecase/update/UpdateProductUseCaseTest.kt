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
import kr.kro.dearmoment.common.fixture.studioFixture
import kr.kro.dearmoment.image.application.handler.ImageHandler
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.dto.request.AdditionalImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.SubImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateAdditionalImageAction
import kr.kro.dearmoment.product.application.dto.request.UpdatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateSubImageAction
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.option.ProductOptionUseCase
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.application.port.output.GetStudioPort
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime
import java.util.UUID

class UpdateProductUseCaseTest : BehaviorSpec({

    // -- Mock 설정 --
    val productPersistencePort = mockk<ProductPersistencePort>()
    val imageHandler = mockk<ImageHandler>()
    val getProductPort = mockk<GetProductPort>()
    val getStudioPort = mockk<GetStudioPort>()
    val productOptionUseCase = mockk<ProductOptionUseCase>(relaxed = true)
    val studio = studioFixture(1L)
    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    // 실제 테스트 대상 UseCase
    val useCase =
        UpdateProductUseCaseImpl(
            productPersistencePort = productPersistencePort,
            imageHandler = imageHandler,
            productOptionUseCase = productOptionUseCase,
            getStudioPort = getStudioPort,
            getProductPort = getProductPort,
        )

    // -- 더미 데이터 --
    val dummyExistingMainImage =
        Image(
            imageId = 101L,
            userId = dummyUserId,
            parId = "",
            fileName = "existing_main.jpg",
            url = "http://example.com/existing_main.jpg",
        )
    val dummyExistingSubImage =
        Image(
            imageId = 200L,
            userId = dummyUserId,
            parId = "",
            fileName = "existing_sub.jpg",
            url = "http://example.com/existing_sub.jpg",
        )
    val dummyExistingAdditionalImage =
        Image(
            imageId = 300L,
            userId = dummyUserId,
            parId = "",
            fileName = "existing_add.jpg",
            url = "http://example.com/existing_add.jpg",
        )

    val existingProduct =
        Product(
            productId = 999L,
            userId = dummyUserId,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Existing Product",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF),
            cameraTypes = setOf(CameraType.FILM),
            retouchStyles = setOf(RetouchStyle.NATURAL),
            mainImage = dummyExistingMainImage,
            subImages = listOf(dummyExistingSubImage, dummyExistingSubImage, dummyExistingSubImage, dummyExistingSubImage),
            additionalImages = listOf(dummyExistingAdditionalImage),
            createdAt = LocalDateTime.now().minusDays(2),
            updatedAt = LocalDateTime.now().minusDays(1),
            options = emptyList(),
        )

    // 업데이트 요청 DTO (userId 필드 제거됨)
    val updateRequest =
        UpdateProductRequest(
            productId = 999L,
            studioId = 1L,
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "Updated Title",
            description = "Updated Description",
            availableSeasons = listOf("YEAR_2025_SECOND_HALF"),
            cameraTypes = listOf("DIGITAL"),
            retouchStyles = listOf("CALM"),
            subImagesFinal =
                listOf(
                    SubImageFinalRequest(UpdateSubImageAction.KEEP, 0, 200L),
                    SubImageFinalRequest(UpdateSubImageAction.DELETE, 1, 200L),
                    SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 2, null),
                    SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 3, null),
                ),
            additionalImagesFinal =
                listOf(
                    AdditionalImageFinalRequest(UpdateAdditionalImageAction.DELETE, 300L),
                    AdditionalImageFinalRequest(UpdateAdditionalImageAction.UPLOAD, null),
                ),
            detailedInfo = "Updated Detailed Info",
            contactInfo = "Updated Contact",
        )

    // 더미 파일
    val mainImageFileTest =
        MockMultipartFile(
            "mainImageFile",
            "new_main.jpg",
            "image/jpeg",
            "new main content".toByteArray(),
        )
    val subImageFileForUpload1 =
        MockMultipartFile(
            "subImageFiles",
            "sub3.jpg",
            "image/jpeg",
            "sub3-content".toByteArray(),
        )
    val subImageFileForUpload2 =
        MockMultipartFile(
            "subImageFiles",
            "sub4.jpg",
            "image/jpeg",
            "sub4-content".toByteArray(),
        )
    val additionalImageFileTest =
        MockMultipartFile(
            "additionalImageFiles",
            "add2.jpg",
            "image/jpeg",
            "add2-content".toByteArray(),
        )

    val dummyNewMainImage =
        Image(
            imageId = 201L,
            userId = dummyUserId,
            parId = "",
            fileName = "new_main.jpg",
            url = "http://example.com/new_main.jpg",
        )
    val dummyNewSubImage1 =
        Image(
            imageId = 202L,
            userId = dummyUserId,
            parId = "",
            fileName = "sub3.jpg",
            url = "http://example.com/sub3.jpg",
        )
    val dummyNewSubImage2 =
        Image(
            imageId = 203L,
            userId = dummyUserId,
            parId = "",
            fileName = "sub4.jpg",
            url = "http://example.com/sub4.jpg",
        )
    val dummyNewAdditionalImage =
        Image(
            imageId = 400L,
            userId = dummyUserId,
            parId = "",
            fileName = "add2.jpg",
            url = "http://example.com/add2.jpg",
        )

    val baseUpdateRequest = updateRequest

    // 서브 이미지 추가 더미 파일들
    val mockSubFileA = MockMultipartFile("subImageFiles", "subA.jpg", "image/jpeg", "subA-content".toByteArray())
    val mockSubFileB = MockMultipartFile("subImageFiles", "subB.jpg", "image/jpeg", "subB-content".toByteArray())
    val mockSubFileC = MockMultipartFile("subImageFiles", "subC.jpg", "image/jpeg", "subC-content".toByteArray())
    val mockSubFileD = MockMultipartFile("subImageFiles", "subD.jpg", "image/jpeg", "subD-content".toByteArray())

    val dummyExistingSubImage1 =
        Image(
            imageId = 201L,
            userId = dummyUserId,
            parId = "",
            fileName = "existing_sub1.jpg",
            url = "http://example.com/existing_sub1.jpg",
        )
    val dummyExistingSubImage2 =
        Image(
            imageId = 202L,
            userId = dummyUserId,
            parId = "",
            fileName = "existing_sub2.jpg",
            url = "http://example.com/existing_sub2.jpg",
        )
    val dummyExistingSubImage3 =
        Image(
            imageId = 203L,
            userId = dummyUserId,
            parId = "",
            fileName = "existing_sub3.jpg",
            url = "http://example.com/existing_sub3.jpg",
        )
    val dummyNewSubImageA =
        Image(
            imageId = 301L,
            userId = dummyUserId,
            parId = "",
            fileName = "new_subA.jpg",
            url = "http://example.com/new_subA.jpg",
        )
    val dummyNewSubImageB =
        Image(
            imageId = 302L,
            userId = dummyUserId,
            parId = "",
            fileName = "new_subB.jpg",
            url = "http://example.com/new_subB.jpg",
        )
    val dummyNewSubImageC =
        Image(
            imageId = 303L,
            userId = dummyUserId,
            parId = "",
            fileName = "new_subC.jpg",
            url = "http://example.com/new_subC.jpg",
        )
    val dummyNewSubImageD =
        Image(
            imageId = 304L,
            userId = dummyUserId,
            parId = "",
            fileName = "new_subD.jpg",
            url = "http://example.com/new_subD.jpg",
        )

    Given("updateProduct 메서드") {

        When("존재하지 않는 상품 ID 요청 시") {
            every { getProductPort.findById(999L) } returns null

            Then("예외 발생") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.updateProduct(
                            userId = dummyUserId,
                            productId = updateRequest.productId,
                            rawRequest = updateRequest,
                            mainImageFile = null,
                            subImageFiles = emptyList(),
                            additionalImageFiles = emptyList(),
                            options = emptyList(),
                        )
                    }
                exception.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
            }
        }

        When("정상 요청 시") {
            every { getStudioPort.findById(studio.id) } returns studio
            every { getProductPort.findById(999L) } returns existingProduct
            every {
                imageHandler.updateMainImage(mainImageFileTest, dummyUserId, existingProduct.mainImage)
            } returns dummyNewMainImage
            every {
                imageHandler.processSubImagesPartial(
                    currentSubImages = existingProduct.subImages,
                    finalRequests = updateRequest.subImagesFinal!!,
                    subImageFiles = listOf(subImageFileForUpload1, subImageFileForUpload2),
                    userId = dummyUserId,
                )
            } returns
                listOf(
                    dummyExistingSubImage,
                    dummyNewSubImage1,
                    dummyNewSubImage2,
                    dummyExistingSubImage,
                )
            every {
                imageHandler.processAdditionalImagesFinal(
                    currentAdditionalImages = existingProduct.additionalImages,
                    finalRequests = updateRequest.additionalImagesFinal ?: emptyList(),
                    additionalImageFiles = listOf(additionalImageFileTest),
                    userId = dummyUserId,
                )
            } returns listOf(dummyNewAdditionalImage)

            val realEntity = ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio))
            val spiedEntity = spyk(realEntity)
            mockkObject(ProductEntity.Companion)
            every { ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)) } returns spiedEntity
            every { productPersistencePort.save(any(), any()) } returns
                existingProduct.copy(
                    title = "Updated Title",
                    availableSeasons = setOf(ShootingSeason.YEAR_2025_SECOND_HALF),
                    cameraTypes = setOf(CameraType.DIGITAL),
                    retouchStyles = setOf(RetouchStyle.CALM),
                    mainImage = dummyNewMainImage,
                    subImages = listOf(dummyExistingSubImage, dummyNewSubImage1, dummyNewSubImage2, dummyExistingSubImage),
                    additionalImages = listOf(dummyNewAdditionalImage),
                    studio = studio,
                )

            val result =
                useCase.updateProduct(
                    userId = dummyUserId,
                    productId = updateRequest.productId,
                    rawRequest = updateRequest,
                    mainImageFile = mainImageFileTest,
                    subImageFiles = listOf(subImageFileForUpload1, subImageFileForUpload2),
                    additionalImageFiles = listOf(additionalImageFileTest),
                    options = emptyList(),
                )

            Then("결과 검증") {
                result.title shouldBe "Updated Title"
                result.availableSeasons shouldHaveSize 1
                result.retouchStyles.first() shouldBe "CALM"
                result.mainImage.url shouldBe "http://example.com/new_main.jpg"
            }

            Then("상호작용 검증") {
                verify(exactly = 1) { imageHandler.updateMainImage(any(), any(), any()) }
                verify(exactly = 1) { imageHandler.processSubImagesPartial(any(), any(), any(), any()) }
                verify(exactly = 1) { productPersistencePort.save(any(), any()) }
            }

            unmockkObject(ProductEntity.Companion)
        }

        When("서브 이미지를 1개만 교체하고, 나머지 3개는 그대로 유지할 때") {
            val oneChangeRequest =
                baseUpdateRequest.copy(
                    subImagesFinal =
                        listOf(
                            SubImageFinalRequest(UpdateSubImageAction.KEEP, 0, dummyExistingSubImage1.imageId),
                            SubImageFinalRequest(UpdateSubImageAction.KEEP, 1, dummyExistingSubImage2.imageId),
                            SubImageFinalRequest(UpdateSubImageAction.KEEP, 2, dummyExistingSubImage3.imageId),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 3, null),
                        ),
                )
            every { getStudioPort.findById(studio.id) } returns studio
            every { getProductPort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every {
                imageHandler.processSubImagesPartial(
                    any(),
                    oneChangeRequest.subImagesFinal!!,
                    listOf(mockSubFileD),
                    dummyUserId,
                )
            } returns
                listOf(
                    dummyExistingSubImage1,
                    dummyExistingSubImage2,
                    dummyExistingSubImage3,
                    dummyNewSubImageD,
                )
            every {
                imageHandler.processAdditionalImagesFinal(
                    any(),
                    any(),
                    any(),
                    dummyUserId,
                )
            } returns listOf(dummyExistingAdditionalImage)

            mockkObject(ProductEntity.Companion)
            val spiedEntity = spyk(ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)))
            every { ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)) } returns spiedEntity

            every { productPersistencePort.save(any(), any()) } answers {
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

            val result =
                useCase.updateProduct(
                    userId = dummyUserId,
                    productId = oneChangeRequest.productId,
                    rawRequest = oneChangeRequest,
                    mainImageFile = null,
                    subImageFiles = listOf(mockSubFileD),
                    additionalImageFiles = emptyList(),
                    options = emptyList(),
                )

            Then("서브 이미지 4개 중 3개는 기존 그대로, 1개만 새로운 이미지로 교체") {
                result.subImages shouldHaveSize 4
                result.subImages[0].url shouldBe dummyExistingSubImage1.url
                result.subImages[1].url shouldBe dummyExistingSubImage2.url
                result.subImages[2].url shouldBe dummyExistingSubImage3.url
                result.subImages[3].url shouldBe dummyNewSubImageD.url
            }

            unmockkObject(ProductEntity.Companion)
        }

        When("서브 이미지를 2개만 교체하고, 나머지 2개는 그대로 유지할 때") {
            val twoChangeRequest =
                baseUpdateRequest.copy(
                    subImagesFinal =
                        listOf(
                            SubImageFinalRequest(UpdateSubImageAction.KEEP, 0, dummyExistingSubImage1.imageId),
                            SubImageFinalRequest(UpdateSubImageAction.KEEP, 1, dummyExistingSubImage2.imageId),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 2, null),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 3, null),
                        ),
                )

            every { getStudioPort.findById(studio.id) } returns studio
            every { getProductPort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every {
                imageHandler.processSubImagesPartial(
                    any(),
                    twoChangeRequest.subImagesFinal!!,
                    listOf(mockSubFileC, mockSubFileD),
                    dummyUserId,
                )
            } returns
                listOf(
                    dummyExistingSubImage1,
                    dummyExistingSubImage2,
                    dummyNewSubImageC,
                    dummyNewSubImageD,
                )
            every {
                imageHandler.processAdditionalImagesFinal(
                    any(),
                    any(),
                    any(),
                    dummyUserId,
                )
            } returns listOf(dummyExistingAdditionalImage)

            mockkObject(ProductEntity.Companion)
            val spiedEntity = spyk(ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)))
            every { ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)) } returns spiedEntity

            every { productPersistencePort.save(any(), any()) } answers {
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

            val result =
                useCase.updateProduct(
                    userId = dummyUserId,
                    productId = twoChangeRequest.productId,
                    rawRequest = twoChangeRequest,
                    mainImageFile = null,
                    subImageFiles = listOf(mockSubFileC, mockSubFileD),
                    additionalImageFiles = emptyList(),
                    options = emptyList(),
                )

            Then("서브 이미지 4개 중 2개는 기존 그대로, 2개가 새 파일로 업로드된다") {
                result.subImages[0].url shouldBe dummyExistingSubImage1.url
                result.subImages[1].url shouldBe dummyExistingSubImage2.url
                result.subImages[2].url shouldBe dummyNewSubImageC.url
                result.subImages[3].url shouldBe dummyNewSubImageD.url
            }

            unmockkObject(ProductEntity.Companion)
        }

        When("서브 이미지를 3개만 교체하고, 나머지 1개는 그대로 유지할 때") {
            val threeChangeRequest =
                baseUpdateRequest.copy(
                    subImagesFinal =
                        listOf(
                            SubImageFinalRequest(UpdateSubImageAction.KEEP, 0, dummyExistingSubImage1.imageId),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 1, null),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 2, null),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 3, null),
                        ),
                )

            every { getStudioPort.findById(studio.id) } returns studio
            every { getProductPort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every {
                imageHandler.processSubImagesPartial(
                    any(),
                    threeChangeRequest.subImagesFinal!!,
                    listOf(mockSubFileA, mockSubFileB, mockSubFileC),
                    dummyUserId,
                )
            } returns
                listOf(
                    dummyExistingSubImage1,
                    dummyNewSubImageA,
                    dummyNewSubImageB,
                    dummyNewSubImageC,
                )
            every {
                imageHandler.processAdditionalImagesFinal(any(), any(), any(), any())
            } returns listOf(dummyExistingAdditionalImage)

            mockkObject(ProductEntity.Companion)
            val spiedEntity = spyk(ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)))
            every { ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)) } returns spiedEntity

            every { productPersistencePort.save(any(), any()) } answers {
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

            val result =
                useCase.updateProduct(
                    userId = dummyUserId,
                    productId = threeChangeRequest.productId,
                    rawRequest = threeChangeRequest,
                    mainImageFile = null,
                    subImageFiles = listOf(mockSubFileA, mockSubFileB, mockSubFileC),
                    additionalImageFiles = emptyList(),
                    options = emptyList(),
                )

            Then("서브 이미지 4개 중 1개는 기존 그대로, 3개가 새로운 이미지로 교체된다") {
                result.subImages[0].url shouldBe dummyExistingSubImage1.url
                result.subImages[1].url shouldBe dummyNewSubImageA.url
                result.subImages[2].url shouldBe dummyNewSubImageB.url
                result.subImages[3].url shouldBe dummyNewSubImageC.url
            }

            unmockkObject(ProductEntity.Companion)
        }

        When("서브 이미지를 4개 모두 교체할 때") {
            val allChangeRequest =
                baseUpdateRequest.copy(
                    subImagesFinal =
                        listOf(
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 0, null),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 1, null),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 2, null),
                            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, 3, null),
                        ),
                )

            every { getStudioPort.findById(studio.id) } returns studio
            every { getProductPort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every {
                imageHandler.processSubImagesPartial(
                    any(),
                    allChangeRequest.subImagesFinal!!,
                    listOf(mockSubFileA, mockSubFileB, mockSubFileC, mockSubFileD),
                    dummyUserId,
                )
            } returns
                listOf(
                    dummyNewSubImageA,
                    dummyNewSubImageB,
                    dummyNewSubImageC,
                    dummyNewSubImageD,
                )
            every {
                imageHandler.processAdditionalImagesFinal(any(), any(), any(), any())
            } returns listOf(dummyExistingAdditionalImage)

            mockkObject(ProductEntity.Companion)
            val spiedEntity = spyk(ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)))
            every { ProductEntity.fromDomain(existingProduct, StudioEntity.from(studio)) } returns spiedEntity

            every { productPersistencePort.save(any(), any()) } answers {
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

            val result =
                useCase.updateProduct(
                    userId = dummyUserId,
                    productId = allChangeRequest.productId,
                    rawRequest = allChangeRequest,
                    mainImageFile = null,
                    subImageFiles = listOf(mockSubFileA, mockSubFileB, mockSubFileC, mockSubFileD),
                    additionalImageFiles = emptyList(),
                    options = emptyList(),
                )

            Then("4개 모두 새로운 이미지로 교체된다") {
                result.subImages[0].url shouldBe dummyNewSubImageA.url
                result.subImages[1].url shouldBe dummyNewSubImageB.url
                result.subImages[2].url shouldBe dummyNewSubImageC.url
                result.subImages[3].url shouldBe dummyNewSubImageD.url
            }

            unmockkObject(ProductEntity.Companion)
        }

        // 옵션 관련 시나리오: 수정 + 신규 추가
        When("옵션 수정 및 신규 옵션 추가 시나리오") {
            val partnerShopRequest =
                UpdatePartnerShopRequest(
                    category = "DRESS",
                    name = "Partner1",
                    link = "http://partner1.com",
                )
            val updatedOption1 =
                UpdateProductOptionRequest(
                    optionId = 1L,
                    name = "Option1 Updated",
                    optionType = "SINGLE",
                    discountAvailable = false,
                    originalPrice = 100000,
                    discountPrice = 80000,
                    description = "Updated Option 1",
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 2,
                    shootingMinutes = 30,
                    retouchedCount = 1,
                    originalProvided = true,
                    partnerShops = emptyList(),
                )
            val newOption =
                UpdateProductOptionRequest(
                    optionId = null,
                    name = "New Option",
                    optionType = "PACKAGE",
                    discountAvailable = true,
                    originalPrice = 200000,
                    discountPrice = 150000,
                    description = "New option added",
                    costumeCount = 0,
                    shootingLocationCount = 0,
                    shootingHours = 0,
                    shootingMinutes = 0,
                    retouchedCount = 0,
                    originalProvided = false,
                    partnerShops = listOf(partnerShopRequest),
                )
            val optionRequestList = listOf(updatedOption1, newOption)
            val expectedOptionResponses =
                optionRequestList.map {
                    ProductOptionResponse.fromDomain(UpdateProductOptionRequest.toDomain(it, 999L))
                }

            every { getProductPort.findById(999L) } returns existingProduct
            every { imageHandler.updateMainImage(any(), any(), any()) } returns dummyNewMainImage
            every { imageHandler.processSubImagesPartial(any(), any(), any(), any()) } returns
                listOf(
                    dummyExistingSubImage,
                    dummyExistingSubImage,
                    dummyExistingSubImage,
                    dummyExistingSubImage,
                )
            every { imageHandler.processAdditionalImagesFinal(any(), any(), any(), any()) } returns
                listOf(
                    dummyExistingAdditionalImage,
                )
            every { productPersistencePort.save(any(), any()) } returns
                existingProduct.copy(
                    title = "Updated Title",
                    availableSeasons = setOf(ShootingSeason.YEAR_2025_SECOND_HALF),
                    cameraTypes = setOf(CameraType.DIGITAL),
                    retouchStyles = setOf(RetouchStyle.CALM),
                    mainImage = dummyNewMainImage,
                    subImages = listOf(dummyExistingSubImage, dummyExistingSubImage, dummyExistingSubImage, dummyExistingSubImage),
                    additionalImages = listOf(dummyExistingAdditionalImage),
                    options = optionRequestList.map { UpdateProductOptionRequest.toDomain(it, 999L) },
                )

            val result =
                useCase.updateProduct(
                    userId = dummyUserId,
                    productId = updateRequest.productId,
                    rawRequest = updateRequest,
                    mainImageFile = mainImageFileTest,
                    subImageFiles = listOf(subImageFileForUpload1, subImageFileForUpload2),
                    additionalImageFiles = listOf(additionalImageFileTest),
                    options = optionRequestList,
                )

            Then("옵션 동기화가 호출되어 옵션들이 수정/추가된다") {
                verify(exactly = 1) { productOptionUseCase.synchronizeOptions(existingProduct, optionRequestList) }
                result.options shouldBe expectedOptionResponses
            }
        }
    }
})
