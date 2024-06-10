package coffee.khyonieheart.lilac.adapter;

import java.util.Map;

import coffee.khyonieheart.lilac.value.TomlObject;

public interface TomlAdapter<T>
{
	public TomlObject<?> toToml(T object);

	public T fromToml(Map<String, TomlObject<?>> tomlData);
}
