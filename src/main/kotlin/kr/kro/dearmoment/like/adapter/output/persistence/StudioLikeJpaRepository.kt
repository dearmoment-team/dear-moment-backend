package kr.kro.dearmoment.like.adapter.output.persistence

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StudioLikeJpaRepository : JpaRepository<StudioLikeEntity, Long>, KotlinJdslJpqlExecutor
