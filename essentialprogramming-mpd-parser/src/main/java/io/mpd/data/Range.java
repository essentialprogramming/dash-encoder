package io.mpd.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.time.Duration;
import java.util.Objects;

public class Range {
    @JacksonXmlProperty(isAttribute = true)
    private final Duration starttime;

    @JacksonXmlProperty(isAttribute = true)
    private final Duration duration;

    private Range(Duration starttime, Duration duration) {
        this.starttime = starttime;
        this.duration = duration;
    }

    @SuppressWarnings("unused")
    private Range() {
        this.starttime = null;
        this.duration = null;
    }

    public Duration getStarttime() {
        return starttime;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Range range = (Range) o;
        return Objects.equals(starttime, range.starttime) &&
                Objects.equals(duration, range.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(starttime, duration);
    }

    @Override
    public String toString() {
        return "Range{" +
                "starttime=" + starttime +
                ", duration=" + duration +
                '}';
    }

    public Builder buildUpon() {
        return new Builder()
                .withStarttime(starttime)
                .withDuration(duration);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Duration starttime;
        private Duration duration;

        public Builder withStarttime(Duration starttime) {
            this.starttime = starttime;
            return this;
        }

        public Builder withDuration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Range build() {
            return new Range(starttime, duration);
        }
    }
}
