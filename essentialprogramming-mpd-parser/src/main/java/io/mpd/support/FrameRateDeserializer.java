package io.mpd.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.mpd.data.FrameRate;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FrameRateDeserializer extends JsonDeserializer<FrameRate> {
    private static final Pattern FRAME_RATE_PATTERN = Pattern.compile("^([0-9]+)(/[0-9]+)?$");

    @Override
    public FrameRate deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
        String text = p.getText();
        Matcher matcher = FRAME_RATE_PATTERN.matcher(text);
        if (matcher.matches()) {
            String a = matcher.group(1);
            String b = matcher.group(2);
            return new FrameRate(Long.parseLong(a),
                    b == null ? null : Long.parseLong(b.substring(1)));
        } else {
            deserializationContext.reportWrongTokenException(this, p.currentToken(), "Invalid ratio");
            return null;
        }
    }
}
