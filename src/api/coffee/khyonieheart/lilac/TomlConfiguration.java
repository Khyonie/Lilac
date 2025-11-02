/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Convenience type which takes a loaded TOML map and allows for values to be referenced using TOML keys.
 *
 * All key lookups are cached for performance.
 *
 * This type distinguishes between two types of "get" methods: exception-throwing, and null-returning. 
 * Exception-throwing methods will throw an exception if any part of a key cannot be resolved.
 * Null-returning methods will return null if any part of a key cannot be resolved.
 *
 * Values returned from this object are and should be treated as read-only. 
 * Tables and arrays are converted into unmodifiable maps and lists.
 *
 * @since 2.1.0
 */
public class TomlConfiguration
{
	private final Map<String, Object> data;
	private Map<String, Object> cache = new HashMap<>();

	public TomlConfiguration(
		Map<String, Object> data
	) {
		this.data = Objects.requireNonNull(data);
	}

	@SuppressWarnings("unchecked")
	private Object get(
		String fullyQualifiedKey,
		boolean throwException
	) {
		String[] keys = TomlUtilities.fullyQualifiedKeyToArray(fullyQualifiedKey);

		// Rebuild key for cache
		String sanitizedKey = String.join(".", keys);

		if (cache.containsKey(sanitizedKey))
		{
			Object value = cache.get(sanitizedKey);
			if (value == null && throwException)
			{
				throw new NoSuchElementException("No such key \"" + fullyQualifiedKey + "\" in TOML configuration");
			}
			return cache.get(sanitizedKey);
		}
		
		// Otherwise lookup value iteratively
		Map<String, Object> targetMap = data;
		for (int i = 0; i < keys.length - 1; i++)
		{
			Object value = targetMap.get(keys[i]);
			if (value == null)
			{
				if (throwException)
				{
					throw new NoSuchElementException("No such value \"" + keys[i] + "\" of \"" + fullyQualifiedKey + "\" in TOML configuration");
				}

				return null;
			}

			if (!(value instanceof Map))
			{
				if (throwException)
				{
					throw new ClassCastException("Value at key \"" + keys[i] + "\" is of type " + value.getClass() + ", not a map");
				}

				return null;
			}

			targetMap = (Map<String, Object>) value;
		}

		Object value = targetMap.get(keys[keys.length - 1]);

		if (value == null && throwException)
		{
			throw new NoSuchElementException("No such value \"" + keys[keys.length - 1] + "\" of \"" + fullyQualifiedKey + "\" in TOML configuration");
		}

		// Collection values must be read-only
		if (value instanceof Map)
		{
			value = Collections.unmodifiableMap((Map<String, Object>) value);
		}

		if (value instanceof List)
		{
			value = Collections.unmodifiableList((List<Object>) value);
		}

		// Cache and return
		cache.put(sanitizedKey, value);
		return value;
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
	 * Gets an integer from this configuration.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key
	 *
	 * @return An integer from this map.
	 * @throws NoSuchElementException Thrown if any part of the key cannot be resolved.
	 * @throws ClassCastException Thrown if value cannot be cast to an integer, or if a table section of the key cannot be resolved.
	 */
	public int getInteger(
		String fullyQualifiedKey
	) {
		return this.getWithCast(fullyQualifiedKey, true, Integer.class);
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
}
