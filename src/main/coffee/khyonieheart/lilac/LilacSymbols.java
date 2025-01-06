/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.TomlBoolean;
import coffee.khyonieheart.lilac.symbol.TomlComment;
import coffee.khyonieheart.lilac.symbol.TomlDecimal;
import coffee.khyonieheart.lilac.symbol.TomlEndOfDocument;
import coffee.khyonieheart.lilac.symbol.TomlFloat;
import coffee.khyonieheart.lilac.symbol.TomlStartArray;
import coffee.khyonieheart.lilac.symbol.TomlStartArrayTable;
import coffee.khyonieheart.lilac.symbol.TomlStartInlineTable;
import coffee.khyonieheart.lilac.symbol.TomlStartTable;
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
 * Reusable collections of symbols
 */
public class LilacSymbols
{
	/** Symbols valid at the start of a document */
	public static final Collection<Class<? extends Symbol<?>>> STARTER_SYMBOLS = List.of(TomlComment.class, TomlStartArrayTable.class, TomlStartTable.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	/** Symbols for all key types */
	public static final Collection<Class<? extends Symbol<?>>> KEY_SYMBOLS = build().add(TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class).create();
	/** Symbols for all value types */
	public static final Collection<Class<? extends Symbol<?>>> VALUE_SYMBOLS = build().add(TomlMultilineString.class, TomlMultilineLiteralString.class, TomlRegularString.class, TomlLiteralString.class, TomlStartArray.class, TomlStartInlineTable.class, TomlBoolean.class, TomlOffsetDateTime.class, TomlDateTime.class, TomlDate.class, TomlTime.class, TomlFloat.class, TomlDecimal.class).create();

	public static LilacSymbolCollectionBuilder build()
	{
		return new LilacSymbolCollectionBuilder();
	}

	public static class LilacSymbolCollectionBuilder
	{
		private List<Class<? extends Symbol<?>>> symbols = new ArrayList<>();

		/** Internal constructor. Use {@link LilacSymbols#build()}. */
		private LilacSymbolCollectionBuilder() {}

		@SafeVarargs
		public final LilacSymbolCollectionBuilder add(
			Collection<Class<? extends Symbol<?>>>... symbols
		) {
			for (Collection<Class<? extends Symbol<?>>> symbolsCollection : symbols)
			{
				for (Class<? extends Symbol<?>> symbolClass : symbolsCollection)
				{
					this.symbols.add(symbolClass);
				}
			}

			return this;
		}

		@SafeVarargs
		public final LilacSymbolCollectionBuilder add(
			Class<? extends Symbol<?>>... symbols
		) {
			for (Class<? extends Symbol<?>> symbol : symbols)
			{
				this.symbols.add(symbol);
			}

			return this;
		}

		public Collection<Class<? extends Symbol<?>>> create()
		{
			return this.symbols;
		}
	}
}
