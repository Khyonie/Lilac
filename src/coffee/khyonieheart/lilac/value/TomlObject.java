package coffee.khyonieheart.lilac.value;

import coffee.khyonieheart.lilac.api.Commentable;

public interface TomlObject<T> extends Cloneable
{
	public T get();

	public TomlObjectType getType();

	public String serialize();

	public int getNumberOfTrailingNewlines();
	public void incrementTrailingNewlines();

	public default TomlObject<T> clone()
	{
		TomlObject<T> clone = this.clone();
		if (clone instanceof Commentable c)
		{
			c.setComment(((Commentable) this).getComment());
		}

		for (int i = 0; i < this.getNumberOfTrailingNewlines(); i++)
		{
			clone.incrementTrailingNewlines();
		}

		return clone;
	}
}
