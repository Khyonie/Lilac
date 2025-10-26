/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Convenience type which takes a loaded TOML map and allows for values to be 
 * referenced using TOML keys.
 *
 * Successful key lookups are cached for performance.
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

	private Object get(
		String fullyQualifiedKey
	) {
		return null;
	}
}
