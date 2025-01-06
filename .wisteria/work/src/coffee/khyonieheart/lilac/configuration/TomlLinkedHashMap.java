package coffee.khyonieheart.lilac.configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TomlLinkedHashMap extends LinkedHashMap<String, Object>
{
	private Map<Map<String, Object>, TableTypeContext> tableTypes;
	private Map<List<Object>, ArrayTypeContext> arrayTypes;

	public void setContext(
		Map<Map<String, Object>, TableTypeContext> tableTypes,
		Map<List<Object>, ArrayTypeContext> arrayTypes
	) {
		this.tableTypes = Objects.requireNonNull(tableTypes);
		this.arrayTypes = Objects.requireNonNull(arrayTypes);
	}

	public TableTypeContext getTableType(
		Map<String, Object> table
	) {
		return this.tableTypes.get(table);
	}

	public ArrayTypeContext getArrayType(
		List<Object> array
	) {
		return this.arrayTypes.get(array);
	}
}
