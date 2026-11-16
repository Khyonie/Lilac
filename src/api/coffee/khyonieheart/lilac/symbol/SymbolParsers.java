package coffee.khyonieheart.lilac.symbol;

import coffee.khyonieheart.lilac.Document;
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

/**
 * Shared symbol parsers for transitions and diagnostics. Eligibility checks default to true.
 */
public final class SymbolParsers
{
	public static final SymbolParser BARE_KEY = new SymbolParser(TomlBareKey.class, TomlBareKey::tryParse);
	public static final SymbolParser LITERAL_KEY = new SymbolParser(TomlLiteralKey.class, TomlLiteralKey::tryParse);
	public static final SymbolParser QUOTED_KEY = new SymbolParser(TomlQuotedKey.class, TomlQuotedKey::tryParse);
	public static final SymbolParser LITERAL_STRING = new SymbolParser(TomlLiteralString.class, SymbolParsers::tryLiteralString, TomlLiteralString::tryParse);
	public static final SymbolParser MULTILINE_LITERAL_STRING = new SymbolParser(TomlMultilineLiteralString.class, SymbolParsers::tryMultilineLiteralString, TomlMultilineLiteralString::tryParse);
	public static final SymbolParser MULTILINE_STRING = new SymbolParser(TomlMultilineString.class, SymbolParsers::tryMultilineString, TomlMultilineString::tryParse);
	public static final SymbolParser REGULAR_STRING = new SymbolParser(TomlRegularString.class, SymbolParsers::tryString, TomlRegularString::tryParse);
	public static final SymbolParser DATE = new SymbolParser(TomlDate.class, TomlDate::tryParse);
	public static final SymbolParser DATE_TIME = new SymbolParser(TomlDateTime.class, TomlDateTime::tryParse);
	public static final SymbolParser OFFSET_DATE_TIME = new SymbolParser(TomlOffsetDateTime.class, TomlOffsetDateTime::tryParse);
	public static final SymbolParser TIME = new SymbolParser(TomlTime.class, TomlTime::tryParse);
	public static final SymbolParser ARRAY_SEPARATOR = new SymbolParser(TomlArraySeparator.class, SymbolParsers::tryArraySeparator, TomlArraySeparator::tryParse);
	public static final SymbolParser BOOLEAN = new SymbolParser(TomlBoolean.class, TomlBoolean::tryParse);
	public static final SymbolParser COMMENT = new SymbolParser(TomlComment.class, SymbolParsers::tryComment, TomlComment::tryParse);
	public static final SymbolParser DECIMAL = new SymbolParser(TomlDecimal.class, TomlDecimal::tryParse);
	public static final SymbolParser END_ARRAY = new SymbolParser(TomlEndArray.class, SymbolParsers::tryEndArray, TomlEndArray::tryParse);
	public static final SymbolParser END_ARRAY_TABLE = new SymbolParser(TomlEndArrayTable.class, SymbolParsers::tryEndArrayTable, TomlEndArrayTable::tryParse);
	public static final SymbolParser END_INLINE_TABLE = new SymbolParser(TomlEndInlineTable.class, SymbolParsers::tryEndInlineTable, TomlEndInlineTable::tryParse);
	public static final SymbolParser END_OF_DOCUMENT = new SymbolParser(TomlEndOfDocument.class, TomlEndOfDocument::tryParse);
	public static final SymbolParser END_TABLE = new SymbolParser(TomlEndTable.class, SymbolParsers::tryEndArray, TomlEndTable::tryParse);
	public static final SymbolParser EQUALS = new SymbolParser(TomlEquals.class, TomlEquals::tryParse);
	public static final SymbolParser FLOAT = new SymbolParser(TomlFloat.class, TomlFloat::tryParse);
	public static final SymbolParser KEY_SEPARATOR = new SymbolParser(TomlKeySeparator.class, TomlKeySeparator::tryParse);
	public static final SymbolParser NEWLINE = new SymbolParser(TomlNewline.class, TomlNewline::tryParse);
	public static final SymbolParser START_ARRAY = new SymbolParser(TomlStartArray.class, SymbolParsers::tryStartArray, TomlStartArray::tryParse);
	public static final SymbolParser START_ARRAY_TABLE = new SymbolParser(TomlStartArrayTable.class, SymbolParsers::tryStartArrayTable, TomlStartArrayTable::tryParse);
	public static final SymbolParser START_INLINE_TABLE = new SymbolParser(TomlStartInlineTable.class, SymbolParsers::tryStartInlineTable, TomlStartInlineTable::tryParse);
	public static final SymbolParser START_OF_DOCUMENT = new SymbolParser(TomlStartOfDocument.class, TomlStartOfDocument::tryParse);
	public static final SymbolParser START_TABLE = new SymbolParser(TomlStartTable.class, SymbolParsers::tryStartArray, TomlStartTable::tryParse);

	private SymbolParsers() {}

	// Validators for potential work
	//---------------------------------------- 

	private static boolean tryComment(Document document)
	{
		return document.getCharAtPointer() == '#';
	}

	private static boolean tryMultilineLiteralString(Document document)
	{
		return document.matchsSubstringAtPointer("'''");
	}

	private static boolean tryMultilineString(Document document)
	{
		return document.matchsSubstringAtPointer("\"\"\"");
	}

	private static boolean tryString(Document document)
	{
		return document.getCharAtPointer() == '"';
	}

	private static boolean tryLiteralString(Document document)
	{
		return document.getCharAtPointer() == '\'';
	}

	private static boolean tryArraySeparator(Document document)
	{
		return document.getCharAtPointer() == ',';
	}

	private static boolean tryStartArray(Document document)
	{
		return document.getCharAtPointer() == '[';
	}

	private static boolean tryEndArray(Document document)
	{
		return document.getCharAtPointer() == ']';
	}

	private static boolean tryStartArrayTable(Document document)
	{
		return document.matchsSubstringAtPointer("[[");
	}

	private static boolean tryEndArrayTable(Document document)
	{
		return document.matchsSubstringAtPointer("]]");
	}

	private static boolean tryStartInlineTable(Document document)
	{
		return document.getCharAtPointer() == '{';
	}

	private static boolean tryEndInlineTable(Document document)
	{
		return document.getCharAtPointer() == '}';
	}
}
