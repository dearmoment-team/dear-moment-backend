package kr.kro.onboarding.boardgame.domain

class BoardGame(
    val name: String,
    val koreanName: String,
    val playTime: Int,
    val category: BoardGameCategory,
    val age: Int,
    val publisher: String,
    val minPlayer: Int,
    val maxPlayer: Int,
    val difficulty: Double,
)
