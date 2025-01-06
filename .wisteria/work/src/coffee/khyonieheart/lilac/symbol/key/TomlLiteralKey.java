package coffee.khyonieheart.lilac.symbol.key;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.LilacStrings;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.TomlEndArrayTable;
import coffee.khyonieheart.lilac.symbol.TomlEndTable;
import coffee.khyonieheart.lilac.symbol.TomlEquals;
import coffee.khyonieheart.lilac.symbol.TomlKeySeparator;

public class TomlLiteralKey extends Symbol<String>
{
	private String value;
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlKeySeparator.class, TomlEquals.class, TomlEndArrayTable.class, TomlEndTable.class);

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		String string = LilacStrings.tryParseLiteralString(document);

		if (string == null)
		{
			return false;
		}

		this.value = string;
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
		return SymbolType.KEY;
	}

	@Override
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		return NEXT_SYMBOLS;
	}
}
