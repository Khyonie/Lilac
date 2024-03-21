package coffee.khyonieheart.lilac.api;

public enum NumberBase
{
	BINARY(2),
	OCTAL(8),
	DECIMAL(10),
	HEXADECIMAL(16)
	;

	private final int radix;

	private NumberBase(
		int radix
	) {
		this.radix = radix;
	}

	public int getRadix()
	{
		return this.radix;
	}
}
