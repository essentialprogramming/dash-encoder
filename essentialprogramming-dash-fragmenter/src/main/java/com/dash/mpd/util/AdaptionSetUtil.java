package com.dash.mpd.util;

import io.mpd.data.FrameRate;
import io.mpd.data.Representation;

import java.util.List;
import java.util.function.Function;

public class AdaptionSetUtil {

    public static long adjustMin(List<Representation> representations, Function<Representation, Long> get) {
        Long min = adjustMinMax(representations, get, true);
        if (min != null) return min;
        return 0;
    }

    public static long adjustMax(List<Representation> representations, Function<Representation, Long> get) {
        Long max = adjustMinMax(representations, get, false);
        if (max != null) return max;
        return 10000;
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



    public static FrameRate adjustAdaptationSetFrameRate(List<Representation> representations, Function<Representation, Object> get) {
        Object v = null;
        for (Representation representationType : representations) {
            Object _v = get.apply(representationType);
            if (_v == null) {
                return null; // no need to move it around when it doesn't exist
            }
            if (v == null || v.equals(_v)) {
                v = _v;
            } else {
                return null;
            }
            representationType.setFrameRate(null);
        }
        return (FrameRate) v;
    }

   /* TODO : method that could be used for adding extra content protection (not needed in current scenario)
   public static void optimizeContentProtection(AdaptationSetType parent) {
        List<DescriptorType> contentProtection = new ArrayList<>();
        for (RepresentationBaseType representationType : parent.getRepresentation()) {
            if (contentProtection.isEmpty()) {
                contentProtection.addAll(representationType.getContentProtection());
            } else {
                List<DescriptorType> currentCP = representationType.getContentProtection();
                if (contentProtection.size() == currentCP.size()) {
                    for (int i = 0; i < currentCP.size(); i++) {
                        DescriptorType a = currentCP.get(i);
                        DescriptorType b = contentProtection.get(i);
                        if (!(Objects.equals(a.getValue(), b.getValue()) && Objects.equals(a.getSchemeIdUri(), b.getSchemeIdUri()) && Objects.equals(a.getId(), b.getId()))) {
                            return;
                        }

                    }
                }
            }
        }
        if (!contentProtection.isEmpty()) {
            for (RepresentationBaseType representationType : parent.getRepresentation()) {
                representationType.getContentProtection().clear();
            }
            parent.getContentProtection().addAll(contentProtection);
        }

    }*/


   /* TODO : method that could be used for adding extra minFrameRate and maxFrameRate attributes on Adaptation
            in current scenario we dont add minFrameRate and maxFrameRate on adaption set
   public static FrameRate adjustMinMaxFrameRate(List<Representation> representations) {
        Fraction min = null, max = null;
        for (Representation representationType : representations) {
            FrameRate frameRate = representationType.getFrameRate();
            if (frameRate.toString() != null) {
                Fraction f = Fraction.getFraction(frameRate.toString());

                min = min == null || f.compareTo(min) < 0 ? f : min;
                max = max == null || f.compareTo(max) > 0 ? f : max;
            }
        }
        if (max != null && !min.equals(max)) { // min/max doesn't make sense when both values are the same
            return new FrameRate((long) min.doubleValue(), (long) max.doubleValue());
        }
        return  null;
    }*/
}
