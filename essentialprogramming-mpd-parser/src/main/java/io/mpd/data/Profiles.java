package io.mpd.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.mpd.support.ProfilesDeserializer;
import io.mpd.support.ProfilesSerializer;
import io.mpd.support.Utils;

import java.util.List;
import java.util.Objects;

@JsonSerialize(using = ProfilesSerializer.class)
@JsonDeserialize(using = ProfilesDeserializer.class)
public class Profiles {
    private final List<Profile> profiles;
    private final List<String> interoperabilityPointsAndExtensions;

    public Profiles(List<Profile> profiles, List<String> interoperabilityPointsAndExtensions) {
        this.profiles = profiles;
        this.interoperabilityPointsAndExtensions = interoperabilityPointsAndExtensions;
    }

    Profiles() {
        this.profiles = null;
        this.interoperabilityPointsAndExtensions = null;
    }

    public List<Profile> getProfiles() {
        return Utils.unmodifiableList(profiles);
    }

    public List<String> getInteroperabilityPointsAndExtensions() {
        return Utils.unmodifiableList(interoperabilityPointsAndExtensions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profiles profiles1 = (Profiles) o;
        return Objects.equals(profiles, profiles1.profiles) &&
                Objects.equals(interoperabilityPointsAndExtensions, profiles1.interoperabilityPointsAndExtensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profiles, interoperabilityPointsAndExtensions);
    }

    @Override
    public String toString() {
        return "Profiles{" +
                "profiles=" + profiles +
                ", interoperabilityPointsAndExtensions=" + interoperabilityPointsAndExtensions +
                '}';
    }

    public Builder buildUpon() {
        return new Builder()
                .withProfiles(profiles)
                .withInteroperabilityPointsAndExtensions(interoperabilityPointsAndExtensions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Profile> profiles;
        private List<String> interoperabilityPointsAndExtensions;

        public Builder withProfiles(List<Profile> profiles) {
            this.profiles = profiles;
            return this;
        }

        public Builder withInteroperabilityPointsAndExtensions(List<String> interoperabilityPointsAndExtensions) {
            this.interoperabilityPointsAndExtensions = interoperabilityPointsAndExtensions;
            return this;
        }

        public Profiles build() {
            return new Profiles(profiles, interoperabilityPointsAndExtensions);
        }
    }
}
