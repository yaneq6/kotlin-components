package com.gogoapps.androidcomponents.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAccessor


class DateSerializer(
    val dateTimeFormatter: DateTimeFormatter
) : JsonSerializer<TemporalAccessor>()
{
    override fun serialize(
        value: TemporalAccessor,
        generator: JsonGenerator,
        serializers: SerializerProvider
    ) = generator.writeString(dateTimeFormatter.format(value))
}

class DateDeserializer<T>(
    val dateTimeFormatter: DateTimeFormatter,
    val deserialize: (TemporalAccessor) -> T
) : JsonDeserializer<T>()
{
    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext
    ) : T = deserialize(dateTimeFormatter.parse(parser.valueAsString))
}
