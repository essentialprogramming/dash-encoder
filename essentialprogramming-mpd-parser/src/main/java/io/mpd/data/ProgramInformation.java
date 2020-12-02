package io.mpd.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Objects;

@JsonPropertyOrder({
    "title",
    "source",
    "copyright",
    "any"
})
public class ProgramInformation {
    @JacksonXmlProperty(localName = "Title", namespace = MPD.NAMESPACE)
    private final String title;

    @JacksonXmlProperty(localName = "Source", namespace = MPD.NAMESPACE)
    private final String source;

    @JacksonXmlProperty(localName = "Copyright", namespace = MPD.NAMESPACE)
    private final String copyright;

    @JacksonXmlProperty(isAttribute = true)
    private final String lang;

    @JacksonXmlProperty(isAttribute = true)
    private final String moreInformationURL;

    private ProgramInformation(String title, String source, String copyright, String lang, String moreInformationURL) {
        this.title = title;
        this.source = source;
        this.copyright = copyright;
        this.lang = lang;
        this.moreInformationURL = moreInformationURL;
    }

    @SuppressWarnings("unused")
    private ProgramInformation() {
        this.title = null;
        this.source = null;
        this.copyright = null;
        this.lang = null;
        this.moreInformationURL = null;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getLang() {
        return lang;
    }

    public String getMoreInformationURL() {
        return moreInformationURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramInformation that = (ProgramInformation) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(source, that.source) &&
                Objects.equals(copyright, that.copyright) &&
                Objects.equals(lang, that.lang) &&
                Objects.equals(moreInformationURL, that.moreInformationURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, source, copyright, lang, moreInformationURL);
    }

    @Override
    public String toString() {
        return "ProgramInformation{" +
                "title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", copyright='" + copyright + '\'' +
                ", lang='" + lang + '\'' +
                ", moreInformationURL='" + moreInformationURL + '\'' +
                '}';
    }

    public Builder buildUpon() {
        return new Builder()
                .withTitle(title)
                .withSource(source)
                .withCopyright(copyright)
                .withLang(lang)
                .withMoreInformationURL(moreInformationURL);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String source;
        private String copyright;
        private String lang;
        private String moreInformationURL;

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withSource(String source) {
            this.source = source;
            return this;
        }

        public Builder withCopyright(String copyright) {
            this.copyright = copyright;
            return this;
        }

        public Builder withLang(String lang) {
            this.lang = lang;
            return this;
        }

        public Builder withMoreInformationURL(String moreInformationURL) {
            this.moreInformationURL = moreInformationURL;
            return this;
        }

        public ProgramInformation build() {
            return new ProgramInformation(title, source, copyright, lang, moreInformationURL);
        }
    }
}
