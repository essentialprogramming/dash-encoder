package io.mpd.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {
    @Override
    public void serialize(OffsetDateTime value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeString(value.atZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
