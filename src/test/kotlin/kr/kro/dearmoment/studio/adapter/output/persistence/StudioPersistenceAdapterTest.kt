package kr.kro.dearmoment.studio.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.studioFixture

@RepositoryTest
class StudioPersistenceAdapterTest(
    private val studioRepository: StudioJpaRepository,
) : DescribeSpec({
        val adapter = StudioPersistenceAdapter(studioRepository)

        describe("StudioPersistenceAdapter 클래스는") {
            context("저장하려는 스튜디오를 전달하면") {
                it("DB에 데이터를 저장하고 반환한다.") {
                    val savedStudio = adapter.save(studioFixture())
                    savedStudio.id shouldNotBe 0L
                }
            }
            context("조회하려는 스튜디오 식별자를 전달하면") {
                val expected = adapter.save(studioFixture())
                it("DB에서 해당 스튜디오 엔티티를 반환한다.") {
                    val result = adapter.findById(expected.id)
                    result shouldBe expected
                }
            }

            context("수정하려는 스튜디오를 전달하면") {
                val savedStudio = adapter.save(studioFixture())
                val updatedStudio = studioFixture(id = savedStudio.id, userId = savedStudio.userId)

                it("DB에 해당 데이터를 업데이트하고 반환한다.") {
                    val result = adapter.update(updatedStudio)
                    updatedStudio.userId shouldBe result.userId
                    updatedStudio.id shouldBe result.id
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
