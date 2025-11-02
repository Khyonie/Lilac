/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

public enum TomlWarning
{
	/** Supertables don't need to be explicitly defined. */
	UNNECESSARY_EXPLICIT_SUPERTABLE,
	/** Though multiline inline tables are allowed in v1.1, they're discouraged. */
	MULTILINE_INLINE_TABLE,
	/** Java does not support -nan, so -nan turns into +nan. */
	UNSUPPORTED_NEGATIVE_NaN,
	;
}
