package coffee.khyonieheart.lilac;

import java.util.Map;

public interface TomlEncoder
{
	public String encode(
		Map<String, Object> data
	);
}
