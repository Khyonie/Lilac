package coffee.khyonieheart.lilac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import coffee.khyonieheart.lilac.value.TomlInlineTable;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlTable;

public class TomlConfiguration
{
	private Map<String, TomlObject<?>> configuration;

	public TomlConfiguration(
		Map<String, TomlObject<?>> configuration
	) {
		this.configuration = Objects.requireNonNull(configuration);
	}

	public Map<String, TomlObject<?>> getBacking()
	{
		return this.configuration;
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

		String[] keys = TomlKeys.extractKeys(key);

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

	public boolean hasKey(
		String key
	) {
		return hasKeys(TomlKeys.extractKeys(key));
	}

	public boolean hasKeys(
		String... keys
	) {
		Objects.requireNonNull(keys);

		if (keys.length == 0)
		{
			throw new IllegalStateException("At least one key must be given");
		}

		Map<String, TomlObject<?>> targetTable = this.configuration;
		String key;
		Iterator<String> keyIter = Arrays.asList(keys).iterator();
		while (keyIter.hasNext())
		{
			key = keyIter.next();

			if (!targetTable.containsKey(key))
			{
				return false;
			}

			switch (targetTable.get(key).getType())
			{
				case TABLE -> {
					targetTable = ((TomlTable) targetTable.get(key)).get();
					continue;
				}
				case INLINE_TABLE -> {
					targetTable = ((TomlTable) targetTable.get(key)).get();
					continue;
				}
				default -> { return !keyIter.hasNext(); }
			}
		}

		return true;
	}

	// Utility
	//-------------------------------------------------------------------------------- 

	public void set(
		String key,
		TomlObject<?> object
	) {
		this.set(object, TomlKeys.extractKeys(key));
	}

	public void set(
		TomlObject<?> object,
		String... keys
	) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(keys);

		if (keys.length == 0)
		{
			throw new IllegalArgumentException("At least one key must be given");
		}

		Map<String, TomlObject<?>> targetTable = this.configuration;
		List<String> parents = new ArrayList<>();


		for (int i = 0; i < keys.length; i++)
		{
			String key = keys[i];
			
			if (i + 1 < keys.length)
			{
				parents.add(key);
				if (!targetTable.containsKey(key))
				{
					targetTable.put(key, new TomlTable(new ArrayList<>(parents)));
				}

				targetTable = switch (targetTable.get(key).getType())
				{
					case TABLE -> ((TomlTable) targetTable.get(key)).get();
					case INLINE_TABLE -> ((TomlInlineTable) targetTable.get(key)).get();
					default -> throw new IllegalStateException("Unexpected type " + targetTable.get(key).getType().name() + " for key " + key + ", expected a TABLE or INLINE_TABLE");
				};
				continue;
			}

			targetTable.put(key, object);
		}
	}
}
