package coffee.khyonieheart.lilac;

public interface TomlDecoder
{
	public TomlConfiguration decode(
		String document
	)
		throws TomlSyntaxException;
}
