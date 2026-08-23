package coffee.khyonieheart.lilac;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

final class TomlTestExpectations
{
	private static final Gson GSON = new Gson();

	private TomlTestExpectations()
	{}

	@SuppressWarnings("unchecked")
	static Map<String, Object> load(
		String test
	) throws IOException {
		String json = Files.readString(Path.of("tests", test + ".json"));
		return (Map<String, Object>) normalize(GSON.fromJson(json, Map.class));
	}

	@SuppressWarnings("unchecked")
	private static Object normalize(
		Object value
	) {
		if (value instanceof List)
		{
			List<Object> normalized = new ArrayList<>();
			for (Object element : (List<Object>) value)
			{
				normalized.add(normalize(element));
			}
			return normalized;
		}

		if (value instanceof Map)
		{
			Map<String, Object> map = (Map<String, Object>) value;
			if (map.containsKey("type") && map.containsKey("value"))
			{
				return typedValue((String) map.get("type"), (String) map.get("value"));
			}

			Map<String, Object> normalized = new LinkedHashMap<>();
			for (Map.Entry<String, Object> entry : map.entrySet())
			{
				normalized.put(entry.getKey(), normalize(entry.getValue()));
			}
			return normalized;
		}

		return value;
	}

	private static Object typedValue(
		String type,
		String value
	) {
		return switch (type)
		{
			case "string" -> value;
			case "integer" -> Long.parseLong(value);
			case "float" -> floatValue(value);
			case "bool" -> Boolean.parseBoolean(value);
			case "datetime" -> OffsetDateTime.parse(value);
			case "datetime-local" -> LocalDateTime.parse(value);
			case "date-local" -> LocalDate.parse(value);
			case "time-local" -> LocalTime.parse(value);
			default -> throw new IllegalArgumentException("Unknown toml-test value type \"" + type + "\"");
		};
	}

	private static Float floatValue(
		String value
	) {
		return switch (value)
		{
			case "inf", "+inf" -> Float.POSITIVE_INFINITY;
			case "-inf" -> Float.NEGATIVE_INFINITY;
			case "nan", "+nan", "-nan" -> Float.NaN;
			default -> Float.parseFloat(value);
		};
	}
}
