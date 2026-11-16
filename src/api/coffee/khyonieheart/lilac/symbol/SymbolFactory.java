package coffee.khyonieheart.lilac.symbol;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
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

public class SymbolFactory
{
	private static final Map<Class<?>, Supplier<Symbol<?>>> SYMBOL_REGISTRY;
	private static final Map<Class<?>, SymbolParser> SYMBOL_PARSE_REGISTRY;

	static {
		SYMBOL_REGISTRY = new HashMap<>() {{
			// key
			put(TomlBareKey.class, () -> new TomlBareKey());
			put(TomlLiteralKey.class, () -> new TomlLiteralKey());
			put(TomlQuotedKey.class, () -> new TomlQuotedKey());

			// string
			put(TomlLiteralString.class, () -> new TomlLiteralString());
			put(TomlMultilineLiteralString.class, () -> new TomlMultilineLiteralString());
			put(TomlMultilineString.class, () -> new TomlMultilineString());
			put(TomlRegularString.class, () -> new TomlRegularString());
			
			// time
			put(TomlDate.class, () -> new TomlDate());
			put(TomlDateTime.class, () -> new TomlDateTime());
			put(TomlOffsetDateTime.class, () -> new TomlOffsetDateTime());
			put(TomlTime.class, () -> new TomlTime());

			// Others
			put(TomlArraySeparator.class, () -> new TomlArraySeparator());
			put(TomlBoolean.class, () -> new TomlBoolean());
			put(TomlComment.class, () -> new TomlComment());
			put(TomlDecimal.class, () -> new TomlDecimal());
			put(TomlEndArray.class, () -> new TomlEndArray());
			put(TomlEndArrayTable.class, () -> new TomlEndArrayTable());
			put(TomlEndInlineTable.class, () -> new TomlEndInlineTable());
			put(TomlEndOfDocument.class, () -> new TomlEndOfDocument());
			put(TomlEndTable.class, () -> new TomlEndTable());
			put(TomlEquals.class, () -> new TomlEquals());
			put(TomlFloat.class, () -> new TomlFloat());
			put(TomlKeySeparator.class, () -> new TomlKeySeparator());
			put(TomlNewline.class, () -> new TomlNewline());
			put(TomlStartArray.class, () -> new TomlStartArray());
			put(TomlStartArrayTable.class, () -> new TomlStartArrayTable());
			put(TomlStartInlineTable.class, () -> new TomlStartInlineTable());
			put(TomlStartOfDocument.class, () -> new TomlStartOfDocument());
			put(TomlStartTable.class, () -> new TomlStartTable());
		}};

		SYMBOL_PARSE_REGISTRY = new HashMap<>() {{
			// key
			put(TomlBareKey.class, SymbolParsers.BARE_KEY);
			put(TomlLiteralKey.class, SymbolParsers.LITERAL_KEY);
			put(TomlQuotedKey.class, SymbolParsers.QUOTED_KEY);

			// string
			put(TomlLiteralString.class, SymbolParsers.LITERAL_STRING);
			put(TomlMultilineLiteralString.class, SymbolParsers.MULTILINE_LITERAL_STRING);
			put(TomlMultilineString.class, SymbolParsers.MULTILINE_STRING);
			put(TomlRegularString.class, SymbolParsers.REGULAR_STRING);
			
			// time
			put(TomlDate.class, SymbolParsers.DATE);
			put(TomlDateTime.class, SymbolParsers.DATE_TIME);
			put(TomlOffsetDateTime.class, SymbolParsers.OFFSET_DATE_TIME);
			put(TomlTime.class, SymbolParsers.TIME);

			// Others
			put(TomlArraySeparator.class, SymbolParsers.ARRAY_SEPARATOR);
			put(TomlBoolean.class, SymbolParsers.BOOLEAN);
			put(TomlComment.class, SymbolParsers.COMMENT);
			put(TomlDecimal.class, SymbolParsers.DECIMAL);
			put(TomlEndArray.class, SymbolParsers.END_ARRAY);
			put(TomlEndArrayTable.class, SymbolParsers.END_ARRAY_TABLE);
			put(TomlEndInlineTable.class, SymbolParsers.END_INLINE_TABLE);
			put(TomlEndOfDocument.class, SymbolParsers.END_OF_DOCUMENT);
			put(TomlEndTable.class, SymbolParsers.END_TABLE);
			put(TomlEquals.class, SymbolParsers.EQUALS);
			put(TomlFloat.class, SymbolParsers.FLOAT);
			put(TomlKeySeparator.class, SymbolParsers.KEY_SEPARATOR);
			put(TomlNewline.class, SymbolParsers.NEWLINE);
			put(TomlStartArray.class, SymbolParsers.START_ARRAY);
			put(TomlStartArrayTable.class, SymbolParsers.START_ARRAY_TABLE);
			put(TomlStartInlineTable.class, SymbolParsers.START_INLINE_TABLE);
			put(TomlStartOfDocument.class, SymbolParsers.START_OF_DOCUMENT);
			put(TomlStartTable.class, SymbolParsers.START_TABLE);
		}};
	}

	public static Symbol<?> getInstance(Class<? extends Symbol<?>> clazz)
	{
		return SYMBOL_REGISTRY.get(clazz).get();
	}

	public static Symbol<?> tryParse(Class<? extends Symbol<?>> clazz, Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		SymbolParser parser = SYMBOL_PARSE_REGISTRY.get(clazz);
		return parser.isApplicable(document) ? parser.tryParse(document, decoder, symbols) : null;
	}
}
