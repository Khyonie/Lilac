/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.BiConsumer;

public interface TomlDecoder
{
	public Map<String, Object> decode(
		Document document
	);

	public default Map<String, Object> decode(
		String document
	) {
		return this.decode(new Document(document));
	}

	public default Map<String, Object> decode(
		File file
	) throws 
		IOException
	{
		return this.decode(Files.readString(file.toPath()));
	}

	public void addContext(
		ParserContext context
	);

	public ParserContext getContext();

	public void removeContext();

	public TomlDecoder setWarningHandler(
		BiConsumer<TomlWarning, String> handler
	);

	public void sendWarning(
		TomlWarning warning,
		String message
	);

	public TomlDecoder disableWarnings(
		TomlWarning... warnings
	);

	public TomlDecoder enableWarnings(
		TomlWarning... warnings
	);

	public boolean isWarningEnabled(
		TomlWarning warningType
	);

	public TomlDecoder setTomlVersion(
		TomlVersion version
	);

	public TomlVersion getVersion();
}
