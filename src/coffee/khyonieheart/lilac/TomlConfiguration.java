package coffee.khyonieheart.lilac;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import coffee.khyonieheart.lilac.value.TomlInlineTable;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlTable;

public class TomlConfiguration
{
	private Map<String, TomlObject<?>> configuration;
	private static Pattern normalKey = Pattern.compile("([A-Za-z0-9_-]+)");
	private static Pattern quotedKey = Pattern.compile("([\"'])((?:\\\\1|.)*?)(\\1)");

	public TomlConfiguration(
		Map<String, TomlObject<?>> configuration
	) {
		this.configuration = Objects.requireNonNull(configuration);
	}

	public TomlObject<?> getTomlObject(
		String... keys
	) {
		Objects.requireNonNull(keys);
		
		if (keys.length == 0)
		{
			throw new IllegalArgumentException("At least one key must be given");
		}

		Map<String, TomlObject<?>> targetConfiguration = this.configuration;

		for (int i = 0; i < keys.length; i++)
		{
			if (!targetConfiguration.containsKey(keys[i]))
			{
				return null;
			}

			if (i + 1 < keys.length)
			{
				targetConfiguration = switch (targetConfiguration.get(keys[i]).getType())
				{
					case TABLE -> ((TomlTable) targetConfiguration.get(keys[i])).get();
					case INLINE_TABLE -> ((TomlInlineTable) targetConfiguration.get(keys[i])).get();
					default -> throw new ClassCastException("Expected a table for key " + keys[i] + ", got an object of type " + targetConfiguration.get(keys[i]).getType().name());
				};

				continue;
			}

			return targetConfiguration.get(keys[i]);
		}

		return null;

	}

	/**
	 * Retrieves a value using the given Key identity. This method may return null if no such key exists.
	 * Multiple keys can be specified by concatenating them with a single dot. The rules for a key's format match that of the TOML spec.
	 *
	 * @param <T> T Type of object being retrieved
	 * @param key Key string
	 *
	 * @return The value stored in the configuration at the specified key. May be null
	 *
	 * @throws IllegalArgumentException If key is empty
	 * @throws ClassCastException If a value being traversed with the key is not a table or an inline table
	 * @throws ClassCastException If the value being retrieved is not of type T
	 */ 
	public <T> T get(
		String key,
		Class<T> type
	) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(type);

		if (key.length() == 0)
		{
			throw new IllegalArgumentException("Key must be at least 1 character");
		}

		String[] keys = extractKeys(key);

		TomlObject<?> obj = getTomlObject(keys);
		if (obj == null)
		{
			return null;
		}

