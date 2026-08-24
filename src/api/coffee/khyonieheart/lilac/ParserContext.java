/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

/**
 * Parser context used to decide which TOML symbols may be read next.
 */
public enum ParserContext
{
	/** Parser is inside an inline table. */
	INLINE_TABLE,
	/** Parser is inside an array. */
	ARRAY,
	/** Parser is inside the document root. */
	ROOT
	;
}
