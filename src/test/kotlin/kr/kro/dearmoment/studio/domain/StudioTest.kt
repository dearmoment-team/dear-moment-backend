package kr.kro.dearmoment.studio.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class StudioTest : DescribeSpec({

    describe("스튜디오는 생성시") {
        context("스튜디오 이름이 빈 값이면") {
            it("예외를 발생 시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    Studio(
                        userId = 1L,
                        name = "",
                        contact = "010-1234-5678",
                        studioIntro = "스튜디오 소개글",
                        artistsIntro = "작가 소개글",
                        instagramUrl = "인스타 url",
                        kakaoChannelUrl = "카카오톡 채널 url",
                        reservationNotice = "",
                        cancellationPolicy = "",
                        status = StudioStatus.ACTIVE,
                    )
                }
            }
        }

        context("연락처가 빈 값이면") {
            it("예외를 발생 시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    Studio(
                        userId = 1L,
                        name = "스튜디오",
                        contact = "",
                        studioIntro = "스튜디오 소개글",
                        artistsIntro = "작가 소개글",
                        instagramUrl = "인스타 url",
                        kakaoChannelUrl = "카카오톡 채널 url",
                        reservationNotice = "",
                        cancellationPolicy = "",
                        status = StudioStatus.ACTIVE,
                    )
                }
            }
        }

        context("스튜디오 소개글이 빈 값이면") {
            it("예외를 발생 시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    Studio(
                        userId = 1L,
                        name = "스튜디오",
                        contact = "010-1234-5678",
                        studioIntro = "",
                        artistsIntro = "작가 소개글",
                        instagramUrl = "인스타 url",
                        kakaoChannelUrl = "카카오톡 채널 url",
                        reservationNotice = "",
                        cancellationPolicy = "",
                        status = StudioStatus.ACTIVE,
                    )
                }
            }
        }

        context("작가 소개글이 빈 값이면") {
            it("예외를 발생 시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    Studio(
                        userId = 1L,
                        name = "스튜디오",
                        contact = "010-1234-5678",
                        studioIntro = "스튜디오 소개글",
                        artistsIntro = "",
                        instagramUrl = "인스타 url",
                        kakaoChannelUrl = "카카오톡 채널 url",
                        reservationNotice = "",
                        cancellationPolicy = "",
                        status = StudioStatus.ACTIVE,
                    )
                }
            }
        }

        context("인스타 url이 빈 값이면") {
            it("예외를 발생 시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    Studio(
                        userId = 1L,
                        name = "스튜디오",
                        contact = "010-1234-5678",
                        studioIntro = "스튜디오 소개글",
                        artistsIntro = "작가 소개글",
                        instagramUrl = "",
                        kakaoChannelUrl = "카카오톡 채널 url",
                        reservationNotice = "",
                        cancellationPolicy = "",
                        status = StudioStatus.ACTIVE,
                    )
                }
            }
        }

        context("카카오톡 채널 url이 빈 값이면") {
            it("예외를 발생 시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    Studio(
                        userId = 1L,
                        name = "스튜디오",
                        contact = "010-1234-5678",
                        studioIntro = "스튜디오 소개글",
                        artistsIntro = "작가 소개글",
                        instagramUrl = "인스타 url",
                        kakaoChannelUrl = "",
                        reservationNotice = "",
                        cancellationPolicy = "",
                        status = StudioStatus.ACTIVE,
                    )
                }
            }
        }
    }
})
