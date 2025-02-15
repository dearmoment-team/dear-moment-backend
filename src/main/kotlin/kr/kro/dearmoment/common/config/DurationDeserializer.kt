import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DurationDeserializer : JsonDeserializer<Duration>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Duration {
        return p.longValue.toDuration(DurationUnit.MILLISECONDS)
    }
}
