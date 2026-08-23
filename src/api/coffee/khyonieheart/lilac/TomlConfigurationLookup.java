/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

final class TomlConfigurationLookup
{
	private final Map<String, Object> data;
	private final Map<List<String>, Object> cache = new HashMap<>();

	TomlConfigurationLookup(
		Map<String, Object> data
	) {
		this.data = Objects.requireNonNull(data);
	}

	@SuppressWarnings("unchecked")
	Object get(
		String fullyQualifiedKey,
		boolean throwException
	) {
		String[] keys = TomlUtilities.fullyQualifiedKeyToArray(fullyQualifiedKey);
		List<String> cacheKey = List.of(keys);

		if (cache.containsKey(cacheKey))
		{
			Object value = cache.get(cacheKey);
			if (value == null && throwException)
			{
				throw new NoSuchElementException("No such key \"" + fullyQualifiedKey + "\" in TOML configuration");
			}
			return cache.get(cacheKey);
		}

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

		value = makeReadOnly(value);
		cache.put(cacheKey, value);
		return value;
	}

	@SuppressWarnings("unchecked")
	private static Object makeReadOnly(
		Object value
	) {
		if (value instanceof Map)
		{
			return Collections.unmodifiableMap((Map<String, Object>) value);
		}

		if (value instanceof List)
		{
			return Collections.unmodifiableList((List<Object>) value);
		}

		return value;
	}
}
