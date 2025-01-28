package kr.kro.dearmoment.product.application.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.util.*

class ProductOptionUseCaseTest : DescribeSpec({

    val productOptionPort = mockk<ProductOptionPersistencePort>()
    val productRetrievalPort = mockk<ProductEntityRetrievalPort>()
    val useCase = ProductOptionUseCase(productOptionPort, productRetrievalPort)

    // 공통 테스트 데이터
    val validProduct = Product(
        productId = 1L,
        title = "Test Product",
        price = 10000,
        typeCode = 1
    )

    val validOption = ProductOption(
        productId = 1L,
        name = "Test Option",
        additionalPrice = 5000
    )

    afterEach {
        clearAllMocks()
    }

    describe("saveProductOption") {
        context("유효한 상품 ID와 새로운 옵션 이름으로 요청 시") {
            beforeEach {
                every { productRetrievalPort.getProductById(1L) } returns validProduct
                every { productOptionPort.findByProduct(validProduct) } returns emptyList()
                every { productOptionPort.save(validOption) } returns validOption
            }

            it("옵션 저장 성공") {
                val result = useCase.saveProductOption(validOption)
                result shouldBe validOption
                verify(exactly = 1) { productOptionPort.save(validOption) }
            }
        }

        context("존재하지 않는 상품 ID 요청 시") {
            beforeEach {
                every { productRetrievalPort.getProductById(999L) } returns null
            }

            it("예외 발생") {
                val exception = shouldThrow<IllegalArgumentException> {
                    useCase.saveProductOption(validOption.copy(productId = 999L))
                }
                exception.message shouldBe "Product with ID 999 not found"
            }
        }

        context("기존 옵션과 중복된 이름 요청 시") {
            beforeEach {
                every { productRetrievalPort.getProductById(1L) } returns validProduct
                every { productOptionPort.findByProduct(validProduct) } returns listOf(
                    validOption.copy(optionId = 1L)
                )
            }

            it("예외 발생") {
                val exception = shouldThrow<IllegalArgumentException> {
                    useCase.saveProductOption(validOption)
                }
                exception.message shouldBe "Duplicate option name: Test Option"
            }
        }
    }

    describe("getProductOptionById") {
        context("존재하는 옵션 ID 조회 시") {
            beforeEach {
                every { productOptionPort.findById(1L) } returns validOption
            }

            it("옵션 반환") {
                val result = useCase.getProductOptionById(1L)
                result shouldBe validOption
            }
        }

        context("존재하지 않는 옵션 ID 조회 시") {
            beforeEach {
                every { productOptionPort.findById(999L) } throws NoSuchElementException()
            }

            it("예외 전파") {
                shouldThrow<NoSuchElementException> {
                    useCase.getProductOptionById(999L)
                }
            }
        }
    }

    describe("getAllProductOptions") {
        context("옵션이 존재할 때") {
            val options = listOf(
                validOption.copy(optionId = 1L),
                validOption.copy(optionId = 2L, name = "Option 2")
            )

            beforeEach {
                every { productOptionPort.findAll() } returns options
            }

            it("전체 옵션 리스트 반환") {
                val result = useCase.getAllProductOptions()
                result.size shouldBe 2
                result.map { it.name } shouldBe listOf("Test Option", "Option 2")
            }
        }

        context("옵션이 없을 때") {
            beforeEach {
                every { productOptionPort.findAll() } returns emptyList()
            }

            it("빈 리스트 반환") {
                useCase.getAllProductOptions() shouldBe emptyList()
            }
        }
    }

    describe("deleteProductOptionById") {
        context("유효한 옵션 ID 삭제 시") {
            beforeEach {
                every { productOptionPort.deleteById(1L) } just Runs
            }

            it("삭제 메서드 호출 확인") {
                useCase.deleteProductOptionById(1L)
                verify(exactly = 1) { productOptionPort.deleteById(1L) }
            }
        }
    }

    describe("getProductOptionsByProductId") {
        context("유효한 상품 ID 요청 시") {
            val options = listOf(
                validOption.copy(optionId = 1L),
                validOption.copy(optionId = 2L)
            )

            beforeEach {
                every { productRetrievalPort.getProductById(1L) } returns validProduct
                every { productOptionPort.findByProduct(validProduct) } returns options
            }

            it("상품별 옵션 리스트 반환") {
                val result = useCase.getProductOptionsByProductId(1L)
                result.size shouldBe 2
            }
        }

        context("존재하지 않는 상품 ID 요청 시") {
            beforeEach {
                every { productRetrievalPort.getProductById(999L) } returns null
            }

            it("예외 발생") {
                val exception = shouldThrow<IllegalArgumentException> {
                    useCase.getProductOptionsByProductId(999L)
                }
                exception.message shouldBe "Product with ID 999 not found"
            }
        }
    }

    describe("deleteAllProductOptionsByProductId") {
        context("유효한 상품 ID 삭제 요청 시") {
            beforeEach {
                every { productOptionPort.deleteAllByProductId(1L) } just Runs
            }

            it("일괄 삭제 메서드 호출 확인") {
                useCase.deleteAllProductOptionsByProductId(1L)
                verify(exactly = 1) { productOptionPort.deleteAllByProductId(1L) }
            }
        }
    }

    describe("existsProductOptions") {
        context("옵션이 존재하는 상품 ID 요청 시") {
            beforeEach {
                every { productOptionPort.existsByProductId(1L) } returns true
            }

            it("true 반환") {
                useCase.existsProductOptions(1L) shouldBe true
            }
        }

        context("옵션이 없는 상품 ID 요청 시") {
            beforeEach {
                every { productOptionPort.existsByProductId(2L) } returns false
            }

            it("false 반환") {
                useCase.existsProductOptions(2L) shouldBe false
            }
        }
    }
})