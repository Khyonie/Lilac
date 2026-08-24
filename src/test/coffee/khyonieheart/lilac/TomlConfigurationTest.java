package coffee.khyonieheart.lilac;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class TomlConfigurationTest
{
	@Test
	void containsReturnsFalseForMissingOrUnresolvablePaths()
	{
		TomlConfiguration configuration = configuration();

		assertTrue(configuration.contains("server.host"));
		assertFalse(configuration.contains("server.missing"));
		assertFalse(configuration.contains("enabled.value"));
	}

	@Test
	void getOrReturnsFallbackOnlyWhenValueIsMissing()
	{
		TomlConfiguration configuration = configuration();

		assertEquals("localhost", configuration.getOr("server.host", "fallback"));
		assertEquals("fallback", configuration.getOr("server.missing", "fallback"));
		assertEquals(8080L, configuration.getOr("server.port", Long.class, 9000L));
		assertEquals(9000L, configuration.getOr("server.missing", Long.class, 9000L));
		assertThrows(ClassCastException.class, () -> configuration.getOr("server.port", "not-a-number"));
	}

	@Test
	void getOrElseOnlyCallsFallbackWhenValueIsMissing()
	{
		TomlConfiguration configuration = configuration();
		AtomicBoolean presentFallbackCalled = new AtomicBoolean();
		AtomicBoolean missingFallbackCalled = new AtomicBoolean();

		String host = configuration.getOrElse("server.host", String.class, () -> {
			presentFallbackCalled.set(true);
			return "fallback";
		});
		Long port = configuration.getOrElse("server.missing", Long.class, () -> {
			missingFallbackCalled.set(true);
			return 9000L;
		});

		assertEquals("localhost", host);
		assertEquals(9000L, port);
		assertFalse(presentFallbackCalled.get());
		assertTrue(missingFallbackCalled.get());
	}

	@Test
	void nullablePrimitiveGettersReturnNullForMissingValues()
	{
		TomlConfiguration configuration = configuration();

		assertEquals(Long.valueOf(8080L), configuration.getLongOrNull("server.port"));
		assertEquals(Integer.valueOf(8080), configuration.getIntegerOrNull("server.port"));
		assertEquals(Float.valueOf(1.5f), configuration.getFloatOrNull("ratio"));
		assertEquals(Boolean.TRUE, configuration.getBooleanOrNull("enabled"));
		assertNull(configuration.getLongOrNull("server.missing"));
		assertNull(configuration.getIntegerOrNull("server.missing"));
		assertNull(configuration.getFloatOrNull("server.missing"));
		assertNull(configuration.getBooleanOrNull("server.missing"));
	}

	@Test
	void fullyQualifiedKeysRejectInvalidBareKeyCharacters()
	{
		assertThrows(IllegalArgumentException.class, () -> TomlUtilities.fullyQualifiedKeyToArray("server/path"));
	}

	@Test
	void fullyQualifiedKeysRejectEmptySegments()
	{
		assertThrows(IllegalArgumentException.class, () -> TomlUtilities.fullyQualifiedKeyToArray(""));
		assertThrows(IllegalArgumentException.class, () -> TomlUtilities.fullyQualifiedKeyToArray("   "));
		assertThrows(IllegalArgumentException.class, () -> TomlUtilities.fullyQualifiedKeyToArray(".server"));
		assertThrows(IllegalArgumentException.class, () -> TomlUtilities.fullyQualifiedKeyToArray("server."));
		assertThrows(IllegalArgumentException.class, () -> TomlUtilities.fullyQualifiedKeyToArray("server..host"));
	}

	@Test
	void fullyQualifiedKeysSupportEscapedUnicodeCodepoints()
	{
		String emoji = new String(Character.toChars(0x1F600));

		assertArrayEquals(
			new String[] { "emoji" + emoji },
			TomlUtilities.fullyQualifiedKeyToArray("\"emoji\\U0001F600\"")
		);
	}

	private TomlConfiguration configuration()
	{
		Map<String, Object> server = new LinkedHashMap<>();
		server.put("host", "localhost");
		server.put("port", 8080L);

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("enabled", true);
		data.put("ratio", 1.5f);
		data.put("server", server);

		return new TomlConfiguration(data);
	}
}
