import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import kotlin.time.Duration
import kotlin.time.DurationUnit

class DurationSerializer : JsonSerializer<Duration>() {
    override fun serialize(value: Duration, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.toLong(DurationUnit.MILLISECONDS))
    }
}