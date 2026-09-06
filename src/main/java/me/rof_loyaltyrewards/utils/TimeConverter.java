package me.rof_loyaltyrewards.utils;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeConverter {

    // Captures a number immediately followed by a run of letters, e.g. "5d", "30m", "1mo".
    private static final Pattern COMPONENT_PATTERN = Pattern.compile("(\\d+)\\s*([a-zA-Z]+)");
    private static final long SECOND = 1000L;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    // A calendar month has no fixed length; 30 days is used as a practical approximation.
    private static final long MONTH = 30 * DAY;

    /**
     * Converts a time string like "1mo 5d 6h 30m 20s" into milliseconds.
     * Supported units: mo (month, 30 days), d (day), h (hour), m (minute), s (second).
     * Returns 0 for null/blank input. Unrecognized components are skipped and
     * a warning is logged (when a logger is supplied) so a config typo shows
     * up as a log line rather than silently becoming an instant reward.
     */
    public static long convertToMilliseconds(String timeString) {
        return convertToMilliseconds(timeString, null, null);
    }

    public static long convertToMilliseconds(String timeString, Logger logger, String context) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return 0;
        }

        Matcher matcher = COMPONENT_PATTERN.matcher(timeString.trim());
        long total = 0;
        boolean parsedAnything = false;
        int lastMatchEnd = 0;

        while (matcher.find()) {
            lastMatchEnd = matcher.end();
            long value;
            try {
                value = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                warn(logger, context, matcher.group());
                continue;
            }
            String unit = matcher.group(2).toLowerCase();

            switch (unit) {
                case "mo":
                    total += value * MONTH;
                    parsedAnything = true;
                    break;
                case "d":
                    total += value * DAY;
                    parsedAnything = true;
                    break;
                case "h":
                    total += value * HOUR;
                    parsedAnything = true;
                    break;
                case "m":
                    total += value * MINUTE;
                    parsedAnything = true;
                    break;
                case "s":
                    total += value * SECOND;
                    parsedAnything = true;
                    break;
                default:
                    warn(logger, context, matcher.group());
                    break;
            }
        }

        boolean trailingGarbage = lastMatchEnd < timeString.trim().length()
                && !timeString.trim().substring(lastMatchEnd).trim().isEmpty();

        if ((!parsedAnything || trailingGarbage) && logger != null) {
            logger.warning("[ROF_LoyaltyRewards] Time value \"" + timeString + "\""
                    + (context != null ? " (" + context + ")" : "")
                    + (parsedAnything
                        ? " contains unrecognized text that was ignored."
                        : " did not contain any valid mo/d/h/m/s components - it will require 0 time, "
                          + "meaning the reward will fire immediately.")
                    + " Check your config.yml. Expected format like \"1mo 5d 6h 30m 20s\".");
        }

        return total;
    }

    private static void warn(Logger logger, String context, String component) {
        if (logger != null) {
            logger.warning("[ROF_LoyaltyRewards] Could not parse time component \"" + component + "\""
                    + (context != null ? " in " + context : "") + " - skipping it. "
                    + "Expected units: mo (month), d (day), h (hour), m (minute), s (second).");
        }
    }
}
