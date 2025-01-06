package coffee.khyonieheart.lilac.symbol;

public enum SymbolType
{
	KEY, // Bare, quoted, and literal
	VALUE, // Strings, integers, floats, booleans, times, arrays, inline tables, table-arrays
	TABLE_START,
	TABLE_END,
	ARRAY_START,
	ARRAY_END,
	INLINE_TABLE_START,
	INLINE_TABLE_END,
	TABLE_ARRAY_START,
	TABLE_ARRAY_END,
	KEY_SEPARATOR, // Dot symbol
	ARRAY_SEPARATOR, // Comma
	EQUALS,
	NEWLINE,
	COMMENT,
	DOCUMENT_START,
	DOCUMENT_END
	;
}