		return type.cast(obj.get());
	}

	/**
	 * Retrieves a value using the given keys. This method may return null if no such key exists. 
	 * The advantage of this method is that keys don't need to be manually concatenated together with dots.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param <T> T Type of object being retrieved
	 * @param keys Keys, in order of encounter
	 *
	 * @return The value stored in the configuration at the specified key. May be null
	 *
	 * @throws IllegalArgumentException If no keys are specified
	 * @throws ClassCastException If a value being traversed with a key is not a table or an inline table
	 * @throws ClassCastException If the value being retrieved is not of type T
	 */ 
	public <T> T get(
		Class<T> type,
		String... keys
	) {
		TomlObject<?> obj = getTomlObject(keys);
		if (obj == null)
		{
			return null;
		}

		return type.cast(obj.get());
	}

	/**
	 * Retrieves a string at the given key. This method may return null if no such key exists.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The string stored in the configuration at the specified key. May be null
	 */
	public String getString(
		String key
	) {
		return get(key, String.class);
	}

	/**
	 * Retrieves a string with the given keys. This method may return null if no such key exists.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The string stored in the configuration at the specified key. May be null
	 */
	public String getString(
		String... keys
	) {
		return get(String.class, keys);
	}

	/**
	 * Retrieves a byte at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The byte stored in the configuration at the specified key
	 */
	public byte getByte(
		String key
	) {
		return get(key, Byte.class);
	}

	/**
	 * Retrieves a byte with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The byte stored in the configuration at the specified key.
	 */
	public byte getByte(
		String... keys
	) {
		return get(Byte.class, keys);
	}

	/**
	 * Retrieves a short at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The short stored in the configuration at the specified key
	 */
	public short getShort(
		String key
	) {
		return get(key, Short.class);
	}

	/**
	 * Retrieves a short with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The short stored in the configuration at the specified key.
	 */
	public short getShort(
		String... keys
	) {
		return get(Short.class, keys);
	}

	/**
	 * Retrieves an integer at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The integer stored in the configuration at the specified key
	 */
	public int getInt(
		String key
	) {
		return get(key, Integer.class);
	}

	/**
	 * Retrieves an integer with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The integer stored in the configuration at the specified key.
	 */
	public int getInt(
		String... keys
	) {
		return get(Integer.class, keys);
	}

	/**
	 * Retrieves a long at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The long stored in the configuration at the specified key
	 */
	public long getLong(
		String key
	) {
		return get(key, Long.class);
	}

	/**
	 * Retrieves a long with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The long stored in the configuration at the specified key.
	 */
	public long getLong(
		String... keys
	) {
		return get(Long.class, keys);
	}

	/**
	 * Retrieves a float at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The float stored in the configuration at the specified key
	 */
	public float getFloat(
		String key
	) {
		return get(key, Float.class);
	}

	/**
	 * Retrieves a float with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The float stored in the configuration at the specified key.
	 */
	public float getFloat(
		String... keys
	) {
		return get(Float.class, keys);
	}

	/**
	 * Retrieves a double at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The double stored in the configuration at the specified key
	 */
	public double getDouble(
		String key
	) {
		return get(key, Double.class);
	}

	/**
	 * Retrieves a double with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The double stored in the configuration at the specified key.
	 */
	public double getDouble(
		String... keys
	) {
		return get(Double.class, keys);
	}

	/**
	 * Retrieves a boolean at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The boolean stored in the configuration at the specified key
	 */
	public boolean getBoolean(
		String key
	) {
		return get(key, Boolean.class);
	}

	/**
	 * Retrieves a boolean with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The boolean stored in the configuration at the specified key.
	 */
	public boolean getBoolean(
		String... keys
	) {
		return get(Boolean.class, keys);
	}

	/**
	 * Retrieves an array at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 * @param componentType Type of object represented within the array
	 *
	 * @return The array stored in the configuration at the specified key
	 */
	public <T> T[] getArray(
		String key,
		Class<T> componentType
	) {
		throw new UnsupportedOperationException("Array serialization/deserialization is not supported yet");
	}

	/**
	 * Retrieves an array with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param componentType Type of object represented within the array
	 * @param keys Key array
	 *
	 * @return The array stored in the configuration at the specified key.
	 */
	public <T> T[] getArray(
		Class<T> componentType,
		String... keys
	) {
		throw new UnsupportedOperationException("Array serialization/deserialization is not supported yet");
	}

	/**
	 * Retrieves a list at the given key.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 * @param listElementType Type of object represented within the list
	 *
	 * @return The list stored in the configuration at the specified key
	 *
	 * @implNote {@link ArrayList}s are used internally. You must map the output of this method to change the type of list used.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(
		String key,
		Class<T> listElementType
	) {
		return get(key, List.class);
	}

	/**
	 * Retrieves a list with the given keys.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param listElementType Type of object represented within the list
	 * @param keys Key array
	 *
	 * @return The list stored in the configuration at the specified key.
	 *
	 * @implNote {@link ArrayList}s are used internally. You must map the output of this method to change the type of list used.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(
		Class<T> componentType,
		String... keys
	) {
		return get(List.class, keys);
	}

	/**
	 * Retrieves a TOML map at the given key. This map is directly representative of this configuration, and edits will affect the overall configuration.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param key Key
	 *
	 * @return The map stored in the configuration at the specified key
	 *
	 * @implNote {@link LinkedHashMap}s are used internally. You must map the output of this method to change the type of map used.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, TomlObject<?>> getTable(
		String key
	) {
		return get(key, Map.class);
	}

	/**
	 * Retrieves a TOML map with the given keys. This map is directly representative of this configuration, and edits will affect the overall configuration.
	 * The rules for a key's format match that of the TOML spec.
	 *
	 * @param keys Key array
	 *
	 * @return The map stored in the configuration at the specified key
	 *
	 * @implNote {@link LinkedHashMap}s are used internally. You must map the output of this method to change the type of map used.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, TomlObject<?>> getTable(
		String... keys
	) {
		return get(Map.class, keys);
	}

	// Utility
	//-------------------------------------------------------------------------------- 
	private String[] extractKeys(
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
