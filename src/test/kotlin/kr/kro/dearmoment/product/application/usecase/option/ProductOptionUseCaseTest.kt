package kr.kro.dearmoment.product.application.usecase.option

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.port.out.GetProductOptionPort
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.option.OptionType
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import java.time.LocalDateTime
import java.util.UUID

class ProductOptionUseCaseTest : BehaviorSpec({

    // 목 객체 생성
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>()
    val getProductOptionPort = mockk<GetProductOptionPort>()
    val getProductPort = mockk<GetProductPort>()
    val useCase: ProductOptionUseCase =
        ProductOptionUseCaseImpl(
            productOptionPersistencePort,
            getProductOptionPort,
            getProductPort,
        )

    // dummy user ID (UUID)
    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    // mockProduct: Product의 userId를 UUID로 설정
    val mockProduct =
        Product(
            productId = 1L,
            userId = dummyUserId,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Test Product",
            availableSeasons = emptySet(),
            cameraTypes = emptySet(),
            retouchStyles = emptySet(),
            mainImage = Image(userId = dummyUserId, fileName = "main.jpg", url = "http://example.com/main.jpg"),
            subImages =
                listOf(
                    Image(userId = dummyUserId, fileName = "sub1.jpg", url = "http://example.com/sub1.jpg"),
                    Image(userId = dummyUserId, fileName = "sub2.jpg", url = "http://example.com/sub2.jpg"),
                    Image(userId = dummyUserId, fileName = "sub3.jpg", url = "http://example.com/sub3.jpg"),
                    Image(userId = dummyUserId, fileName = "sub4.jpg", url = "http://example.com/sub4.jpg"),
                ),
            additionalImages = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList(),
        )

    val validRequest =
        CreateProductOptionRequest(
            name = "Option 1",
            optionType = "SINGLE",
            discountAvailable = false,
            originalPrice = 5000,
            discountPrice = 0,
            description = "Test option",
            costumeCount = 1,
            shootingLocationCount = 1,
            shootingHours = 1,
            shootingMinutes = 0,
            retouchedCount = 1,
            partnerShops = emptyList(),
        )

    val savedDomainOption =
        ProductOption(
            optionId = 1L,
            productId = 1L,
            name = "Option 1",
            optionType = OptionType.SINGLE,
            discountAvailable = false,
            originalPrice = 5000,
            discountPrice = 0,
            description = "Test option",
            costumeCount = 1,
            shootingLocationCount = 1,
            shootingHours = 1,
            shootingMinutes = 0,
            retouchedCount = 1,
            partnerShops = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

    Given("saveProductOption") {
        When("존재하지 않는 productId로 요청 시") {
            every { getProductPort.findById(999L) } returns null

            Then("CustomException 발생") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.saveProductOption(999L, validRequest)
                    }
                exception shouldHaveMessage ErrorCode.PRODUCT_NOT_FOUND.message
            }
        }

        When("유효한 옵션 정보일 경우") {
            every { getProductPort.findById(1L) } returns mockProduct
            every { getProductOptionPort.existsByProductIdAndName(1L, "Option 1") } returns false
            // 정적 팩토리 메서드를 사용하여 도메인 객체로 변환한 후 저장
            every { productOptionPersistencePort.save(any(), any()) } returns savedDomainOption

            Then("옵션이 정상 저장되고 응답 DTO 반환") {
                val response = useCase.saveProductOption(1L, validRequest)
                response shouldBe ProductOptionResponse.fromDomain(savedDomainOption)
                verify(exactly = 1) {
                    productOptionPersistencePort.save(match { it.optionId == 0L }, mockProduct)
                }
            }
        }
    }

    Given("getProductOptionById") {
        When("존재하는 옵션 ID로 요청 시") {
            every { getProductOptionPort.findById(1L) } returns savedDomainOption

            Then("응답 DTO 반환") {
                val response = useCase.getProductOptionById(1L)
                response shouldBe ProductOptionResponse.fromDomain(savedDomainOption)
            }
        }

        When("존재하지 않는 옵션 ID로 요청 시") {
            every { getProductOptionPort.findById(999L) } throws CustomException(ErrorCode.OPTION_NOT_FOUND)

            Then("CustomException 발생") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.getProductOptionById(999L)
                    }
                exception shouldHaveMessage ErrorCode.OPTION_NOT_FOUND.message
            }
        }
    }

    Given("getProductOptionsByProductId") {
        val options =
            listOf(
                savedDomainOption,
                savedDomainOption.copy(optionId = 2L, name = "Option 2"),
            )
        When("특정 상품의 옵션 조회 시") {
            every { getProductOptionPort.findByProductId(1L) } returns options

            Then("DTO 리스트 반환") {
                val responses = useCase.getProductOptionsByProductId(1L)
                responses shouldHaveSize 2
                responses[0].name shouldBe "Option 1"
                responses[1].name shouldBe "Option 2"
            }
        }
    }

    Given("existsProductOptions") {
        When("옵션이 존재하는 경우") {
            every { getProductOptionPort.existsByProductId(1L) } returns true
            Then("true 반환") {
                useCase.existsProductOptions(1L) shouldBe true
            }
        }
        When("옵션이 존재하지 않는 경우") {
            every { getProductOptionPort.existsByProductId(1L) } returns false
            Then("false 반환") {
                useCase.existsProductOptions(1L) shouldBe false
            }
        }
    }

    Given("getAllProductOptions") {
        When("저장된 옵션이 여러 개 존재하면") {
            val allOptions = listOf(savedDomainOption, savedDomainOption.copy(optionId = 2L, name = "Option 2"))
            every { getProductOptionPort.findAll() } returns allOptions

            Then("전체 옵션 리스트 DTO 반환") {
                val responses = useCase.getAllProductOptions()
                responses shouldHaveSize 2
                responses[0].name shouldBe "Option 1"
                responses[1].name shouldBe "Option 2"
            }
        }

        When("저장된 옵션이 없으면") {
            every { getProductOptionPort.findAll() } returns emptyList()

            Then("빈 리스트 반환") {
                useCase.getAllProductOptions() shouldBe emptyList()
            }
        }
    }

    Given("deleteProductOptionById") {
        When("특정 옵션 삭제 시") {
            every { productOptionPersistencePort.deleteById(1L) } returns Unit

            Then("삭제 메서드가 호출된다") {
                useCase.deleteProductOptionById(1L)
                verify { productOptionPersistencePort.deleteById(1L) }
            }
        }
    }

    Given("deleteAllProductOptionsByProductId") {
        When("특정 상품의 모든 옵션 삭제 시") {
            every { productOptionPersistencePort.deleteAllByProductId(1L) } returns Unit

            Then("삭제 메서드가 호출된다") {
                useCase.deleteAllProductOptionsByProductId(1L)
                verify { productOptionPersistencePort.deleteAllByProductId(1L) }
            }
        }
    }

    Given("saveOrUpdateProductOption") {
        When("요청 DTO가 null이면 null 반환") {
            every { getProductPort.findById(1L) } returns mockProduct
            Then("null 반환") {
                useCase.saveOrUpdateProductOption(1L, null) shouldBe null
            }
        }

        When("요청 DTO의 optionId가 null이면 신규 옵션 추가") {
            val newOptionRequest =
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
                    partnerShops =
                        listOf(
                            UpdatePartnerShopRequest(
                                category = "DRESS",
                                name = "Partner1",
                                link = "http://partner1.com",
                            ),
                        ),
                )
            val newOptionDomain = UpdateProductOptionRequest.toDomain(newOptionRequest, 1L)
            every { getProductPort.findById(1L) } returns mockProduct
            every { productOptionPersistencePort.save(newOptionDomain, mockProduct) } returns newOptionDomain.copy(optionId = 3L)

            Then("신규 옵션 추가 후 DTO 반환") {
                val response = useCase.saveOrUpdateProductOption(1L, newOptionRequest)
                response shouldBe ProductOptionResponse.fromDomain(newOptionDomain.copy(optionId = 3L))
            }
        }

        When("요청 DTO의 optionId가 존재하면 기존 옵션 업데이트") {
            val updateOptionRequest =
                UpdateProductOptionRequest(
                    optionId = 1L,
                    name = "Option Updated",
                    optionType = "SINGLE",
                    discountAvailable = false,
                    originalPrice = 6000,
                    discountPrice = 5000,
                    description = "Option Updated Description",
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 1,
                    shootingMinutes = 0,
                    retouchedCount = 1,
                    originalProvided = true,
                    partnerShops = emptyList(),
                )
            val existingOption = savedDomainOption
            // 수정 후 description을 "Option Updated Description"으로 업데이트
            val updatedOption =
                existingOption.copy(
                    name = "Option Updated",
                    originalPrice = 6000,
                    discountPrice = 5000,
                    description = "Option Updated Description",
                )
            every { getProductPort.findById(1L) } returns mockProduct
            every { getProductOptionPort.findById(1L) } returns existingOption
            every { productOptionPersistencePort.save(updatedOption, mockProduct) } returns updatedOption

            Then("기존 옵션 업데이트 후 DTO 반환") {
                val response = useCase.saveOrUpdateProductOption(1L, updateOptionRequest)
                response shouldBe ProductOptionResponse.fromDomain(updatedOption)
            }
        }

        When("상품이 존재하지 않는 경우 saveOrUpdateProductOption 호출 시") {
            every { getProductPort.findById(999L) } returns null

            Then("CustomException 발생") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.saveOrUpdateProductOption(
                            999L,
                            validRequest.let {
                                UpdateProductOptionRequest(
                                    optionId = null,
                                    name = it.name,
                                    optionType = it.optionType,
                                    discountAvailable = it.discountAvailable,
                                    originalPrice = it.originalPrice,
                                    discountPrice = it.discountPrice,
                                    description = it.description,
                                    costumeCount = it.costumeCount,
                                    shootingLocationCount = it.shootingLocationCount,
                                    shootingHours = it.shootingHours,
                                    shootingMinutes = it.shootingMinutes,
                                    retouchedCount = it.retouchedCount,
                                    originalProvided = true,
                                    partnerShops = emptyList(),
                                )
                            },
                        )
                    }
                exception shouldHaveMessage ErrorCode.PRODUCT_NOT_FOUND.message
            }
        }
    }
})
