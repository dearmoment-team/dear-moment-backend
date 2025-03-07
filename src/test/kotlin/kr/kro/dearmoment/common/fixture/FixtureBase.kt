package kr.kro.dearmoment.common.fixture

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin

val fixtureBuilder =
    FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()
