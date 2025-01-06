/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.Map;

public interface TomlEncoder
{
	/**
	 * Encodes the given map as a TOML string.
	 *
	 * @param data Map to encode
	 *
	 * @return TOML string representing the given map.
	 */
	public String encode(
		Map<String, Object> data
	);
}
