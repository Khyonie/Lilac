package coffee.khyonieheart.lilac.value;

public interface TomlObject<T>
{
	public T get();

	public TomlObjectType getType();

	public String serialize();

	public int getNumberOfTrailingNewlines();
	public void incrementTrailingNewlines();
}
