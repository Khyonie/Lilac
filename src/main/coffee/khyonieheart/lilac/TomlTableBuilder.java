/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import coffee.khyonieheart.lilac.configuration.ArrayTypeContext;
import coffee.khyonieheart.lilac.configuration.TableTypeContext;
import coffee.khyonieheart.lilac.configuration.TomlLinkedHashMap;

class TomlTableBuilder
{
	private final Map<List<Object>, ArrayTypeContext> arrayTypes = new IdentityHashMap<>();
	private final Map<Map<String, Object>, TableTypeContext> tableTypes = new IdentityHashMap<>();

	TomlLinkedHashMap createRootMap()
	{
		TomlLinkedHashMap rootMap = new TomlLinkedHashMap();
		rootMap.setContext(tableTypes, arrayTypes);
		return rootMap;
	}

	@SuppressWarnings("unchecked")
	List<Object> getOrPutArray(
		Map<String, Object> map,
		Deque<String> keys
	) {
		Deque<String> keysCopy = keysInOrder(keys);

		Map<String, Object> targetMap = map;
		String key;
		while (keysCopy.size() > 1)
		{
			key = keysCopy.pop();
			if (!targetMap.containsKey(key))
			{
				Map<String, Object> nextMap = new LinkedHashMap<>();
				targetMap.put(key, nextMap);
				targetMap = nextMap;
				tableTypes.put(nextMap, TableTypeContext.TABLE_IMPLICIT);
				continue;
			}

			if (targetMap.get(key) instanceof List)
			{
				Object target = getLast((List<Object>) targetMap.get(key));

				if (!(target instanceof Map))
				{
					throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as an array");
				}

				targetMap = (Map<String, Object>) target;
				continue;
			}

			if (!(targetMap.get(key) instanceof Map))
			{
				throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as a table");
			}

			targetMap = (Map<String, Object>) targetMap.get(key);
		}

		key = keysCopy.pop();
		if (targetMap.containsKey(key))
		{
			if (targetMap.get(key) instanceof List)
			{
				if (arrayTypes.get((List<Object>) targetMap.get(key)) != ArrayTypeContext.ARRAY_OF_TABLES)
				{
					throw new TomlRedefineKeyException("Cannot extend regular array \"" + key + "\" as an array of tables");
				}
				return (List<Object>) targetMap.get(key);
			}

			throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as an array");
		}

		List<Object> array = new ArrayList<>();
		targetMap.put(key, array);
		arrayTypes.put(array, ArrayTypeContext.ARRAY_OF_TABLES);
		return array;
	}

	Map<String, Object> putTable(
		Map<String, Object> root,
		Deque<String> keys,
		Map<String, Object> value
	) {
		return put(root, KeyPlaceContext.TABLE, TableTypeContext.EXPLICIT, keys, value);
	}

	void putValue(
		Map<String, Object> root,
		Deque<String> keys,
		Object value
	) {
		put(root, KeyPlaceContext.VALUE, TableTypeContext.KEY_VALUE_IMPLICIT, keys, value);
	}

