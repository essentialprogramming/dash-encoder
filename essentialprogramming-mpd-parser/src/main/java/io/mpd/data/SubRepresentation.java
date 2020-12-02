package io.mpd.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.mpd.data.descriptor.Descriptor;

import java.util.List;
import java.util.Objects;

public class SubRepresentation extends RepresentationBase {
    @JacksonXmlProperty(isAttribute = true)
    private final Long level;

    @JacksonXmlProperty(isAttribute = true)
    private final String dependencyLevel;

    @JacksonXmlProperty(isAttribute = true)
    private final Long bandwidth;

    @JacksonXmlProperty(isAttribute = true)
    private final String contentComponent;

    private SubRepresentation(List<Descriptor> framePackings, List<Descriptor> audioChannelConfigurations, List<Descriptor> contentProtections, List<Descriptor> essentialProperties, List<Descriptor> supplementalProperties, List<EventStream> inbandEventStreams, String profiles, Long width, Long height, Ratio sar, FrameRate frameRate, String audioSamplingRate, String mimeType, String segmentProfiles, String codecs, Double maximumSAPPeriod, Long startWithSAP, Double maxPlayoutRate, Boolean codingDependency, VideoScanType scanType, Long level, String dependencyLevel, Long bandwidth, String contentComponent) {
        super(framePackings, audioChannelConfigurations, contentProtections, essentialProperties, supplementalProperties, inbandEventStreams, profiles, width, height, sar, frameRate, audioSamplingRate, mimeType, segmentProfiles, codecs, maximumSAPPeriod, startWithSAP, maxPlayoutRate, codingDependency, scanType);
        this.level = level;
        this.dependencyLevel = dependencyLevel;
        this.bandwidth = bandwidth;
        this.contentComponent = contentComponent;
    }

    @SuppressWarnings("unused")
    private SubRepresentation() {
        this.level = null;
        this.dependencyLevel = null;
        this.bandwidth = null;
        this.contentComponent = null;
    }

    public Long getLevel() {
        return level;
    }

    public String getDependencyLevel() {
        return dependencyLevel;
    }

    public Long getBandwidth() {
        return bandwidth;
    }

    public String getContentComponent() {
        return contentComponent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubRepresentation that = (SubRepresentation) o;
        return Objects.equals(level, that.level) &&
                Objects.equals(dependencyLevel, that.dependencyLevel) &&
                Objects.equals(bandwidth, that.bandwidth) &&
                Objects.equals(contentComponent, that.contentComponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), level, dependencyLevel, bandwidth, contentComponent);
    }

    @Override
    public String toString() {
        return "SubRepresentation{" +
                "super=" + super.toString() +
                ", level=" + level +
                ", dependencyLevel=" + dependencyLevel +
                ", bandwidth=" + bandwidth +
                ", contentComponent=" + contentComponent +
                '}';
    }

    public Builder buildUpon() {
        return super.buildUpon(new Builder()
                .withLevel(level)
                .withDependencyLevel(dependencyLevel)
                .withBandwidth(bandwidth)
                .withContentComponent(contentComponent));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends RepresentationBase.AbstractBuilder<Builder> {
        private Long level;
        private String dependencyLevel;
        private Long bandwidth;
        private String contentComponent;

        public Builder withLevel(Long level) {
            this.level = level;
            return this;
        }

        public Builder withDependencyLevel(String dependencyLevel) {
            this.dependencyLevel = dependencyLevel;
            return this;
        }

        public Builder withBandwidth(Long bandwidth) {
            this.bandwidth = bandwidth;
            return this;
        }

        public Builder withContentComponent(String contentComponent) {
            this.contentComponent = contentComponent;
            return this;
        }

        public SubRepresentation build() {
            return new SubRepresentation(framePackings, audioChannelConfigurations, contentProtections, essentialProperties, supplementalProperties, inbandEventStreams, profiles, width, height, sar, frameRate, audioSamplingRate, mimeType, segmentProfiles, codecs, maximumSAPPeriod, startWithSAP, maxPlayoutRate, codingDependency, scanType, level, dependencyLevel, bandwidth, contentComponent);
        }

        @Override
        Builder getThis() {
            return this;
        }
    }
}
