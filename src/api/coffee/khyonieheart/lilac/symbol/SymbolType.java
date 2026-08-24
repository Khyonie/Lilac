/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

/**
 * Categories of parser symbols used by Lilac's scanner and parser.
 */
public enum SymbolType
{
	/** Bare, quoted, and literal keys. */
	KEY,
	/** Strings, integers, floats, booleans, times, arrays, inline tables, and table-arrays. */
	VALUE,
	/** Start of a standard table. */
	TABLE_START,
	/** End of a standard table. */
	TABLE_END,
	/** Start of an array. */
	ARRAY_START,
	/** End of an array. */
	ARRAY_END,
	/** Start of an inline table. */
	INLINE_TABLE_START,
	/** End of an inline table. */
	INLINE_TABLE_END,
	/** Start of an array of tables. */
	TABLE_ARRAY_START,
	/** End of an array of tables. */
	TABLE_ARRAY_END,
	/** Dot symbol between dotted key segments. */
	KEY_SEPARATOR,
	/** Comma between array or inline-table values. */
	ARRAY_SEPARATOR,
	/** Equals sign between a key and value. */
	EQUALS,
	/** Newline symbol. */
	NEWLINE,
	/** Comment symbol. */
	COMMENT,
	/** Start of document marker. */
	DOCUMENT_START,
	/** End of document marker. */
	DOCUMENT_END
	;
}
