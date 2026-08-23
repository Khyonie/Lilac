package coffee.khyonieheart.lilac;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class LilacEncoderTest
{
	@Test
	void encodesIntegerAndBooleanValues()
	{
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("i32", 1);
		data.put("i64", 2L);
		data.put("enabled", true);

		Map<String, Object> decoded = assertDoesNotThrow(() -> new LilacDecoder(TomlVersion.V1_1_0).decode(new LilacEncoder().encode(data)));

		assertEquals(1L, decoded.get("i32"));
		assertEquals(2L, decoded.get("i64"));
		assertEquals(true, decoded.get("enabled"));
	}

	@Test
	void rejectsUnsupportedValuesByDefault()
	{
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("unsupported", new UnsupportedValue());

		assertThrows(IllegalArgumentException.class, () -> new LilacEncoder().encode(data));
	}

	@Test
	void skipsUnsupportedValuesWithoutEmittingInvalidToml()
	{
		Map<String, Object> inlineTable = new LinkedHashMap<>();
		inlineTable.put("keep", "value");
		inlineTable.put("drop", new UnsupportedValue());

		List<Object> array = new ArrayList<>();
		array.add(1);
		array.add(new UnsupportedValue());
		array.add(null);
		array.add(true);
		array.add(inlineTable);

		Map<String, Object> childTable = new LinkedHashMap<>();
		childTable.put("drop", new UnsupportedValue());
		childTable.put("keep", 2L);

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("drop", new UnsupportedValue());
		data.put("array", array);
		data.put("child", childTable);

		String encoded = new LilacEncoder()
			.setSkipNonTomlTypes(true)
			.encode(data);

		assertFalse(encoded.contains("drop"));
		assertFalse(encoded.contains("null"));
		assertFalse(encoded.contains("UnsupportedValue"));

		Map<String, Object> decoded = assertDoesNotThrow(() -> new LilacDecoder(TomlVersion.V1_1_0).decode(encoded));
		List<?> decodedArray = (List<?>) decoded.get("array");
		Map<?, ?> decodedChildTable = (Map<?, ?>) decoded.get("child");

		assertEquals(3, decodedArray.size());
		assertEquals(1L, decodedArray.get(0));
		assertEquals(true, decodedArray.get(1));
		assertEquals(2L, decodedChildTable.get("keep"));
	}

	private static final class UnsupportedValue
	{}
}
