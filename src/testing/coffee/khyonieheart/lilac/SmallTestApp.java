package coffee.khyonieheart.lilac;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

public class SmallTestApp
{
	public static void main(String[] args) throws Exception
	{
		String file = Files.readString(new File("./project.toml").toPath());

		TomlDecoder decoder = new LilacDecoder(TomlVersion.V1_1_0);
		Map<String, Object> data = decoder.decode(new Document(file));
		printMap(data, 0);
	}

	@SuppressWarnings("unchecked")
	private static void printMap(
		Map<String, Object> data,
		int depth
	) {
		for (String key : data.keySet())
		{
			Object value = data.get(key);
			if (value instanceof Map)
			{
				System.out.println("- ".repeat(depth) + key + ":");
				printMap((Map<String, Object>) value, depth + 1);
				continue;
			}

			System.out.println("- ".repeat(depth) + key + " = " + value);
		}
	}
}
