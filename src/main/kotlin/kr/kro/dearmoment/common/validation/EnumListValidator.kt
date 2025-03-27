package kr.kro.dearmoment.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumListValidator : ConstraintValidator<EnumValue, List<String>> {
    private lateinit var enumValues: Array<out Enum<*>>
    private var ignoreCase: Boolean = false

    override fun initialize(constraintAnnotation: EnumValue) {
        this.enumValues = constraintAnnotation.enumClass.java.enumConstants
        this.ignoreCase = constraintAnnotation.ignoreCase
    }

    override fun isValid(
        value: List<String>?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value.isNullOrEmpty()) return true // 값이 없으면 검증 패스

        return value.all { item ->
            enumValues.any { it.name.equals(item, ignoreCase) }
        }
    }
}
