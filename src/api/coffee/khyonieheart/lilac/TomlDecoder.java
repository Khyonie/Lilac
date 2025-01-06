/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public interface TomlDecoder
{
	/**
	 * Reads a TOML document into an ordered map.
	 *
	 * @param document Document to read
	 *
	 * @return A map representing the given document.
	 * @throws TomlSyntaxException Thrown if the document is invalid.
	 * @throws TomlRedefineKeyException Thrown if document contains any duplicate keys.
	 */
	public Map<String, Object> decode(
		Document document
	);

	/**
	 * Reads a TOML document into an ordered map.
	 *
	 * @param document Document to read
	 *
	 * @return A map representing the given document.
	 * @throws TomlSyntaxException Thrown if the document is invalid.
	 * @throws TomlRedefineKeyException Thrown if document contains any duplicate keys.
	 */
	public default Map<String, Object> decode(
		String document
	) {
		Objects.requireNonNull(document);
		return this.decode(new Document(document));
	}

	/**
	 * Loads the contents of the given file as a TOML document and reads it into an ordered map.
	 *
	 * @param file A file containing a TOML document
	 *
	 * @return A map representing the given document.
	 * @throws TomlSyntaxException Thrown if the document is invalid.
	 * @throws TomlRedefineKeyException Thrown if document contains any duplicate keys.
	 * @throws IOException Thrown if file cannot be read.
	 */
	public default Map<String, Object> decode(
		File file
	) throws 
		IOException
	{
		Objects.requireNonNull(file);
		return this.decode(Files.readString(file.toPath()));
	}

	/**
	 * Adds to context when parsing a document.
	 *
	 * @param context Context to add
	 */
	public void addContext(
		ParserContext context
	);

	/**
	 * Gets the current context of the parser. May be {@code null} if inside the root context.
	 *
	 * @return Current parser context. May be null.
	 */
	public ParserContext getContext();

	/**
	 * Removes the most recent context from the parser.
	 */
	public void removeContext();

	/**
	 * Sets the warning handler. Set to {@code null} to disable warning handling.
	 *
	 * @param handler Warning handler. May be null.
	 *
	 * @return This decoder instance.
	 */
	public TomlDecoder setWarningHandler(
		BiConsumer<TomlWarning, String> handler
	);

	/**
	 * Sends a non-fatal warning about a TOML practice.
	 *
	 * @param warning Warning type
	 * @param message String message explaining the warning
	 */
	public void sendWarning(
		TomlWarning warning,
		String message
	);

	/**
	 * Disables the given warnings.
	 *
	 * @param warnings Warnings to disable
	 *
	 * @return This decoder instance.
	 */
	public TomlDecoder disableWarnings(
		TomlWarning... warnings
	);

	/**
	 * Enables the given warnings.
	 *
	 * @param warnings Warnings to enable
	 *
	 * @return This decoder instance.
	 */
	public TomlDecoder enableWarnings(
		TomlWarning... warnings
	);

	/**
	 * Gets if a warning is enabled or not.
	 *
	 * @param warningType Warning type
	 *
	 * @return Whether or not the given warning type is enabled
	 */
	public boolean isWarningEnabled(
		TomlWarning warningType
	);

	/**
	 * Sets the TOML language specification to follow.
	 *
	 * @param version TOML specification version
	 *
	 * @return This decoder instance.
	 */
	public TomlDecoder setTomlVersion(
		TomlVersion version
	);

	/**
	 * Gets the TOML specification this decoder is currently using.
	 *
	 * @return Current TOML specification being followed by this decoder.
	 */
	public TomlVersion getVersion();
}
