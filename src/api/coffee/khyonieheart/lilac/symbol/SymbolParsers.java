package coffee.khyonieheart.lilac.symbol;

import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;
import coffee.khyonieheart.lilac.symbol.string.TomlLiteralString;
import coffee.khyonieheart.lilac.symbol.string.TomlMultilineLiteralString;
import coffee.khyonieheart.lilac.symbol.string.TomlMultilineString;
import coffee.khyonieheart.lilac.symbol.string.TomlRegularString;
import coffee.khyonieheart.lilac.symbol.time.TomlDate;
import coffee.khyonieheart.lilac.symbol.time.TomlDateTime;
import coffee.khyonieheart.lilac.symbol.time.TomlOffsetDateTime;
import coffee.khyonieheart.lilac.symbol.time.TomlTime;
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

/**
 * Shared parser references for symbol transitions and diagnostic reverse lookups.
 */
public final class SymbolParsers
{
	public static final SymbolParseFunction BARE_KEY = TomlBareKey::tryParse;
	public static final SymbolParseFunction LITERAL_KEY = TomlLiteralKey::tryParse;
	public static final SymbolParseFunction QUOTED_KEY = TomlQuotedKey::tryParse;
	public static final SymbolParseFunction LITERAL_STRING = TomlLiteralString::tryParse;
	public static final SymbolParseFunction MULTILINE_LITERAL_STRING = TomlMultilineLiteralString::tryParse;
	public static final SymbolParseFunction MULTILINE_STRING = TomlMultilineString::tryParse;
	public static final SymbolParseFunction REGULAR_STRING = TomlRegularString::tryParse;
	public static final SymbolParseFunction DATE = TomlDate::tryParse;
	public static final SymbolParseFunction DATE_TIME = TomlDateTime::tryParse;
	public static final SymbolParseFunction OFFSET_DATE_TIME = TomlOffsetDateTime::tryParse;
	public static final SymbolParseFunction TIME = TomlTime::tryParse;
	public static final SymbolParseFunction ARRAY_SEPARATOR = TomlArraySeparator::tryParse;
	public static final SymbolParseFunction BOOLEAN = TomlBoolean::tryParse;
	public static final SymbolParseFunction COMMENT = TomlComment::tryParse;
	public static final SymbolParseFunction DECIMAL = TomlDecimal::tryParse;
	public static final SymbolParseFunction END_ARRAY = TomlEndArray::tryParse;
	public static final SymbolParseFunction END_ARRAY_TABLE = TomlEndArrayTable::tryParse;
	public static final SymbolParseFunction END_INLINE_TABLE = TomlEndInlineTable::tryParse;
	public static final SymbolParseFunction END_OF_DOCUMENT = TomlEndOfDocument::tryParse;
	public static final SymbolParseFunction END_TABLE = TomlEndTable::tryParse;
	public static final SymbolParseFunction EQUALS = TomlEquals::tryParse;
	public static final SymbolParseFunction FLOAT = TomlFloat::tryParse;
	public static final SymbolParseFunction KEY_SEPARATOR = TomlKeySeparator::tryParse;
	public static final SymbolParseFunction NEWLINE = TomlNewline::tryParse;
	public static final SymbolParseFunction START_ARRAY = TomlStartArray::tryParse;
	public static final SymbolParseFunction START_ARRAY_TABLE = TomlStartArrayTable::tryParse;
	public static final SymbolParseFunction START_INLINE_TABLE = TomlStartInlineTable::tryParse;
	public static final SymbolParseFunction START_OF_DOCUMENT = TomlStartOfDocument::tryParse;
	public static final SymbolParseFunction START_TABLE = TomlStartTable::tryParse;

	private SymbolParsers() {}
}
