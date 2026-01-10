/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.configuration;

public enum ArrayTypeContext
{
	/** Array is a regular array. */
	REGULAR,
	/** Array is an array of tables. */
	ARRAY_OF_TABLES,
	;
}
