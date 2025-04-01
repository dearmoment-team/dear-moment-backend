package kr.kro.dearmoment.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<EnumValue, Any> {
    private lateinit var enumValue: EnumValue

    override fun initialize(constraintAnnotation: EnumValue) {
        this.enumValue = constraintAnnotation
    }

    override fun isValid(
        value: Any?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) return true // null 허용

        val enumConstants = enumValue.enumClass.java.enumConstants ?: return false

        return when (value) {
            is String -> validateEnum(value, enumConstants)
            is Collection<*> -> value.all { it is String && validateEnum(it, enumConstants) }
            else -> false
        }
    }

    private fun validateEnum(
        value: String,
        enumConstants: Array<out Enum<*>>,
    ): Boolean {
        val trimmedValue = value.trim()
        return enumConstants.any { enumConstant ->
            convertible(trimmedValue, enumConstant) || convertibleIgnoreCase(trimmedValue, enumConstant)
        }
    }

    private fun convertibleIgnoreCase(
        value: String,
        enumConstant: Enum<*>,
    ): Boolean {
        return enumValue.ignoreCase && value.equals(enumConstant.name, ignoreCase = true)
    }

    private fun convertible(
        value: String,
        enumConstant: Enum<*>,
    ): Boolean {
        return value == enumConstant.name
    }
}
