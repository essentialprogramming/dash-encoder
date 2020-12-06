package com.dash.mpd.util;

import io.mpd.data.FrameRate;
import io.mpd.data.Representation;
import org.apache.commons.lang.math.Fraction;

import java.util.List;
import java.util.function.Function;

public class AdaptionSetUtil {

    public static long adjustMin(List<Representation> representations, Function<Representation, Long> get) {
        final Long min = adjustMinMax(representations, get, true);
        return min != null ? min : 0;
    }

    public static long adjustMax(List<Representation> representations, Function<Representation, Long> get) {
        final Long max = adjustMinMax(representations, get, false);
        return max != null ? max : 10000;
    }

    private static Long adjustMinMax(List<Representation> representations, Function<Representation, Long> get, boolean needMinimum) {
        long min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Representation representationType : representations) {
            Long n = get.apply(representationType);
            if (n != null) {
                min = Math.min(n, min);
                max = Math.max(n, max);
            }
        }
        if (min != Integer.MAX_VALUE && min != max) {
            return needMinimum ? min : max;
        }

        return null;
    }


    public static FrameRate adjustAdaptationSetFrameRate(List<Representation> representations) {
        FrameRate finalFrameRate = null;
        for (Representation representation : representations) {
            FrameRate frameRate = representation.getFrameRate();
            if (frameRate == null) {
                return null;
            }
            if (finalFrameRate == null || finalFrameRate.equals(frameRate)) {
                finalFrameRate = frameRate;
            } else {
                return null;
            }
            representation.setFrameRate(null);
        }
        return finalFrameRate;
    }


    public static FrameRate adjustMinMaxFrameRate(List<Representation> representations, boolean needMinimum) {
        Fraction min = null, max = null;
        for (Representation representation : representations) {
            FrameRate frameRate = representation.getFrameRate();
            if (frameRate != null) {
                Fraction fraction = Fraction.getFraction(Math.toIntExact(frameRate.getNumerator()),
                        Math.toIntExact(frameRate.getDenominator()));

                min = min == null || fraction.compareTo(min) < 0 ? fraction : min;
                max = max == null || fraction.compareTo(max) > 0 ? fraction : max;
            }
        }
        // when both values are the same there is no need to set min/max frame rates
        if (min != null && !min.equals(max)) {
            FrameRate minFrameRate = new FrameRate(Long.parseLong(min.toString().split("/")[0]), Long.valueOf(min.toString().split("/")[1]));
            FrameRate maxFrameRate = new FrameRate(Long.parseLong(max.toString().split("/")[0]), Long.valueOf(max.toString().split("/")[1]));
            return needMinimum ? minFrameRate : maxFrameRate;
        }
        return null;
    }
}
