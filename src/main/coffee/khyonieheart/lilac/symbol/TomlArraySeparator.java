/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.ParserContext;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlVersion;
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

public class TomlArraySeparator extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartArray.class, TomlEndArray.class, TomlStartInlineTable.class, TomlBoolean.class, TomlMultilineString.class, TomlMultilineLiteralString.class, TomlRegularString.class, TomlLiteralString.class, TomlOffsetDateTime.class, TomlDateTime.class, TomlDate.class, TomlTime.class, TomlDecimal.class, TomlFloat.class);
	private static final List<Class<? extends Symbol<?>>> INLINE_TABLE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class);
	private static final List<Class<? extends Symbol<?>>> V1_1_INLINE_TABLE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndInlineTable.class);

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		if (document.getCharAtPointer() != ',')
		{
			return false;
		}

		symbols.push(this);
		document.incrementPointer();
		return true;
	}

	@Override
	public String getValue() 
	{
		return "<Array separator>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.ARRAY_SEPARATOR;
	}

	@Override
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		ParserContext context = decoder.getContext();

		if (context == null)
		{
			throw new IllegalStateException("Context must not be null when getting next symbols for array");
		}

		return switch (context) {
			case ARRAY -> ARRAY_NEXT_SYMBOLS;
			case INLINE_TABLE -> decoder.getVersion() == TomlVersion.V1_0_0 ? INLINE_TABLE_NEXT_SYMBOLS : V1_1_INLINE_TABLE_NEXT_SYMBOLS;
			case ROOT -> throw new IllegalStateException("Context not valid for lexeme");
		};
	}
}
