/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.decoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import coffee.khyonieheart.lilac.symbol.SymbolParsers;
import coffee.khyonieheart.lilac.symbol.SymbolParser;

/**
 * Reusable collections of symbols
 */
public class LilacSymbols
{
	/** Symbols valid at the start of a document */
	public static final List<SymbolParser> STARTER_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.START_ARRAY_TABLE, SymbolParsers.START_TABLE, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY, SymbolParsers.END_OF_DOCUMENT);
	/** Symbols for all key types */
	public static final List<SymbolParser> KEY_SYMBOLS = build().add(SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY).create();
	/** Symbols for all value types */
	public static final List<SymbolParser> VALUE_SYMBOLS = build().add(SymbolParsers.MULTILINE_STRING, SymbolParsers.MULTILINE_LITERAL_STRING, SymbolParsers.REGULAR_STRING, SymbolParsers.LITERAL_STRING, SymbolParsers.START_ARRAY, SymbolParsers.START_INLINE_TABLE, SymbolParsers.BOOLEAN, SymbolParsers.OFFSET_DATE_TIME, SymbolParsers.DATE_TIME, SymbolParsers.DATE, SymbolParsers.TIME, SymbolParsers.NUMBER).create();

	public static LilacSymbolCollectionBuilder build()
	{
		return new LilacSymbolCollectionBuilder();
	}

	public static class LilacSymbolCollectionBuilder
	{
		private List<SymbolParser> symbols = new ArrayList<>();

		/** Internal constructor. Use {@link LilacSymbols#build()}. */
		private LilacSymbolCollectionBuilder() {}

		@SafeVarargs
		public final LilacSymbolCollectionBuilder add(
			Collection<SymbolParser>... symbols
		) {
			for (Collection<SymbolParser> symbolsCollection : symbols)
			{
				for (SymbolParser symbolParser : symbolsCollection)
				{
					this.symbols.add(symbolParser);
				}
			}

			return this;
		}

		@SafeVarargs
		public final LilacSymbolCollectionBuilder add(
			SymbolParser... symbols
		) {
			for (SymbolParser symbol : symbols)
			{
				this.symbols.add(symbol);
			}

			return this;
		}

		public List<SymbolParser> create()
		{
			return this.symbols;
		}
	}
}
