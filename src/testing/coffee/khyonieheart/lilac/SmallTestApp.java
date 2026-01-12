package coffee.khyonieheart.lilac;

public class SmallTestApp
{
	private static final String[] TEST_KEYS = {
		"key",
		"bare_key",
		"bare-key",
		"_underscore",
		"a1b2c3",
		"true",
		"false",
		"has spaces",
		"\"has.dots.inside\"",
		"\"127.0.0.1\"",
		"\"quote \" inside\"",
		"\"tab\tand\nnewline\"",
		"\"\u03C0\"",
		"'no escapes here \n \t'",
		"'contains \"double quotes\"'",
		"'path C:\\Users\\Hailey'",
		"a.b.c",
		"site.\"google.com\".status",
		"servers.'eu-west-1'.ip",
		"\"root-key\".child.\"leaf-key\""
	};
	public static void main(String[] args) throws Exception
	{
		for (String testKey : TEST_KEYS)
		{
			System.out.println("Key: " + testKey);
			String[] output = TomlUtilities.fullyQualifiedKeyToArray(testKey);
			for (String subkey : output)
			{
				System.out.println("- " + subkey);
			}
		}
	}
}
