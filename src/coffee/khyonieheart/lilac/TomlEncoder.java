package coffee.khyonieheart.lilac;

public interface TomlEncoder
{
	public String encode(
		TomlConfiguration configuration,
		TomlParser parser
	);
}
