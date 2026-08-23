/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import coffee.khyonieheart.lilac.configuration.ArrayTypeContext;
import coffee.khyonieheart.lilac.configuration.TableTypeContext;
import coffee.khyonieheart.lilac.configuration.TomlLinkedHashMap;

public class LilacEncoder implements TomlEncoder
{
	private final TomlEncoderSettings settings = new TomlEncoderSettings();
	private final TomlValueEncoder values = new TomlValueEncoder(settings);

	@Override
	public String encode(
		Map<String, Object> data
	) {
		StringBuilder builder = new StringBuilder();
		encode(data, data, new ArrayDeque<>(), builder);

		return builder.toString();
	}

	private void encode(
		Map<String, Object> rootData,
		Map<String, Object> data,
		Deque<String> keys,
		StringBuilder builder
	) {
		int maxKeyLength = maxKeyLength(data);

		for (Map.Entry<String, Object> entry : data.entrySet())
		{
			if (!shouldEmitChild(rootData, entry.getValue()))
			{
				encodeValue(rootData, entry, maxKeyLength, builder);
			}
		}

		for (Map.Entry<String, Object> entry : data.entrySet())
		{
			if (shouldEmitChild(rootData, entry.getValue()))
			{
				encodeChild(rootData, entry, keys, builder);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void encodeChild(
		Map<String, Object> rootData,
		Map.Entry<String, Object> entry,
		Deque<String> keys,
		StringBuilder builder
	) {
		String key = entry.getKey();
		Object value = entry.getValue();
		keys.push(key);

		if (shouldEmitTable(rootData, value))
		{
			if (settings.newlineTables && builder.length() != 0)
			{
				builder.append('\n');
			}

			builder.append(TomlKeyEncoder.tableHeader(keys))
				.append('\n');
			encode(rootData, (Map<String, Object>) value, keys, builder);
		} else {
			encodeArrayOfTables(rootData, (List<Map<String, Object>>) value, keys, builder);
		}

		keys.pop();
	}

	private void encodeValue(
		Map<String, Object> rootData,
		Map.Entry<String, Object> entry,
		int maxKeyLength,
		StringBuilder builder
	) {
		String key = entry.getKey();
		String value = values.encode(rootData, entry.getValue(), 0);
		if (value == null)
		{
			return;
		}

		builder.append(TomlKeyEncoder.sanitizeKey(key));

		if (settings.alignEquals)
		{
			builder.append(" ".repeat(maxKeyLength - key.length()));
		}

		builder.append(" = ")
			.append(value)
			.append('\n');
	}

	@SuppressWarnings("unchecked")
	private boolean shouldEmitTable(
		Map<String, Object> rootData,
		Object value
	) {
		if (!(value instanceof Map))
		{
			return false;
		}

		if (!(rootData instanceof TomlLinkedHashMap))
		{
			return true;
		}

		return ((TomlLinkedHashMap) rootData).getTableType((Map<String, Object>) value) != TableTypeContext.INLINE;
	}

	@SuppressWarnings("unchecked")
	private boolean isArrayOfTables(
		Map<String, Object> rootData,
		Object value
	) {
		return value instanceof List
			&& rootData instanceof TomlLinkedHashMap
			&& ((TomlLinkedHashMap) rootData).getArrayType((List<Object>) value) == ArrayTypeContext.ARRAY_OF_TABLES;
	}

	private boolean shouldEmitChild(
		Map<String, Object> rootData,
		Object value
	) {
		return shouldEmitTable(rootData, value) || isArrayOfTables(rootData, value);
	}

	private void encodeArrayOfTables(
		Map<String, Object> rootData,
		List<Map<String, Object>> data,
		Deque<String> keys,
		StringBuilder builder
	) {
		for (Map<String, Object> table : data)
		{
			if (builder.length() != 0 && settings.newlineTables)
			{
				builder.append('\n');
			}

			builder.append(TomlKeyEncoder.arrayOfTablesHeader(keys))
				.append('\n');

			encode(rootData, table, keys, builder);
		}
	}

	private int maxKeyLength(
		Map<String, Object> data
	) {
		if (!settings.alignEquals)
		{
			return 0;
		}

		int maxKeyLength = 0;
		for (String key : data.keySet())
		{
			maxKeyLength = Math.max(maxKeyLength, key.length());
		}

		return maxKeyLength;
	}

	//
	// Settings
	//
	
	public LilacEncoder setBreakArrays(
		boolean breakArrays
	) {
		this.settings.breakArrays = breakArrays;
		return this;
	}

	public LilacEncoder setAlignValues(
		boolean alignValues
	) {
		this.settings.alignEquals = alignValues;
		return this;
	}

	public LilacEncoder setAddNewlineBeforeTables(
		boolean addNewline
	) {
		this.settings.newlineTables = addNewline;
		return this;
	}

	public LilacEncoder setSkipNonTomlTypes(
		boolean skipNonTomlTypes
	) {
		this.settings.skipNonTomlObjects = skipNonTomlTypes;
		return this;
	}
}
