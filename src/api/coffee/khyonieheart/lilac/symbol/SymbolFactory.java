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
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

public class SymbolFactory
{
	private static final Map<Class<?>, Supplier<Symbol<?>>> SYMBOL_REGISTRY;
	private static final Map<Class<?>, SymbolParseFunction> SYMBOL_PARSE_REGISTRY;

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
			put(TomlBareKey.class, TomlBareKey::tryParse);
			put(TomlLiteralKey.class, TomlLiteralKey::tryParse);
			put(TomlQuotedKey.class, TomlQuotedKey::tryParse);

			// string
			put(TomlLiteralString.class, TomlLiteralString::tryParse);
			put(TomlMultilineLiteralString.class, TomlMultilineLiteralString::tryParse);
			put(TomlMultilineString.class, TomlMultilineString::tryParse);
			put(TomlRegularString.class, TomlRegularString::tryParse);
			
			// time
			put(TomlDate.class, TomlDate::tryParse);
			put(TomlDateTime.class, TomlDateTime::tryParse);
			put(TomlOffsetDateTime.class, TomlOffsetDateTime::tryParse);
			put(TomlTime.class, TomlTime::tryParse);

			// Others
			put(TomlArraySeparator.class, TomlArraySeparator::tryParse);
			put(TomlBoolean.class, TomlBoolean::tryParse);
			put(TomlComment.class, TomlComment::tryParse);
			put(TomlDecimal.class, TomlDecimal::tryParse);
			put(TomlEndArray.class, TomlEndArray::tryParse);
			put(TomlEndArrayTable.class, TomlEndArrayTable::tryParse);
			put(TomlEndInlineTable.class, TomlEndInlineTable::tryParse);
			put(TomlEndOfDocument.class, TomlEndOfDocument::tryParse);
			put(TomlEndTable.class, TomlEndTable::tryParse);
			put(TomlEquals.class, TomlEquals::tryParse);
			put(TomlFloat.class, TomlFloat::tryParse);
			put(TomlKeySeparator.class, TomlKeySeparator::tryParse);
			put(TomlNewline.class, TomlNewline::tryParse);
			put(TomlStartArray.class, TomlStartArray::tryParse);
			put(TomlStartArrayTable.class, TomlStartArrayTable::tryParse);
			put(TomlStartInlineTable.class, TomlStartInlineTable::tryParse);
			put(TomlStartOfDocument.class, TomlStartOfDocument::tryParse);
			put(TomlStartTable.class, TomlStartTable::tryParse);
		}};
	}

	public static Symbol<?> getInstance(Class<? extends Symbol<?>> clazz)
	{
		return SYMBOL_REGISTRY.get(clazz).get();
	}

	public static Symbol<?> tryParse(Class<? extends Symbol<?>> clazz, Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		return SYMBOL_PARSE_REGISTRY.get(clazz).apply(document, decoder, symbols);
	}
}
