package coffee.khyonieheart.lilac;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolParsers;
import coffee.khyonieheart.lilac.symbol.TomlArraySeparator;
import coffee.khyonieheart.lilac.symbol.SymbolParser;

class SymbolDiagnosticsTest
{
	@Test
	void everyTransitionHasADiagnosticName() throws Exception
	{
		for (var field : SymbolParsers.class.getFields())
		{
			SymbolParser parser = (SymbolParser) field.get(null);
			Class<?> type = parser.type();
			assertNotNull(type, field.getName());
			Symbol<?> symbol = (Symbol<?>) type.getConstructor().newInstance();
			for (TomlVersion version : TomlVersion.values())
			{
				for (ParserContext context : new ParserContext[] { null, ParserContext.ROOT, ParserContext.ARRAY, ParserContext.INLINE_TABLE })
				{
					if (type == TomlArraySeparator.class && (context == null || context == ParserContext.ROOT))
					{
						continue;
					}
					LilacDecoder decoder = new LilacDecoder(version);
					if (context != null)
					{
						decoder.addContext(context);
					}
					for (SymbolParser next : symbol.getNextSymbols(decoder))
					{
						assertNotNull(next.type(), type.getSimpleName() + "/" + context + "/" + version);
					}
				}
			}
		}
	}

	@Test
	void invalidInputReportsExpectedSymbolNames()
	{
		for (TomlVersion version : TomlVersion.values())
		{
			assertExpectedSymbols(version, "a ? 1\n", "TomlKeySeparator, TomlEquals, TomlEndArrayTable, TomlEndTable");
			assertExpectedSymbols(version, "a = [1 ?]\n", "TomlComment, TomlArraySeparator, TomlEndArray");
			assertExpectedSymbols(version, "a = { b = 1 ? }\n", "TomlComment, TomlArraySeparator, TomlEndInlineTable");
		}
	}

	private void assertExpectedSymbols(TomlVersion version, String input, String expected)
	{
		TomlSyntaxException error = assertThrows(TomlSyntaxException.class, () -> new LilacDecoder(version).decode(input));
		assertTrue(error.getMessage().contains("Expected one of [ " + expected + " ]"), error.getMessage());
	}
}
