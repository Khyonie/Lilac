package coffee.khyonieheart.lilac.testing;

import java.util.ArrayDeque;
import java.util.Deque;

import coffee.khyonieheart.lilac.Lilac;
import coffee.khyonieheart.lilac.TomlBuilder;
import coffee.khyonieheart.lilac.TomlSyntaxException;

public class LilacTestApp
{

	// Keys
	//-------------------------------------------------------------------------------- 
private static String keyTest = """
# Test for all key types
key = "value"
commented_key = "value"
dotted.key = "value"
"quoted"."".dotted.key = "value"
'C:\\LiteralKey' = "value"
simple_key  .  with   .  whitespace = "value"
""";

private static String commentTest = """
# Comment test
key = "value" # Inline comment
""";

private static String tableTest = """
sample_key = "value"
#sample_key.value = "value" # Uncomment to error

[discrete_table]
sample_key = 100
inline_table = { value = 10 }
nested_table = { nested = { nested_key = 10 } } # This should resolve to an indiscrete table and an inline table
indiscrete_table.value = 10
""";

private static String scalarTest = """
simple_int = 1
prefixed_int = +2
negative_int = -4

int_with_underscore = "8_16" # Resolves to 816

hex_integer = 0xFF
bin_integer = 0b1111_0000
oct_integer = 0o755
""";

private static String floatTest = """
simple_float = 3.1415
prefixed_float = +10.5
negative_float = -21.5

exponential_float = 10e10 
positive_exponential_float = 10e+10 
negative_exponential_float = 5e-10

mixed_float = 32.5e+6

infinity = inf 
negative_infinity = -inf 

not_a_number = nan
""";

	public static void main(String[] args)
	{
		TomlBuilder toml = Lilac.newBuilder()
			.setStoreJavaTypes(true)
			.setUppercaseHexadecimal(true)
			.setPreserveComments(true);

		Deque<String> passedTests = new ArrayDeque<>();
		try {
			toml.toTomlFromTable(toml.parseString(keyTest));
			passedTests.push("Keys");

			toml.toTomlFromTable(toml.parseString(commentTest));
			passedTests.push("Comments");

			toml.toTomlFromTable(toml.parseString(tableTest));
			passedTests.push("Tables");

			toml.toTomlFromTable(toml.parseString(scalarTest));
			passedTests.push("Integers");

			toml.toTomlFromTable(toml.parseString(floatTest));
			passedTests.push("Float");
		} catch (TomlSyntaxException e) {
			e.printStackTrace();
		}

		if (passedTests.size() < 5)
		{
			System.out.println("[ FAIL ] Passed " + passedTests.size() + "/5 tests");
			return;
		}

		System.out.println("[ PASS ] Passed all tests");
	}
}
