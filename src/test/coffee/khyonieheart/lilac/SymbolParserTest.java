package coffee.khyonieheart.lilac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.List;

import org.junit.jupiter.api.Test;

import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolParser;
import coffee.khyonieheart.lilac.symbol.TomlBoolean;
import coffee.khyonieheart.lilac.symbol.TomlFloat;

class SymbolParserTest
{
	@Test
	void eligibilityCheckDoesNotInvokeParserOrChangeDocument()
	{
		Document document = new Document("true ");
		document.pinPointer();
		SymbolParser parser = new SymbolParser(TomlBoolean.class, d -> {
			assertSame(document, d);
			return false;
		}, (d, owner, stack) -> {
			throw new AssertionError("Eligibility check must not invoke the parser");
		});

		assertFalse(parser.isApplicable(document));
		assertEquals(0, document.getPointer());
		assertEquals(1, document.getPinDepth());
		assertEquals(0, document.getPin());
	}

	@Test
	void defaultEligibilityAllowsParsing()
	{
		Document document = new Document("true ");
		var symbols = new ArrayDeque<Symbol<?>>();
		SymbolParser parser = new SymbolParser(TomlBoolean.class, TomlBoolean::tryParse);

		assertTrue(parser.isApplicable(document));
		Symbol<?> result = parser.tryParse(document, new LilacDecoder(TomlVersion.V1_1_0), symbols);
		assertNotNull(result);
		assertSame(TomlBoolean.class, parser.type());
		assertSame(result, symbols.peek());
		assertEquals(true, result.getValue());
		assertEquals(4, document.getPointer());
	}

	@Test
	void floatGuardRejectsInvalidStartsAndAllowsAllSupportedPrefixes()
	{
		var decoder = new LilacDecoder(TomlVersion.V1_1_0);
		for (String input : List.of(".5 ", "e2 ", "@ "))
		{
			Document document = new Document(input);
			document.pinPointer();
			var symbols = new ArrayDeque<Symbol<?>>();
			assertNull(TomlFloat.tryParse(document, decoder, symbols), input);
			assertEquals(0, document.getPointer(), input);
			assertEquals(1, document.getPinDepth(), input);
			assertTrue(symbols.isEmpty(), input);
		}
		for (String input : List.of("0.5 ", "1.5 ", "+1.5 ", "-1.5 ", "inf ", "+inf ", "-inf ", "nan ", "+nan ", "-nan "))
		{
			assertNotNull(TomlFloat.tryParse(new Document(input), decoder, new ArrayDeque<>()), input);
		}
	}
}