	void putInlineTable(
		Map<String, Object> root,
		Deque<String> keys,
		Map<String, Object> value
	) {
		put(root, KeyPlaceContext.VALUE, TableTypeContext.INLINE, keys, value);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> put(
		Map<String, Object> root,
		KeyPlaceContext keyContext,
		TableTypeContext tableContext,
		Deque<String> keys,
		Object value
	) {
		Deque<String> keysCopy = keysInOrder(keys);

		Map<String, Object> targetMap = root;
		TableTypeContext previousTableContext = null;
		String key;
		while (keysCopy.size() > 1)
		{
			key = keysCopy.pop();
			if (!targetMap.containsKey(key))
			{
				Map<String, Object> nextMap = new LinkedHashMap<>();
				targetMap.put(key, nextMap);
				targetMap = nextMap;

				tableTypes.put(nextMap, tableContext == TableTypeContext.EXPLICIT ? TableTypeContext.TABLE_IMPLICIT : TableTypeContext.KEY_VALUE_IMPLICIT);
				previousTableContext = tableTypes.get(nextMap);
				continue;
			}

			if (targetMap.get(key) instanceof List)
			{
				Object target = getLast((List<Object>) targetMap.get(key));

				if (!(target instanceof Map))
				{
					throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as an array");
				}

				if (!arrayTypes.containsKey(targetMap.get(key)) || arrayTypes.get(targetMap.get(key)) != ArrayTypeContext.ARRAY_OF_TABLES)
				{
					throw new TomlRedefineKeyException("Cannot extend static table \"" + key + "\"");
				}

				targetMap = (Map<String, Object>) target;

				if (keyContext == KeyPlaceContext.VALUE)
				{
					throw new TomlRedefineKeyException("Cannot redefine existing array of tables \"" + key + "\" outside of an array-of-tables definition");
				}
				continue;
			}

			if (!(targetMap.get(key) instanceof Map))
			{
				throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as a table");
			}

			if (tableTypes.get((Map<String, Object>) targetMap.get(key)) == TableTypeContext.INLINE)
			{
				throw new TomlRedefineKeyException("Cannot extend inline table \"" + key + "\"");
			}

			if (tableContext == TableTypeContext.KEY_VALUE_IMPLICIT && previousTableContext == TableTypeContext.TABLE_IMPLICIT && tableTypes.get(targetMap.get(key)) == TableTypeContext.EXPLICIT)
			{
				throw new TomlRedefineKeyException("Dotted keys cannot insert into already defined explicit tables, see https://github.com/toml-lang/toml/issues/846");
			}

			previousTableContext = tableTypes.get(targetMap.get(key));
			targetMap = (Map<String, Object>) targetMap.get(key);
		}

		key = keysCopy.pop();
		if (targetMap.containsKey(key))
		{
			return putExistingValue(targetMap, tableContext, key, keys, value);
		}

		if (value instanceof Map)
		{
			tableTypes.put((Map<String, Object>) value, tableContext);
		}

		if (value instanceof List)
		{
			arrayTypes.put((List<Object>) value, ArrayTypeContext.REGULAR);
		}
		targetMap.put(key, value);
		return null;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> putExistingValue(
		Map<String, Object> targetMap,
		TableTypeContext tableContext,
		String key,
		Deque<String> keys,
		Object value
	) {
		if (targetMap.get(key) instanceof Map && value instanceof Map)
		{
			Map<String, Object> target = (Map<String, Object>) targetMap.get(key);
			if (tableTypes.get(target) == TableTypeContext.TABLE_IMPLICIT && tableContext != TableTypeContext.EXPLICIT)
			{
				throw new TomlRedefineKeyException("Duplicate table \"" + keysToString(keys) + "\"");
			}

			if (tableTypes.get(target) == TableTypeContext.KEY_VALUE_IMPLICIT && tableContext == TableTypeContext.EXPLICIT)
			{
				throw new TomlRedefineKeyException("Cannot extend an implicitly created table \"" + key + "\" with an explicit table definition");
			}

			if (tableTypes.get(target) == TableTypeContext.EXPLICIT && tableContext == TableTypeContext.EXPLICIT)
			{
				throw new TomlRedefineKeyException("Duplicate explicit table \"" + key + "\"");
			}

			if (tableContext == TableTypeContext.INLINE)
			{
				throw new TomlRedefineKeyException("Cannot redefine implicitly defined table \"" + key + "\" as an inline table");
			}

			tableTypes.put((Map<String, Object>) targetMap.get(key), TableTypeContext.EXPLICIT);
			return (Map<String, Object>) targetMap.get(key);
		}

		throw new TomlRedefineKeyException("Cannot redefine existing key \"" + key + "\" with value of type " + targetMap.get(key).getClass().getName() + " as value with type " + value.getClass().getName());
	}

	private static String keysToString(
		Deque<String> keys
	) {
		StringBuilder builder = new StringBuilder();
		Deque<String> keysCopy = keysInOrder(keys);

		while (!keysCopy.isEmpty())
		{
			builder.append(keysCopy.pop());

			if (!keysCopy.isEmpty())
			{
				builder.append('.');
			}
		}

		return builder.toString();
	}

	private static Deque<String> keysInOrder(
		Deque<String> keys
	) {
		Deque<String> orderedKeys = new ArrayDeque<>();
		Iterator<String> keyIterator = keys.descendingIterator();
		while (keyIterator.hasNext())
		{
			orderedKeys.addLast(keyIterator.next());
		}

		return orderedKeys;
	}

	private static <T> T getLast(
		List<T> values
	) {
		return values.get(values.size() - 1);
	}

	private static enum KeyPlaceContext
	{
		TABLE,
		VALUE
		;
	}
}
