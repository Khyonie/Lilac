/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

public class TomlRedefineKeyException extends RuntimeException
{
	public TomlRedefineKeyException(
		String message
	) {
		super(message);
	}
}
