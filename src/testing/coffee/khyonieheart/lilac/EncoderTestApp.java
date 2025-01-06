package coffee.khyonieheart.lilac;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

public class EncoderTestApp
{
	public static void main(
		String[] args
	)
		throws Exception
	{
		TomlDecoder decoder = new LilacDecoder(TomlVersion.V1_1_0);
		TomlEncoder encoder = new LilacEncoder().setAlignValues(true);

		String testDocument = Files.readString(new File("test_document.toml").toPath());

		System.out.println("##### V Input document V #####");
		System.out.println(testDocument);
		System.out.println();
		Map<String, Object> decoded = decoder.decode(testDocument);

		System.out.println("##### V Output V #####");
		String encoded = encoder.encode(decoded);

		System.out.println(encoded);
	}
}
