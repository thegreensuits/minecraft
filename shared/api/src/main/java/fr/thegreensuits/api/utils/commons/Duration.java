package fr.thegreensuits.api.utils.commons;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.ImmutableMap;

public class Duration implements Comparable<Duration> {
  private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)");

  private static final Map<String, TimeUnit> SUFFIXES = new ImmutableMap.Builder<String, TimeUnit>()
      .put("ns", TimeUnit.NANOSECONDS)
      .put("nanosecond", TimeUnit.NANOSECONDS)
      .put("nanoseconde", TimeUnit.NANOSECONDS)
      .put("nanosecondes", TimeUnit.NANOSECONDS)
      .put("nanoseconds", TimeUnit.NANOSECONDS)
      .put("us", TimeUnit.MICROSECONDS)
      .put("microsecond", TimeUnit.MICROSECONDS)
      .put("microsecondes", TimeUnit.MICROSECONDS)
      .put("microseconde", TimeUnit.MICROSECONDS)
      .put("microseconds", TimeUnit.MICROSECONDS)
      .put("ms", TimeUnit.MILLISECONDS)
      .put("millisecond", TimeUnit.MILLISECONDS)
      .put("milliseconde", TimeUnit.MILLISECONDS)
      .put("millisecondes", TimeUnit.MILLISECONDS)
      .put("milliseconds", TimeUnit.MILLISECONDS)
      .put("s", TimeUnit.SECONDS)
      .put("second", TimeUnit.SECONDS)
      .put("seconde", TimeUnit.SECONDS)
      .put("secondes", TimeUnit.SECONDS)
      .put("seconds", TimeUnit.SECONDS)
      .put("m", TimeUnit.MINUTES)
      .put("min", TimeUnit.MINUTES)
      .put("mins", TimeUnit.MINUTES)
      .put("minute", TimeUnit.MINUTES)
      .put("minutes", TimeUnit.MINUTES)
      .put("h", TimeUnit.HOURS)
      .put("heure", TimeUnit.HOURS)
      .put("heures", TimeUnit.HOURS)
      .put("hour", TimeUnit.HOURS)
      .put("hours", TimeUnit.HOURS)
      .put("d", TimeUnit.DAYS)
      .put("j", TimeUnit.DAYS)
      .put("jour", TimeUnit.DAYS)
      .put("jours", TimeUnit.DAYS)
      .put("day", TimeUnit.DAYS)
      .put("days", TimeUnit.DAYS)
      .build();

  public static Duration nanoseconds(long count) {
    return new Duration(count, TimeUnit.NANOSECONDS);
  }

  public static Duration microseconds(long count) {
    return new Duration(count, TimeUnit.MICROSECONDS);
  }

  public static Duration milliseconds(long count) {
    return new Duration(count, TimeUnit.MILLISECONDS);
  }

  public static Duration seconds(long count) {
    return new Duration(count, TimeUnit.SECONDS);
  }

  public static Duration minutes(long count) {
    return new Duration(count, TimeUnit.MINUTES);
  }

  public static Duration hours(long count) {
    return new Duration(count, TimeUnit.HOURS);
  }

  public static Duration days(long count) {
    return new Duration(count, TimeUnit.DAYS);
  }

  public static Duration parse(String duration) {
    String[] parts = duration.split(" ");

    long nano = 0;

    for (String string : parts) {
      final Matcher matcher = DURATION_PATTERN.matcher(string);
      Validate.isTrue(matcher.matches(), "Invalid duration: " + string);

      final long count = Long.parseLong(matcher.group(1));
      final TimeUnit unit = SUFFIXES.get(matcher.group(2));
      Validate.notNull(unit, "Invalid duration: " + string + ". Wrong time unit");

      nano += unit.toNanos(count);
    }

    return new Duration(nano, TimeUnit.NANOSECONDS);
  }

  private final long count;
  private final TimeUnit unit;

  private Duration(long count, TimeUnit unit) {
    Validate.notNull(unit, "Time unit cannot be null");

    this.count = count;
    this.unit = unit;
  }

  public long getQuantity() {
    return count;
  }

  public TimeUnit getUnit() {
    return unit;
  }

  public long toNanoseconds() {
    return TimeUnit.NANOSECONDS.convert(count, unit);
  }

  public long toMicroseconds() {
    return TimeUnit.MICROSECONDS.convert(count, unit);
  }

  public long toMilliseconds() {
    return TimeUnit.MILLISECONDS.convert(count, unit);
  }

  public long toSeconds() {
    return TimeUnit.SECONDS.convert(count, unit);
  }

  public long toMinutes() {
    return TimeUnit.MINUTES.convert(count, unit);
  }

  public long toHours() {
    return TimeUnit.HOURS.convert(count, unit);
  }

  public long toDays() {
    return TimeUnit.DAYS.convert(count, unit);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }

    final Duration duration = (Duration) obj;

    return (count == duration.count) && (unit == duration.unit);

  }

  @Override
  public int hashCode() {
    return (31 * (int) (count ^ (count >>> 32))) + unit.hashCode();
  }

  @Override
  public String toString() {
    String units = unit.toString().toLowerCase(Locale.ENGLISH);

    if (count == 1) {
      units = units.substring(0, units.length() - 1);
    }

    return Long.toString(count) + ' ' + units;
  }

  @Override
  public int compareTo(Duration other) {
    if (unit == other.unit) {
      return Long.compare(count, other.count);
    }

    return Long.compare(toNanoseconds(), other.toNanoseconds());
  }
}