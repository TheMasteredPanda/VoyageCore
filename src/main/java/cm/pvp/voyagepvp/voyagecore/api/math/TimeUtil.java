package cm.pvp.voyagepvp.voyagecore.api.math;

import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class TimeUtil
{
    private TimeUtil()
    {
        throw new UtilityException();
    }

    /**
     * Converts milliseconds to a date.
     * @param milliseconds - milliseconds.
     * @return the formatted date.
     */
    public static String millisecondsToMetricDate(long milliseconds)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return calendar.get(Calendar.DATE) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
    }

    /**
     * Converts milliseconds to a formatted time.
     * @param unit - the highest time unit you want it for format to.
     * @param time - the time in milliseconds.
     * @param shorten - short date or long date.
     * @return the formatted time.
     */
    public static String millisecondsToTimeUnits(TimeUnit unit, long time, boolean shorten)
    {
        long days = unit.toDays(time);
        long hours = unit.toHours(time) - TimeUnit.DAYS.toHours(unit.toDays(time));
        long minutes = unit.toMinutes(time) - TimeUnit.HOURS.toMinutes(unit.toHours(time));
        long seconds = unit.toSeconds(time) - TimeUnit.MINUTES.toSeconds(unit.toMinutes(time));
        long milliseconds = unit.toMillis(time) - TimeUnit.SECONDS.toMillis(unit.toSeconds(time));
        String string = "";

        if (days > 0L) string = string + days + (days == 1L ? " day" : shorten ? "d" : " days");
        if (hours > 0L) string = string + (!string.isEmpty() ? ", " : "") + hours + (hours == 1L ? " hour" : shorten ? "h" : " hours");
        if (minutes > 0L) string = string + (!string.isEmpty() ? ", " : "") + minutes + (minutes == 1L ? " minute" : shorten ? "m" : " minutes");
        if (seconds > 0L) string = string + (!string.isEmpty() ? ", " : "") + seconds + (seconds == 1L ? " second" : shorten ? "s" : " seconds");
        if (milliseconds > 0L) string = string + (!string.isEmpty() ? ", " : "") + milliseconds + (seconds == 1L ? " millisecond" : shorten ? "ms" : " milliseconds");
        if (string.isEmpty()) string = "0" + (shorten ? "ms" : " millisecond");
        return string;
    }
}
