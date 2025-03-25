package kr.kro.dearmoment.user.application.port.output

import kr.kro.dearmoment.user.domain.User

interface GetUserByKakaoIdPort {
    fun findByKakaoId(kakaoId: Long): User?
}
