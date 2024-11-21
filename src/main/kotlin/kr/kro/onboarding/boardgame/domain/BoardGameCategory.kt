package kr.kro.onboarding.boardgame.domain

enum class BoardGameCategory(val label: String) {
    STRATEGY_GAME("전략 게임"),
    ABSTRACT_STRATEGY_GAME("추상 전략 게임"),
    COLLECTIBLE_GAME("컬렉터블 게임"),
    FAMILY_GAME("가족 게임"),
    KIDS_GAME("어린이 게임"),
    THEME_GAME("테마 게임"),
    PARTY_GAME("파티 게임"),
    WAR_GAME("워 게임"),
}
