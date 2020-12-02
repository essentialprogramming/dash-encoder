package com.util.text;


import java.nio.charset.StandardCharsets;

public final class StringUtils {

    private StringUtils() {
        // private constructor to hide default public one
    }

    /**
     * @param str String object to examine.
     * @return Returns false if the given string is null or empty. Otherwise true.
     */
    public static boolean isEmpty(final String str) {
        return null == str || 0 == str.length();
    }

    /**
     * @param str String object to examine.
     * @return Returns true if the given string is null or empty. Otherwise false.
     */
    public static boolean isNotEmpty(final String str) {
        return !isEmpty(str);
    }

    public static boolean hasText(CharSequence str) {
        return (str != null && str.length() > 0 && containsText(str));
    }

    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndexOf);
    }

    public static String encodeText(String text) {
        int ascii = checkAscii(text);
        if (ascii == 1) {
            return text;
        } else {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    static int checkAscii(String s) {
        int ascii = 0;
        int nonAscii = 0;
        int length = s.length();

        for (int i = 0; i < length; ++i) {
            if (nonAscii(s.charAt(i))) {
                ++nonAscii;
            } else {
                ++ascii;
            }
        }

        if (nonAscii == 0) {
            return 1;
        } else if (ascii > nonAscii) {
            return 2;
        } else {
            return 3;
        }
    }

    public static boolean nonAscii(int b) {
        return b >= 127 || b < 32 && b != 13 && b != 10 && b != 9;
    }
}
