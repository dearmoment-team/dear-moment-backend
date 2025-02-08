package kr.kro.dearmoment.like.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaLikeRepository : JpaRepository<LikeEntity, Long>
