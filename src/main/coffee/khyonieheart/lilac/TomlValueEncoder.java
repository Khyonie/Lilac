/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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
			return skipOrThrow("TOML does not support null values");
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

		if (object instanceof Boolean)
		{
			return object.toString();
		}

		if (object instanceof Byte || object instanceof Short || object instanceof Integer || object instanceof Long)
		{
			return TomlNumberEncoder.encode(((Number) object).longValue());
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

		return skipOrThrow("Object of type " + object.getClass().getName() + " cannot be encoded as TOML");
	}

	private String encodeTableInline(
		Map<String, Object> rootData,
		Map<String, Object> table,
		int tabDepth
	) {
		StringBuilder builder = new StringBuilder();
		builder.append('{');

		for (Map.Entry<String, Object> entry : table.entrySet())
		{
			String value = encode(rootData, entry.getValue(), tabDepth + 1);
			if (value == null)
			{
				continue;
			}

			if (builder.length() > 1)
			{
				builder.append(", ");
			}

			builder.append(TomlKeyEncoder.sanitizeKey(entry.getKey()))
				.append(" = ")
				.append(value);
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

		boolean wroteValue = false;
		for (Object element : array)
		{
			String value = encode(rootData, element, tabDepth);
			if (value == null)
			{
				continue;
			}

			if (wroteValue)
			{
				builder.append(", ");
				if (settings.breakArrays)
				{
					builder.append('\n')
						.append("\t".repeat(tabDepth));
				}
			} else if (settings.breakArrays) {
				builder.append('\n')
					.append("\t".repeat(tabDepth));
			}

			builder.append(value);
			wroteValue = true;
		}

		if (settings.breakArrays && wroteValue)
		{
			builder.append('\n')
				.append("\t".repeat(tabDepth - 1));
		}

		builder.append(']');

		return builder.toString();
	}

	private String skipOrThrow(
		String message
	) {
		if (settings.skipNonTomlObjects)
		{
			return null;
		}

		throw new IllegalArgumentException(message);
	}
}
