package io.mpd.data.descriptor.protection;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.mpd.data.descriptor.Descriptor;

import java.util.Objects;

public class WidewineProtection extends Descriptor {
    public static final String SCHEME_ID_URI = "urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed";

    @JacksonXmlProperty(isAttribute = true, localName = "value")
    private final String value;

    @JacksonXmlProperty(namespace = "urn:mpeg:cenc:2013")
    private final String pssh;

    public WidewineProtection(String value, String pssh) {
        super(SCHEME_ID_URI, null);
        this.value = value;
        this.pssh = pssh;
    }

    @SuppressWarnings("unused")
    private WidewineProtection() {
        super(null, null);
        this.pssh = null;
        this.value = null;
    }

    @Override
    public String getValue() {
        return value;
    }

    public String getPssh() {
        return pssh;
    }

    @Override
    public String toString() {
        return "WidewineProtection{" +
                "value='" + value + '\'' +
                ", pssh='" + pssh + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WidewineProtection that = (WidewineProtection) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(pssh, that.pssh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value, pssh);
    }

    public Builder buildUpon() {
        return new Builder()
                .withPssh(pssh)
                .withValue(value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String value;
        private String pssh;

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withPssh(String pssh) {
            this.pssh = pssh;
            return this;
        }

        public WidewineProtection build() {
            return new WidewineProtection(value, pssh);
        }
    }
}
