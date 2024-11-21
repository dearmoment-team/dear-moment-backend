package kr.kro.onboarding.boardgame.adapter.output.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.onboarding.boardgame.domain.Category

@Entity
@Table(name = "boardgame")
data class BoardGameEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 전략 (데이터베이스에 따라 다를 수 있음)
    @Column
    val id: Long? = null,
    @Column
    val name: String,
    @Column
    val koreanName: String,
    @Column
    val playTime: Int,
    @Enumerated(EnumType.STRING)
    val category: Category,
    @Column
    val age: Int,
    @Column
    val publisher: String,
    @Column
    val minPlayer: Int,
    @Column
    val maxPlayer: Int,
    @Column
    val difficulty: Double,
)
