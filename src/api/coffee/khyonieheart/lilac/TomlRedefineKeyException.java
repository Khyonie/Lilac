/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

/**
 * Exception thrown when TOML source text defines the same key more than once.
 */
public class TomlRedefineKeyException extends RuntimeException
{
	/**
	 * Creates a key redefinition exception.
	 *
	 * @param message Error message.
	 */
	public TomlRedefineKeyException(
		String message
	) {
		super(message);
	}
}
