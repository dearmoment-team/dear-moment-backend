package kr.kro.dearmoment.common.validation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import jakarta.validation.Validation
import jakarta.validation.Validator

enum class Fruit {
    APPLE,
    BANANA,
    ORANGE,
}

data class SampleRequest(
    @field:EnumValue(enumClass = Fruit::class, message = "Invalid enum value")
    val fruit: String,
    @field:EnumValue(enumClass = Fruit::class, message = "Invalid enum list value")
    val fruitList: List<String>,
)

data class SampleRequestIgnoreCase(
    @EnumValue(enumClass = Fruit::class, ignoreCase = true)
    val fruit: String,
    @EnumValue(enumClass = Fruit::class, ignoreCase = true)
    val fruitList: List<String>,
)

class EnumValidatorTest : FunSpec({
    lateinit var validator: Validator

    beforeTest {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    test("유효한 단일 Enum 값은 검증을 통과해야 한다") {
        val request = SampleRequest(fruit = "APPLE", fruitList = listOf("BANANA", "ORANGE"))

        val violations = validator.validate(request)

        violations.shouldBeEmpty()
    }

    test("잘못된 단일 Enum 값은 검증에서 실패해야 한다") {
        val request = SampleRequest(fruit = "MANGO", fruitList = listOf("BANANA", "ORANGE"))

        val violations = validator.validate(request)

        violations.shouldNotBeEmpty()
    }

    test("유효한 Enum 리스트는 검증을 통과해야 한다") {
        val request = SampleRequest(fruit = "APPLE", fruitList = listOf("BANANA", "ORANGE"))

        val violations = validator.validate(request)

        violations.shouldBeEmpty()
    }

    test("잘못된 Enum 리스트는 검증에서 실패해야 한다") {
        val request = SampleRequest(fruit = "APPLE", fruitList = listOf("BANANA", "GRAPE"))

        val violations = validator.validate(request)

        violations.shouldNotBeEmpty()
    }

    test("대소문자 무시 옵션을 활성화하면 검증을 통과해야 한다") {
        val request = SampleRequestIgnoreCase(fruit = "apple", fruitList = listOf("banana", "orange"))

        val violations = validator.validate(request)

        violations.shouldBeEmpty()
    }
})
