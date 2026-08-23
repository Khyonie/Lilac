/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

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

	public TomlConfiguration(
		Map<String, Object> data
	) {
		this.lookup = new TomlConfigurationLookup(data);
	}

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

	public int getInteger(
		String fullyQualifiedKey
	) {
		return Math.toIntExact(this.getLong(fullyQualifiedKey));
	}

	/**
	 * Gets a float from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return A float from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to an float, or if a table section of the key cannot be resolved.
	 */
	public float getFloat(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, Float.class);
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

	public OffsetDateTime getOffsetDateTime(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, OffsetDateTime.class);
	}

	public OffsetDateTime getOffsetDateTimeOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, OffsetDateTime.class);
	}

	public LocalDateTime getLocalDateTime(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, LocalDateTime.class);
	}

	public LocalDateTime getLocalDateTimeOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, LocalDateTime.class);
	}

	public LocalDate getLocalDate(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, LocalDate.class);
	}

	public LocalDate getLocalDateOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, LocalDate.class);
	}

	public LocalTime getLocalTime(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, LocalTime.class);
	}

	public LocalTime getLocalTimeOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, LocalTime.class);
	}

	@SuppressWarnings("unchecked")
	public List<Object> getArray(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, List.class);
	}

	@SuppressWarnings("unchecked")
	public List<Object> getArrayOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, List.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getTable(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, Map.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getTableOrNull(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, false, Map.class);
	}

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
