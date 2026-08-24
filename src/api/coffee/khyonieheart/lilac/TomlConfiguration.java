/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Convenience type which takes a loaded TOML map and allows for values to be referenced using TOML keys.
 *
 * All key lookups are cached for performance.
 *
 * This type distinguishes between two types of "get" methods: exception-throwing, and null-returning. 
 * Exception-throwing methods will throw an exception if any part of a key cannot be resolved.
 * Null-returning methods will return null if any part of a key cannot be resolved.
 *
 * @since 2.1.0
 */
public class TomlConfiguration
{
	private final TomlConfigurationLookup lookup;

	/**
	 * Creates a configuration wrapper around decoded TOML data.
	 *
	 * @param data Decoded TOML data to read from.
	 */
	public TomlConfiguration(
		Map<String, Object> data
	) {
		this.lookup = new TomlConfigurationLookup(data);
	}

	/**
	 * Convenience method to take a path and version and return a {@link TomlConfiguration}.
	 *
	 * @param path Path to a TOML file.
	 * @param version TOML specification version to use while decoding.
	 *
	 * @return A TomlConfiguration from the specified path.
	 * @throws IOException Thrown if the file cannot be read.
	 */
	public static TomlConfiguration from(
		String path,
		TomlVersion version
	) throws IOException
	{
		TomlDecoder decoder = new LilacDecoder(version);

		return decoder.decodeConfiguration(new File(path));
	}

	/**
	 * Checks whether the given fully-qualified key resolves to a value.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key.
	 *
	 * @return {@code true} if the key resolves, {@code false} otherwise.
	 */
	public boolean contains(
		String fullyQualifiedKey
	) {
		try {
			this.get(fullyQualifiedKey, true);
		} catch (NoSuchElementException | ClassCastException e) {
			return false;
		}

		return true;
	}

	private Object get(
		String fullyQualifiedKey,
		boolean throwException
	) {
		return lookup.get(fullyQualifiedKey, throwException);
	}

	private <T> T getWithCast(
		String fullyQualifiedKey,
		boolean throwException,
		Class<T> type
	) {
		Object value = this.get(fullyQualifiedKey, throwException);
		if (value == null)
		{
			return null;
		}

		if (!type.isAssignableFrom(value.getClass()))
		{
			throw new ClassCastException("Value at \"" + fullyQualifiedKey + "\" of type " + value.getClass().getName() + " cannot be cast to " + type.getName());
		}

		return type.cast(value);
	}

	/**
	 * Gets a String from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A String from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast as a String.
	 */
	public String getStringOrNull(
		String fullyQualifiedKey
	) {	
		return this.getWithCast(fullyQualifiedKey, false, String.class);
	}

