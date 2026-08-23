/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class TomlValueEncoder
{
	private final TomlEncoderSettings settings;

	TomlValueEncoder(
		TomlEncoderSettings settings
	) {
		this.settings = settings;
	}

	/** Branch method which takes any TOML-spec type and calls the appropriate encoder method. */
	@SuppressWarnings("unchecked")
	String encode(
		Map<String, Object> rootData,
		Object object,
		int tabDepth
	) {
		if (object == null)
		{
			throw new IllegalArgumentException("TOML does not support null values");
		}

		if (object instanceof Map)
		{
			return encodeTableInline(rootData, (Map<String, Object>) object, tabDepth + 1);
		}

		if (object instanceof List)
		{
			return encodeArray(rootData, (List<Object>) object, tabDepth + 1);
		}

		if (object instanceof String)
		{
			return TomlStringEncoder.encode((String) object);
		}

		if (object instanceof Float)
		{
			return TomlNumberEncoder.encode((float) object);
		}

		if (object instanceof Double)
		{
			return TomlNumberEncoder.encode((double) object);
		}

		if (object instanceof OffsetDateTime)
		{
			return TomlTemporalEncoder.encode((OffsetDateTime) object);
		}

		if (object instanceof LocalDateTime)
		{
			return TomlTemporalEncoder.encode((LocalDateTime) object);
		}

		if (object instanceof LocalDate)
		{
			return TomlTemporalEncoder.encode((LocalDate) object);
		}

		if (object instanceof LocalTime)
		{
			return TomlTemporalEncoder.encode((LocalTime) object);
		}

		return object.toString();
	}

	private String encodeTableInline(
		Map<String, Object> rootData,
		Map<String, Object> table,
		int tabDepth
	) {
		StringBuilder builder = new StringBuilder();
		builder.append('{');

		Iterator<Entry<String, Object>> entryIter = table.entrySet().iterator();
		while (entryIter.hasNext())
		{
			Entry<String, Object> entry = entryIter.next();
			builder.append(TomlKeyEncoder.sanitizeKey(entry.getKey()))
				.append(" = ")
				.append(encode(rootData, entry.getValue(), tabDepth + 1));

			if (entryIter.hasNext())
			{
				builder.append(", ");
			}
		}

		builder.append('}');

		return builder.toString();
	}

	private String encodeArray(
		Map<String, Object> rootData,
		List<Object> array,
		int tabDepth
	) {
		StringBuilder builder = new StringBuilder();
		builder.append('[');

		if (settings.breakArrays && !array.isEmpty())
		{
			builder.append('\n')
				.append("\t".repeat(tabDepth));
		}

		Iterator<Object> iter = array.iterator();
		while (iter.hasNext())
		{
			builder.append(encode(rootData, iter.next(), tabDepth));

			if (iter.hasNext())
			{
				builder.append(", ");
				if (settings.breakArrays)
				{
					builder.append('\n')
						.append("\t".repeat(tabDepth));
				}
			}
		}

		if (settings.breakArrays && !array.isEmpty())
		{
			builder.append('\n')
				.append("\t".repeat(tabDepth - 1));
		}

		builder.append(']');

		return builder.toString();
	}
}
