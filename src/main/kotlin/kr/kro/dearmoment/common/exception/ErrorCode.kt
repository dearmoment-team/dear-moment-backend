package kr.kro.dearmoment.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    // 이미지
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    IMAGE_DELETE_FAIL_FROM_OBJECT_STORAGE(HttpStatus.INTERNAL_SERVER_ERROR, "오브젝트 스토리지에서 이미지 삭제를 실패 했습니다."),

    // 문의
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다."),

    // 스튜디오
    STUDIO_NOT_FOUND(HttpStatus.NOT_FOUND, "스튜디오를 찾을 수 없습니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),

    // 상품 옵션
    PRODUCT_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품 옵션입니다."),
    PRODUCT_OPTION_ID_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "상품 옵션 ID가 널입니다."),

    // 요청 오류
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
}
