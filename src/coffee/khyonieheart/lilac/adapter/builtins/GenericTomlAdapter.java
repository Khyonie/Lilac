package coffee.khyonieheart.lilac.adapter.builtins;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import coffee.khyonieheart.lilac.TomlParser;
import coffee.khyonieheart.lilac.api.LilacExpose;
import coffee.khyonieheart.lilac.value.TomlArray;
import coffee.khyonieheart.lilac.value.TomlInlineTable;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlString;
import coffee.khyonieheart.lilac.value.TomlTable;

public class GenericTomlAdapter
{ 
	/**
	 * @implNote If a plain Map is the argument, the resulting TomlTable object will need to have its parents set
	 */
	public TomlObject<?> toToml(
		Object object
	) {
		if (object.getClass().isPrimitive())
		{
			return PrimitiveTomlAdapter.toToml(object);
		}

		// TODO Rewrite this with switch instanceof pattern matching
		if (object instanceof String str)
		{
			return new TomlString(str);
		}

		if (object.getClass().isArray())
		{
			List<TomlObject<?>> list = new ArrayList<>();
			for (int i = 0; i < Array.getLength(object); i++)
			{
				Object element = Array.get(object, i);
				list.add(toToml(element));
			}

			return new TomlArray(list);
		}

		if (object instanceof Collection c && object instanceof Iterable) // Iterable so we filter out Map
		{
			return toTomlArray(c);
		}

		if (object instanceof Map map)
		{
			Map<String, TomlObject<?>> data = new LinkedHashMap<>();

			for (Object key : map.keySet())
			{
				data.put(key.toString(), toToml(map.get(key)));
			}

			return new TomlTable(new ArrayList<>()).rebase(data);
		}

		// TODO Handle date/time types

		return toInlineTable(object);
	}

	public <C extends Iterable<?>> TomlArray toTomlArray(
		C object
	) {
		List<TomlObject<?>> list = new ArrayList<>();
		for (Object obj : object)
		{
			list.add(toToml(obj));
		}

		return new TomlArray(list);
	}

	private TomlInlineTable toInlineTable(
		Object object
	) {
		Map<String, TomlObject<?>> data = new LinkedHashMap<>();

		for (Field f : object.getClass().getFields())
		{
			if (!f.isAnnotationPresent(LilacExpose.class))
			{
				continue;
			}

			if (!f.getAnnotation(LilacExpose.class).serialize())
			{
				continue;
			}

			f.setAccessible(true);

			try {
				if (f.get(object) == null)
				{
					continue;
				}

				data.put(f.getName(), toToml(f.get(object)));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		for (Field f : object.getClass().getDeclaredFields())
		{
			if (!f.isAnnotationPresent(LilacExpose.class))
			{
				continue;
			}

			if (!f.getAnnotation(LilacExpose.class).serialize())
			{
				continue;
			}

			f.setAccessible(true);

			try {
				if (f.get(object) == null)
				{
					continue;
				}

				data.put(f.getName(), toToml(f.get(object)));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return new TomlInlineTable(data);
	}

	public <T> T fromToml(
		Map<String, TomlObject<?>> tomlData,
		Class<T> type,
		TomlParser parser
	)
		throws NoSuchMethodException,
			InvocationTargetException,
			IllegalAccessException,
			InstantiationException
	{
		T object;
		try {
			Constructor<T> constructor = type.getConstructor();
			constructor.setAccessible(true);

			object = constructor.newInstance();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Type " + type.getName() + " must implicitly or explicitly define a no-args constructor", e);
		} 

		for (Field field : type.getFields())
		{
			if (!tomlData.containsKey(field.getName()))
			{
				continue;
			}

			if (!field.isAnnotationPresent(LilacExpose.class))
			{
				continue;
			}

			if (!field.getAnnotation(LilacExpose.class).deserialize())
			{
				continue;
			}

			field.set(object, switch (tomlData.get(field.getName()).getType())
			{
				case ARRAY -> {
					List<TomlObject<?>> listData = ((TomlArray) tomlData.get(field.getName())).get();

					if (field.getType().isArray())
					{
						if (field.getType().getComponentType().isPrimitive())
						{
							yield copyListIntoPrimitiveArray(field.getType().getComponentType(), listData.size(), listData);
						}

						Object[] array = new Object[listData.size()];
						for (int i = 0; i < listData.size(); i++)
						{
							array[i] = listData.get(i).get();
						}

						yield array;
					}

					if (listData.size() == 0)
					{
						yield new ArrayList<>();
					}
					
					Object[] array = new Object[listData.size()];
					for (int i = 0; i < listData.size(); i++)
					{
						array[i] = listData.get(i).get();
					}

					yield array;
				}
				default -> tomlData.get(field.getName()).get();
			});
		}

		return object;
	}

	private Object copyListIntoPrimitiveArray(
		Class<?> type,
		int length,
		List<TomlObject<?>> data
	) {
		Object array = Array.newInstance(type, length);

		for (int i = 0; i < data.size(); i++)
		{
			Array.set(array, i, data.get(i).get());
		}

		return array;
	}
}
