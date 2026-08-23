/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

final class TomlNumberEncoder
{
	private TomlNumberEncoder()
	{}

	static String encode(
		float f32
	) {
		if (f32 == Float.POSITIVE_INFINITY)
		{
			return "+inf";
		}

		if (f32 == Float.NEGATIVE_INFINITY)
		{
			return "-inf";
		}

		if (Float.isNaN(f32))
		{
			return "nan";
		}

		return Float.toString(f32);
	}

	static String encode(
		double f64
	) {
		if (f64 == Double.POSITIVE_INFINITY)
		{
			return "+inf";
		}

		if (f64 == Double.NEGATIVE_INFINITY)
		{
			return "-inf";
		}

		if (Double.isNaN(f64))
		{
			return "nan";
		}

		return Double.toString(f64);
	}
}
