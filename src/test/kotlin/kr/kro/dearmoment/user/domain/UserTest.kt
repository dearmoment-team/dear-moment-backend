package kr.kro.dearmoment.user.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import java.time.LocalDateTime

class UserTest : DescribeSpec({

    describe("User 엔티티 생성") {

        context("모든 필드가 정상적이면") {
            it("정상적으로 객체가 생성된다.") {
                val now = LocalDateTime.now()
                val user =
                    User(
                        id = null,
                        loginId = "testUser",
                        password = "pass1234",
                        name = "홍길동",
                        isStudio = false,
                        createdAt = now,
                        updatedAt = now,
                    )
            }
        }

        context("모든 필드가 일부만 있더라도") {
            it("null 허용 필드는 없어도 정상 생성된다.") {
                val now = LocalDateTime.now()
                // isStudio, updatedAt, updatedUser를 null로
                val user =
                    User(
                        id = null,
                        loginId = "anotherUser",
                        password = "password1234",
                        name = "홍길동2",
                        isStudio = null,
                        createdAt = now,
                        updatedAt = null,
                    )
            }
        }

        // ---- 아래는 필수 필드 검증 테스트 ----

        context("loginId가 비어있으면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    User(
                        id = null,
                        loginId = "",
                        password = "pass1234",
                        name = "홍길동",
                        isStudio = false,
                        createdAt = LocalDateTime.now(),
                        updatedAt = null,
                    )
                }
            }
        }

        // ... 비밀번호, name, createdUser 등 필수 값 테스트 동일

        context("updatedAt이 존재하지만 createdAt 이후이면") {
            it("예외를 발생시킨다.") {
                val now = LocalDateTime.now()
                val future = now.plusHours(1)

                shouldThrow<IllegalArgumentException> {
                    User(
                        id = null,
                        loginId = "testUser",
                        password = "pass1234",
                        name = "홍길동",
                        isStudio = false,
                        createdAt = future,
                        updatedAt = now,
                    )
                }
            }
        }
    }
})
