package coffee.khyonieheart.lilac.value;

import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.api.StringType;

public class TomlString implements Commentable, TomlObject<String>, CharSequence
{
	private String string = null;
	private String comment;
	private StringType type = StringType.BASIC;
	private int newlines = 0;

	public TomlString(
		String string
	) {
		this.string = string.replace("\\\\", "\\")
			.replace("\\n", "\n");
	}

	public TomlString(
		String string,
		StringType type
	) {
		this(string);
		this.type = type;
	}

	public String get()
	{
		return this.string;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.STRING;
	}

	@Override
	public String serialize() 
	{
		return this.type.getStartAndEnd() + this.string + this.type.getStartAndEnd();
	}

	@Override
	public int length() 
	{
		return this.string.length();
	}

	@Override
	public char charAt(
		int index
	) {
		return this.string.charAt(index);
	}

	@Override
	public CharSequence subSequence(
		int start, 
		int end
	) {
		return this.string.subSequence(start, end);
	}

	@Override
	public String getComment() 
	{
		return this.comment;
	}

	@Override
	public void setComment(
		String comment
	) {
		this.comment = comment;
	}

	@Override
	public int getNumberOfTrailingNewlines() 
	{
		return this.newlines;
	}

	@Override
	public void incrementTrailingNewlines() 
	{
		this.newlines++;
	}
}
