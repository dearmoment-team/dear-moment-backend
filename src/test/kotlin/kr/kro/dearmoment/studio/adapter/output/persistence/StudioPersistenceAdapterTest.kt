package kr.kro.dearmoment.studio.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.studio.domain.Studio
import kr.kro.dearmoment.studio.domain.StudioStatus

@RepositoryTest
class StudioPersistenceAdapterTest(
    private val studioRepository: StudioJpaRepository,
) : DescribeSpec({
        val adapter = StudioPersistenceAdapter(studioRepository)

        describe("StudioPersistenceAdapter 클래스는") {
            context("저장하려는 스튜디오를 전달하면") {
                val studio =
                    Studio(
                        name = "스튜디오",
                        contact = "010-1234-5678",
                        studioIntro = "스튜디오 소개글",
                        artistsIntro = "작가 소개글",
                        instagramUrl = "인스타 url",
                        kakaoChannelUrl = "카카오톡 채널 url",
                        reservationNotice = "",
                        cancellationPolicy = "",
                        status = StudioStatus.ACTIVE,
                    )

                it("DB에 데이터를 저장하고 반환한다.") {
                    val savedStudio = adapter.save(studio)
                    savedStudio.id shouldNotBe 0L
                }
            }

            context("수정하려는 스튜디오를 전달하면") {
                val studio =
                    Studio(
                        name = "스튜디오",
                        contact = "010-1234-5678",
                        studioIntro = "스튜디오 소개글",
                        artistsIntro = "작가 소개글",
                        instagramUrl = "인스타 url",
                        kakaoChannelUrl = "카카오톡 채널 url",
                        reservationNotice = "",
                        cancellationPolicy = "",
                        status = StudioStatus.ACTIVE,
                    )
                val savedStudio = adapter.save(studio)
                val updatedStudio =
                    Studio(
                        id = savedStudio.id,
                        name = "스튜디오 A(수정)",
                        contact = "010-1234-5678(수정)",
                        studioIntro = "스튜디오 소개글(수정)",
                        artistsIntro = "작가 소개글(수정)",
                        instagramUrl = "인스타 url(수정)",
                        kakaoChannelUrl = "카카오톡 채널 url(수정)",
                        reservationNotice = "예약은 평일만 가능합니다.(수정)",
                        cancellationPolicy = "환불은 불가능합니다.(수정)",
                        status = StudioStatus.INACTIVE,
                    )
                it("DB에 해당 데이터를 업데이트하고 반환한다.") {
                    val result = adapter.update(updatedStudio)
                    updatedStudio shouldBe result
                }
            }

            context("삭제하려는 스튜디오 식별자를 전달하면") {
                val deleteId = 1L
                it("해당 스튜디오를 삭제한다.") {
                    shouldNotThrow<Throwable> { adapter.delete(deleteId) }
                }
            }
        }
    })
