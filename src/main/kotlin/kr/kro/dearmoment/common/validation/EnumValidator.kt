package kr.kro.dearmoment.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<EnumValue, String> {
    private lateinit var enumValue: EnumValue

    override fun initialize(constraintAnnotation: EnumValue) {
        this.enumValue = constraintAnnotation
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value.isNullOrBlank()) {
            return false
        }

        val enumConstants = enumValue.enumClass.java.enumConstants ?: return false

        return enumConstants.any { enumConstant ->
            convertible(value, enumConstant) || convertibleIgnoreCase(value, enumConstant)
        }
    }

    private fun convertibleIgnoreCase(
        value: String,
        enumConstant: Enum<*>,
    ): Boolean {
        return enumValue.ignoreCase && value.trim().equals(enumConstant.name, ignoreCase = true)
    }

    private fun convertible(
        value: String,
        enumConstant: Enum<*>,
    ): Boolean {
        return value.trim() == enumConstant.name
    }
}
