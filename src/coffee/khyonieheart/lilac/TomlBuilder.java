package coffee.khyonieheart.lilac;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Objects;

import coffee.khyonieheart.lilac.value.TomlObject;

public interface TomlBuilder
{
	/**
	 * Parses a TOML file into a map of keys and values.
	 * 
	 * @param file File to parse
	 *
	 * @throws FileNotFoundException If the given file does not exist
	 * @throws TomlSyntaxException If the given file cannot be parsed due to a syntax error
	 */
	public Map<String, TomlObject<?>> parseDocument(
		File file
	)
		throws FileNotFoundException,
			TomlSyntaxException;

	/**
	 * Parses a TOML file at the given filepath into a map of keys and values.
	 *
	 * @param filepath Filepath to file to parse
	 *
	 * @throws FileNotFoundException If no file exists at the given path
	 * @throws TomlSyntaxException If the given file cannot be parsed due to a syntax error
	 */
	public default Map<String, TomlObject<?>> parseDocument(
		String filepath
	)
		throws FileNotFoundException,
			TomlSyntaxException
	{
		Objects.requireNonNull(filepath);

		File file = new File(filepath);
		if (!file.exists())
		{
			throw new FileNotFoundException();
		}

		return parseDocument(file);
	}

	/**
	 * Parses a String into a map of keys and values.
	 *
	 * @param string String to parse
	 *
	 * @throws TomlSyntaxException If the given String cannot be parsed due to a syntax error
	 */
	public Map<String, TomlObject<?>> parseString(
		String string
	)
		throws TomlSyntaxException;

	/**
	 * Wraps a configuration parsed from the given file in a {@link TomlConfiguration}.
	 *
	 * @param file File to parse
	 *
	 * @return A TomlConfiguration representing the overall configuration
	 *
	 * @throws FileNotFoundException If the given file does not exist
	 * @throws TomlSyntaxException If the given file cannot be parsed due to a syntax error
	 */
	public default TomlConfiguration parseIntoConfiguration(
		File file
	)
		throws FileNotFoundException,
			TomlSyntaxException 
	{
		return new TomlConfiguration(parseDocument(file));
	}

	/**
	 * Wraps a configuration parsed from the given file in a {@link TomlConfiguration}.
	 *
	 * @param filepath Filepath to file to parse
	 *
	 * @return A TomlConfiguration representing the overall configuration
	 *
	 * @throws FileNotFoundException If no file exists at the given path
	 * @throws TomlSyntaxException If the given file cannot be parsed due to a syntax error
	 */
	public default TomlConfiguration parseIntoConfiguration(
		String filepath
	)
		throws FileNotFoundException,
			TomlSyntaxException
	{
		return new TomlConfiguration(parseDocument(filepath));
	}

	/**
	 * Wraps a configuration parsed from the given string into a {@link TomlConfiguration}.
	 *
	 * @param string String to parse
	 *
	 * @return A TomlConfiguration representing the overall configuration
	 *
	 * @throws TomlSyntaxException If the given String cannot be parsed due to a syntax error
	 */
	public default TomlConfiguration parseStringIntoConfiguration(
		String configuration
	)
		throws FileNotFoundException,
			TomlSyntaxException
	{
		return new TomlConfiguration(parseString(configuration));
	}

	/**
	 * Serializes a TOML table structure into a string.
	 *
	 * @param data TOML table to be serialized
	 */
	public String toTomlFromTable(
		Map<String, TomlObject<?>> data
	);

	/**
	 * Serializes a Map into a TOML string.
	 *
	 * @param data Data to be serialized
	 *
	 * @return TOML serialized data
	 */
	public String toToml(
		Map<String, Object> data
	);

	/**
	 * Serializes an Object into a Map that TOML can serialize.
	 *
	 * @param object Object to be serialized
	 *
	 * @return Map representation of object
	 */
	public Map<String, TomlObject<?>> serializeObject(
		Object object
	);

	/**
	 * Sets whether or not this TOML builder will retain comments.
	 *
	 * @param setting Setting, true or false
	 *
	 * @return This TOML builder, so methods can be chained
	 */
	public TomlBuilder setPreserveComments(
		boolean setting
	);

	/**
	 * Gets whether or not this TOML builder will retain comments.
	 *
	 * @return Whether or not to retain comments
	 */
	public boolean getPreservesComments();

	/**
	 * Sets whether or not this TOML builder will store hexadecimal number literals in uppercase.
	 *
	 * @param setting Setting, true or false
	 *
	 * @return This TOML builder, so methods can be chained
	 */
	public TomlBuilder setUppercaseHexadecimal(
		boolean setting
	);

	/**
	 * Gets whether or not this TOML builder will store hexadecimal number literals in uppercase.
	 *
	 * @return Whether or not this TOML builder will store hexadecimal number literals in uppercase
	 */
	public boolean getUppercaseHexadecimal();

	public TomlBuilder setStoreJavaTypes(
		boolean setting
	);

	public boolean getStoreJavaTypes();
}
