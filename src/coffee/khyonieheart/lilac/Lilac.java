package coffee.khyonieheart.lilac;

import coffee.khyonieheart.lilac.api.LilacTomlBuilder;

/**
 * Root class for the Lilac TOML library.
 *
 * @author Khyonie
 */
public class Lilac
{
	// Hide no-args constructor
	private Lilac() {}

	/**
	 * Creates a new TOML builder.
	 *
	 * @return A new TOML builder to configure
	 */
	public static TomlBuilder newBuilder()
	{
		return new LilacTomlBuilder();
	}

/*private static String testDocument = """
[table]
basic_key = "A value!"
"quoted.key" = 3.14

# I'm a full-line comment, don't mind me
commented = 0 # And I'm an inline comment

"" = 0 # Discouraged but possible
dotted.key = 5 
dotted."quoted".key = 10
whitespace  .  key = 15

# Strings 
[strings]
basic_string = "Basic string"
multiline_string = \"""
	I 
	am a multiline 
	string\"""
literal_string = 'C:\\Users\\'
literal_multiline_string = '''"Something something"'''

# Integers
[integers]
decimal_integer = 10
positive_decimal_integer = +15
negative_decimal_integer = -15

hex_integer = 0xCAFE
octal_integer = 0o755
binary_integer = 0b1000_0001

# Typed integers
binary_byte: byte = 0b1001_0110
hex_short: short = 0xFFFF

# Floats/doubles
[floats]
simple_float = 1.0
positive_float = +5.0
negative_float = -10.0

typed_double: double = 10.0

exponential_float = 5e+22

mixed_float = 3.0e-10

# Boolean
[booleans]
bool_true = true 
bool_false = false
""";

	public static void main(String[] args)
	{
		TomlBuilder builder = newBuilder().setPreserveComments(true).setStoreJavaTypes(true);
		Map<String, TomlObject<?>> data = builder.serializeObject(new TestObject());
		System.out.println("Serialized object");
		printToml(data, 0);

		System.out.println(builder.toTomlFromTable(data));
	}

	static void printToml(
		Map<String, TomlObject<?>> table,
		int depth
	) {
		String key;
		TomlObject<?> value;
		for (Entry<String, TomlObject<?>> entry : table.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();

			if (value.getType() == TomlObjectType.TABLE)
			{
				System.out.println(" -".repeat(depth) + " [" + key + "]");
				printToml(((TomlTable) value).get(), depth + 1);
				continue;
			}

			System.out.println(" -".repeat(depth) + " " + key + "(" + value.getType().name() + "): " + value.serialize());
		}
	}
*/
}
