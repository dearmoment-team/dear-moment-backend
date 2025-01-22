import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

class TestConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)
}