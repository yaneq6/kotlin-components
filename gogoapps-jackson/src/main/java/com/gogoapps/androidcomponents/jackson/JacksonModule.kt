package com.gogoapps.androidcomponents.jackson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAccessor
import kotlin.reflect.KClass

interface JacksonModule
{
    val objectMapper: ObjectMapper
}

class JacksonComponent : JacksonModule
{

    val localDateModule by lazy {
        DateModuleFactory(
            DateTimeFormatter.ISO_LOCAL_DATE,
            LocalDate::class,
            LocalDate::from
        ).create()
    }

    val zonedDateTimeModule by lazy {
        DateModuleFactory(
            DateTimeFormatter.ISO_INSTANT,
            ZonedDateTime::class,
            { Instant.from(it).atZone(ZoneOffset.UTC) }
        ).create()
    }

    override val objectMapper: ObjectMapper by lazy {
        jacksonObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(localDateModule)
            .registerModule(zonedDateTimeModule)
    }
}

class DateModuleFactory<T : TemporalAccessor>(
    val dateFormatter: DateTimeFormatter,
    val serializedClass: KClass<T>,
    val fromTemporalAccessor: (TemporalAccessor) -> T
) {
    constructor(
        dateFormat: String = "yyyy-MM-dd",
        serializedClass: KClass<T>,
        fromTemporalAccessor: (TemporalAccessor) -> T
    ) : this(
        DateTimeFormatter.ofPattern(dateFormat),
        serializedClass,
        fromTemporalAccessor
    )

    val dateDeserializer : JsonDeserializer<T> by lazy {
        DateDeserializer(dateFormatter, fromTemporalAccessor)
    }

    val dateSerializer : JsonSerializer<TemporalAccessor> by lazy {
        DateSerializer(dateFormatter)
    }

    fun create(): Module = SimpleModule()
            .addDeserializer(serializedClass.java, dateDeserializer)
            .addSerializer(serializedClass.java, dateSerializer)
}