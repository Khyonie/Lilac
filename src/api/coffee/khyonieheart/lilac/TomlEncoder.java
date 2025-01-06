/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.Map;

public interface TomlEncoder
{
	public String encode(
		Map<String, Object> data
	);
}
