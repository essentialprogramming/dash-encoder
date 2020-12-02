package io.mpd.data.descriptor.protection;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.mpd.data.descriptor.Descriptor;

import java.util.Objects;

public class PlayReadyContentProtection extends Descriptor {
    public static final String SCHEME_ID_URI = "urn:uuid:9a04f079-9840-4286-ab92-e65be0885f95";

    @JacksonXmlProperty(isAttribute = true, localName = "value")
    private final String value;

    @JacksonXmlProperty(isAttribute = true, localName = "default_KID", namespace = "urn:mpeg:cenc:2013")
    private final String defaultKID;

    @JacksonXmlProperty(namespace = "urn:mpeg:cenc:2013")
    private final String pssh;

    @JacksonXmlProperty(namespace = "urn:microsoft:playready")
    private final String pro;

    public PlayReadyContentProtection(String value, String defaultKID, String pssh, String pro) {
        super(SCHEME_ID_URI, null);
        this.value = value;
        this.defaultKID = defaultKID;
        this.pssh = pssh;
        this.pro = pro;
    }

    @SuppressWarnings("unused")
    private PlayReadyContentProtection() {
        super(null, null);
        this.value = null;
        this.defaultKID = null;
        this.pssh = null;
        this.pro = null;
    }

    @Override
    public String getValue() {
        return value;
    }

    public String getDefaultKID() {
        return defaultKID;
    }

    public String getPssh() {
        return pssh;
    }

    public String getPro() {
        return pro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PlayReadyContentProtection that = (PlayReadyContentProtection) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(defaultKID, that.defaultKID) &&
                Objects.equals(pssh, that.pssh) &&
                Objects.equals(pro, that.pro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value, defaultKID, pssh, pro);
    }

    @Override
    public String toString() {
        return "PlayReadyContentProtection{" +
                "value='" + value + '\'' +
                ", defaultKID='" + defaultKID + '\'' +
                ", pssh='" + pssh + '\'' +
                ", pro='" + pro + '\'' +
                '}';
    }

    public Builder buildUpon() {
        return new Builder()
                .withValue(value)
                .withDefaultKID(defaultKID)
                .withPssh(pssh)
                .withPro(pro);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String value;
        private String defaultKID;
        private String pssh;
        private String pro;

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withDefaultKID(String defaultKID) {
            this.defaultKID = defaultKID;
            return this;
        }

        public Builder withPssh(String pssh) {
            this.pssh = pssh;
            return this;
        }

        public Builder withPro(String pro) {
            this.pro = pro;
            return this;
        }

        public PlayReadyContentProtection build() {
            return new PlayReadyContentProtection(value, defaultKID, pssh, pro);
        }
    }
}
