package kr.kro.dearmoment.inquiry.domain

class AuthorInquiry(
    id: Long = 0L,
    userId: Long = 0L,
    val title: String,
    val content: String,
    val answered: Boolean = false,
) : Inquiry(id, userId) {
    init {
        require(title.isNotEmpty()) { "제목이 존재해야 합니다." }
        require(content.isNotEmpty()) { "내용이 존재해야 합니다." }
    }
}
