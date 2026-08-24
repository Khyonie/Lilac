package coffee.khyonieheart.lilac;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LilacDecoderTest
{
	@Test
	void decodesFromPath(
		@TempDir Path directory
	) throws IOException
	{
		Path config = directory.resolve("config.toml");
		Files.writeString(config, "name = \"Lilac\"\n");

		Map<String, Object> data = new LilacDecoder(TomlVersion.V1_1_0).decode(config);

		assertEquals("Lilac", data.get("name"));
	}

	@Test
	void decodesConfigurationFromStringAndPath(
		@TempDir Path directory
	) throws IOException
	{
		TomlDecoder decoder = new LilacDecoder(TomlVersion.V1_1_0);
		TomlConfiguration fromString = decoder.decodeConfiguration("[server]\nport = 8080\n");

		Path config = directory.resolve("config.toml");
		Files.writeString(config, "enabled = true\n");
		TomlConfiguration fromPath = decoder.decodeConfiguration(config);

		assertEquals(8080L, fromString.getLong("server.port"));
		assertEquals(true, fromPath.getBoolean("enabled"));
	}
}
