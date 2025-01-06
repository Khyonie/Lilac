package coffee.khyonieheart.lilac.symbol.string;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.TomlArraySeparator;
import coffee.khyonieheart.lilac.symbol.TomlComment;
import coffee.khyonieheart.lilac.symbol.TomlEndArray;
import coffee.khyonieheart.lilac.symbol.TomlEndInlineTable;
import coffee.khyonieheart.lilac.symbol.TomlEndOfDocument;
import coffee.khyonieheart.lilac.symbol.TomlStartTable;
import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;

public class TomlMultilineLiteralString extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartTable.class, TomlArraySeparator.class, TomlEndInlineTable.class, TomlEndArray.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	private static final Collection<Class<? extends Symbol<?>>> INLINE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndInlineTable.class);
	private static final Collection<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndArray.class);
	private String value;

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		document.pinPointer();
		for (int i = 0; i < 3; i++)
		{
			if (document.getCharAndIncrement() != '\'')
			{
				document.rewindToPin();
				return false; 
			}
		}

		StringBuilder builder = new StringBuilder();
		if (document.getCharAtPointer() == '\n')
		{
			document.incrementPointer();
		}

		while (document.hasNext())
		{
			char current = document.getCharAndIncrement();

			if (current == '\'')
			{
				int apostrophes = 1;
				while (document.hasNext() && document.getCharAtPointer() == '\'')
				{
					apostrophes++;
					document.incrementPointer();

					if (apostrophes > 5)
					{
						throw TomlSyntaxException.of("Too many apostrophes in multi-line literal TOML string", document);
					}
				}

				if (apostrophes < 3)
				{
					builder.append("'".repeat(apostrophes));
					continue;
				}

				builder.append("'".repeat(apostrophes - 3));
				break;
			}

			if (current <= '\b' || (current > '\n' && current <= '\u001F') || current == '\u007F')
			{
				throw TomlSyntaxException.of("Control characters cannot be used in TOML strings", document);
			}

			builder.append(current);
		}

		value = builder.toString();

		symbols.push(this);
		return true;
	}

	@Override
	public String getValue() 
	{
		return this.value;
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.VALUE;
	}

	@Override
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		if (decoder.getContext() == null)
		{
			return NEXT_SYMBOLS;
		}

		return switch (decoder.getContext()) {
			case ARRAY -> ARRAY_NEXT_SYMBOLS;
			case INLINE_TABLE -> INLINE_NEXT_SYMBOLS;
			case ROOT -> NEXT_SYMBOLS;
		};
	}
}
