package coffee.khyonieheart.lilac.symbol;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;

public class TomlEndArrayTable extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = TomlStartOfDocument.NEXT_SYMBOLS;

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		document.pinPointer();
		if (document.getCharAndIncrement() == ']')
		{
			if (document.getCharAndIncrement() == ']')
			{
				symbols.push(this);
				return true;
			}
		}

		document.rewindToPin();
		return false;
	}

	@Override
	public String getValue() 
	{
		return "<End of array of tables>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.TABLE_ARRAY_END;
	}

	@Override
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		return NEXT_SYMBOLS;
	}
}