	/**
	 * Gets a String from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A String from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a String, or if a table section of the key cannot be resolved.
	 */
	public String getString(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, String.class);
	}

	/**
	 * Gets a long from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A long from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a long, or if a table section of the key cannot be resolved.
	 */
	public long getLong(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, Long.class);
	}

	/**
	 * Gets a long from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A long from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a long.
	 */
	public Long getLongOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, Long.class);
	}

	/**
	 * Gets an integer from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return An integer from this map.
	 * @throws ArithmeticException Thrown if the stored long cannot fit in an int.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a long, or if a table section of the key cannot be resolved.
	 */
	public int getInteger(
		String fullyQualifiedKey
	) {
		return Math.toIntExact(this.getLong(fullyQualifiedKey));
	}

	/**
	 * Gets an integer from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return An integer from this map. May be null if any part of the key cannot be resolved.
	 * @throws ArithmeticException Thrown if the stored long cannot fit in an int.
	 * @throws ClassCastException Thrown if value cannot be cast to a long.
	 */
	public Integer getIntegerOrNull(
		String fullyQualifiedKey
	) {
		Long value = this.getLongOrNull(fullyQualifiedKey);
		return value != null
			? Math.toIntExact(value)
			: null;
	}

	/**
	 * Gets a float from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A float from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a float, or if a table section of the key cannot be resolved.
	 */
	public float getFloat(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, Float.class);
	}

	/**
	 * Gets a float from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A float from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a float.
	 */
	public Float getFloatOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, Float.class);
	}

	/**
	 * Gets a boolean from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A boolean from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a boolean, or if a table section of the key cannot be resolved.
	 */
	public boolean getBoolean(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, Boolean.class);
	}

	/**
	 * Gets a boolean from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A boolean from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a boolean.
	 */
	public Boolean getBooleanOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, Boolean.class);
	}

	/**
	 * Gets an offset date-time from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return An offset date-time from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to an offset date-time, or if a table section of the key cannot be resolved.
	 */
	public OffsetDateTime getOffsetDateTime(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, OffsetDateTime.class);
	}

	/**
	 * Gets an offset date-time from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return An offset date-time from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to an offset date-time.
	 */
	public OffsetDateTime getOffsetDateTimeOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, OffsetDateTime.class);
	}

	/**
	 * Gets a local date-time from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A local date-time from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a local date-time, or if a table section of the key cannot be resolved.
	 */
	public LocalDateTime getLocalDateTime(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, LocalDateTime.class);
	}

	/**
	 * Gets a local date-time from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A local date-time from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a local date-time.
	 */
	public LocalDateTime getLocalDateTimeOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, LocalDateTime.class);
	}

	/**
	 * Gets a local date from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A local date from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a local date, or if a table section of the key cannot be resolved.
	 */
	public LocalDate getLocalDate(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, LocalDate.class);
	}

	/**
	 * Gets a local date from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A local date from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a local date.
	 */
	public LocalDate getLocalDateOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, LocalDate.class);
	}

	/**
	 * Gets a local time from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A local time from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a local time, or if a table section of the key cannot be resolved.
	 */
	public LocalTime getLocalTime(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, LocalTime.class);
	}

	/**
	 * Gets a local time from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A local time from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a local time.
	 */
	public LocalTime getLocalTimeOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, LocalTime.class);
	}

	/**
	 * Gets an array from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return An array from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to an array, or if a table section of the key cannot be resolved.
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getArray(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, List.class);
	}

	/**
	 * Gets an array from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return An array from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to an array.
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getArrayOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, List.class);
	}

	/**
	 * Gets a table from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A table from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a table, or if a table section of the key cannot be resolved.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getTable(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, Map.class);
	}

	/**
	 * Gets a table from this configuration, or {@code null} if the key cannot be resolved.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A table from this map. May be null if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to a table.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getTableOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, Map.class);
	}

	/**
	 * Gets a value from this configuration, or returns a fallback if the key cannot be resolved.
	 *
	 * If the fallback is not {@code null}, the resolved value must be assignable to the fallback's type.
	 *
	 * @param <T> Expected value type.
	 * @param fullyQualifiedKey Fully-qualified TOML key.
	 * @param fallback Value to return if the key cannot be resolved.
	 *
	 * @return The resolved value, or the fallback if the key cannot be resolved.
	 * @throws ClassCastException Thrown if the resolved value cannot be cast to the expected type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getOr(
		String fullyQualifiedKey,
		T fallback
	) {
		if (fallback != null)
		{
			return this.getOr(fullyQualifiedKey, fallbackType(fallback), fallback);
		}

		Object value = this.get(fullyQualifiedKey, false);
		return value != null
			? (T) value
			: null;
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> fallbackType(
		T fallback
	) {
		if (fallback instanceof Map)
		{
			return (Class<T>) Map.class;
		}

		if (fallback instanceof List)
		{
			return (Class<T>) List.class;
		}

		return (Class<T>) fallback.getClass();
	}

	/**
	 * Gets a value from this configuration, or returns a fallback if the key cannot be resolved.
	 *
	 * @param <T> Expected value type.
	 * @param fullyQualifiedKey Fully-qualified TOML key.
	 * @param type Expected value class.
	 * @param fallback Value to return if the key cannot be resolved.
	 *
	 * @return The resolved value, or the fallback if the key cannot be resolved.
	 * @throws ClassCastException Thrown if the resolved value cannot be cast to the expected type.
	 */
	public <T> T getOr(
		String fullyQualifiedKey,
		Class<T> type,
		T fallback
	) {
		T value = this.getWithCast(fullyQualifiedKey, false, Objects.requireNonNull(type));
		return value != null
			? value
				: fallback;
	}

	/**
	 * Gets a value from this configuration, or calls a fallback supplier if the key cannot be resolved.
	 *
	 * @param <T> Expected value type.
	 * @param fullyQualifiedKey Fully-qualified TOML key.
	 * @param fallback Supplier to call if the key cannot be resolved.
	 *
	 * @return The resolved value, or the fallback supplier's value if the key cannot be resolved.
	 * @throws ClassCastException Thrown if the resolved value cannot be cast to the expected type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getOrElse(
		String fullyQualifiedKey,
		Supplier<T> fallback
	) {
		Objects.requireNonNull(fallback);

		T value = (T) this.get(fullyQualifiedKey, false);
		return value != null
			? value
				: fallback.get();
	}

	/**
	 * Gets a value from this configuration, or calls a fallback supplier if the key cannot be resolved.
	 *
	 * @param <T> Expected value type.
	 * @param fullyQualifiedKey Fully-qualified TOML key.
	 * @param type Expected value class.
	 * @param fallback Supplier to call if the key cannot be resolved.
	 *
	 * @return The resolved value, or the fallback supplier's value if the key cannot be resolved.
	 * @throws ClassCastException Thrown if the resolved value cannot be cast to the expected type.
	 */
	public <T> T getOrElse(
		String fullyQualifiedKey,
		Class<T> type,
		Supplier<? extends T> fallback
	) {
		Objects.requireNonNull(fallback);

		T value = this.getWithCast(fullyQualifiedKey, false, Objects.requireNonNull(type));
		return value != null
			? value
			: fallback.get();
	}
}
