package coffee.khyonieheart.lilac;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomlKeys
{
	private static Pattern normalKey = Pattern.compile("([A-Za-z0-9_-]+)");
	private static Pattern quotedKey = Pattern.compile("([\"'])((?:\\\\1|.)*?)(\\1)");

	public static String[] extractKeys(
		String key
	) {
		ArrayList<String> extractedKeys = new ArrayList<>();

		int offset = 0;
		Matcher normalKeyMatcher = normalKey.matcher(key);
		Matcher quotedKeyMatcher = quotedKey.matcher(key);

		boolean foundTopLevelKey = false;
		if (normalKeyMatcher.find(offset))
		{
			if (normalKeyMatcher.start() == offset)
			{
				extractedKeys.add(normalKeyMatcher.group(0));
				foundTopLevelKey = true;
				offset += normalKeyMatcher.group().length();
			}
		}

		if (!foundTopLevelKey)
		{
			if (quotedKeyMatcher.find(offset))
			{
				if (quotedKeyMatcher.start() == offset)
				{
					extractedKeys.add(quotedKeyMatcher.group(2));
					foundTopLevelKey = true;
					offset += quotedKeyMatcher.group().length();
				}
			}
		}

		if (!foundTopLevelKey)
		{
			throw new IllegalArgumentException("Could not extract top-level key from input \"" + key + "\". To use an empty top level key, use the literal \\\"\\\" (two escaped quotes).");
		}

		while (offset < key.length())
		{
			if (!Character.isWhitespace(key.charAt(offset)))
			{
				break;
			}

			offset++;
		}

		while (offset < key.length())
		{
			if (Character.isWhitespace(key.charAt(offset)))
			{
				offset++;
				continue;
			}

			// Consume dot
			if (key.charAt(offset) != '.')
			{
				throw new IllegalArgumentException("Expected a \".\" to pair keys together at position " + offset);
			}

			offset++;
			// Consume whitespace
			while (offset < key.length())
			{
				if (!Character.isWhitespace(key.charAt(offset)))
				{
					break;
				}

				offset++;
			}

			// Consume key
			if (normalKeyMatcher.find(offset))
			{
				if (normalKeyMatcher.start() == offset)
				{
					extractedKeys.add(normalKeyMatcher.group(0));
					offset += normalKeyMatcher.group().length();

					continue;
				}
			}

			if (quotedKeyMatcher.find(offset))
			{
				if (quotedKeyMatcher.start() == offset)
				{
					extractedKeys.add(quotedKeyMatcher.group(2));
					offset += quotedKeyMatcher.group().length();

					continue;
				}
			}

			throw new IllegalArgumentException("Could not find a key after dot at position " + offset + ". To represent an empty key, use a \\\"\\\" literal.");
		}

		String[] keys = new String[extractedKeys.size()];
		return extractedKeys.toArray(keys);
	}
}
