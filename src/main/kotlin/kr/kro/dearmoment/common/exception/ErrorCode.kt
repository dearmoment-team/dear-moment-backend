package kr.kro.dearmoment.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),

    // Image
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    IMAGE_DELETE_FAIL_FROM_OBJECT_STORAGE(HttpStatus.INTERNAL_SERVER_ERROR, "오브젝트 스토리지에서 이미지 삭제를 실패 했습니다."),
    INVALID_SUB_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "서브 이미지는 정확히 4장이어야 합니다."),
    INVALID_ADDITIONAL_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "추가 이미지는 최대 5장까지만 가능합니다."),

    // Inquiry
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다."),

    // Studio
    STUDIO_NOT_FOUND(HttpStatus.NOT_FOUND, "스튜디오를 찾을 수 없습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    PRODUCT_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품 옵션입니다."),
    PRODUCT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "동일 제목의 상품이 이미 존재합니다."),
    SAVED_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "저장된 상품을 찾을 수 없습니다."),

    // Option
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 옵션입니다."),
    DUPLICATE_OPTION_NAME(HttpStatus.BAD_REQUEST, "중복된 옵션명입니다."),
    INVALID_OPTION_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 옵션 타입입니다."),

    // PartnerShop
    INVALID_PARTNER_SHOP_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 제휴 업체 카테고리입니다."),

    // LIKE
    LIKE_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 좋아요가 존재합니다."),
    LIKE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 좋아요 입니다."),

    // Validation
    INVALID_PRODUCT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 상품 유형입니다."),
    INVALID_SHOOTING_PLACE(HttpStatus.BAD_REQUEST, "유효하지 않은 촬영 장소입니다."),
    INVALID_SEASON(HttpStatus.BAD_REQUEST, "유효하지 않은 촬영 가능 시기입니다."),
    INVALID_CAMERA_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 카메라 타입입니다."),
    INVALID_RETOUCH_STYLE(HttpStatus.BAD_REQUEST, "유효하지 않은 보정 스타일입니다."),
}
