/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayList;
import java.util.List;

import coffee.khyonieheart.lilac.symbol.Symbol;

/**
 * Exception thrown when TOML source text cannot be parsed.
 */
public class TomlSyntaxException extends RuntimeException
{
	/**
	 * Creates a syntax exception at the current document pointer.
	 *
	 * @param message Syntax error message.
	 * @param document Document being parsed.
	 *
	 * @return A syntax exception with source context.
	 */
	public static TomlSyntaxException of(
		String message,
		Document document
	) {
		return of(message, document.getDocument(), document.getPointer());
	}

	/**
	 * Creates a syntax exception at a parsed symbol's source position.
	 *
	 * @param message Syntax error message.
	 * @param document Source text being parsed.
	 * @param symbol Symbol whose source position should be reported.
	 *
	 * @return A syntax exception with source context.
	 */
	public static TomlSyntaxException of(
		String message,
		String document,
		Symbol<?> symbol
	) {
		return of(message, document, symbol.getPosition());
	}

	/**
	 * Creates a syntax exception with a cause at the current document pointer.
	 *
	 * @param message Syntax error message.
	 * @param document Document being parsed.
	 * @param cause Underlying cause.
	 *
	 * @return A syntax exception with source context.
	 */
	public static TomlSyntaxException of(
		String message,
		Document document,
		Throwable cause
	) {
		return of(message, document.getDocument(), document.getPointer(), cause);
	}

	/**
	 * Creates a syntax exception with a cause at a parsed symbol's source position.
	 *
	 * @param message Syntax error message.
	 * @param document Source text being parsed.
	 * @param symbol Symbol whose source position should be reported.
	 * @param cause Underlying cause.
	 *
	 * @return A syntax exception with source context.
	 */
	public static TomlSyntaxException of(
		String message,
		String document,
		Symbol<?> symbol,
		Throwable cause
	) {
		return of(message, document, symbol.getPosition(), cause);
	}

	private static TomlSyntaxException of(
		String message,
		String document,
		int pointer
	) {
		// Count newlines
		int newlines = 1;

		int linePosition = 0;
		for (int i = 0; i < pointer; i++)
		{
			linePosition++;
			if (document.charAt(i) == '\n')
			{
				newlines++;
				linePosition = 1;
			}
		}

		message += " at line " + newlines + " position " + linePosition;
		return new TomlSyntaxException(message, document, newlines, linePosition);
	}

	private static TomlSyntaxException of(
		String message,
		String document,
		int pointer,
		Throwable cause
	) {
		// Count newlines
		int newlines = 1;

		int linePosition = 0;
		for (int i = 0; i < pointer; i++)
		{
			linePosition++;
			if (document.charAt(i) == '\n')
			{
				newlines++;
				linePosition = 1;
			}
		}

		message += " at line " + newlines + " position " + linePosition;
		return new TomlSyntaxException(message, document, newlines, linePosition, cause);
	}

	private TomlSyntaxException(
		String message,
		String document,
		int newline,
		int position
	) {
		super(generateMessage(message, document, newline, position));
	}

	private TomlSyntaxException(
		String message,
		String document,
		int newline,
		int position,
		Throwable cause
	) {
		super(generateMessage(message, document, newline, position), cause);
	}

	private static String generateMessage(
		String message,
		String documentText,
		int newline,
		int linePosition
	) {
		StringBuilder builder = new StringBuilder();

		builder.append(message).append("\n");
		List<String> documentTextList = new ArrayList<>();

		int line = 1;
		for (String s : documentText.split("\n"))
		{
			String prefix = (line == newline) ? "!" : " ";
			documentTextList.add(prefix + String.format("%1$4d", line++) + " ║ " + s + "↲");
		}

		if (newline >= documentTextList.size())
		{
			documentTextList.add("╴╴╴╴╴╴╫─" + "─".repeat(linePosition) + "╯ (Here)");
		} else {
			documentTextList.add(newline, "╴╴╴╴╴╴╫─" + "─".repeat(linePosition) + "╯ (Here)");
		}

		for (int i = Math.max(0, newline - 5); i < Math.min(documentTextList.size(), newline + 6); i++)
		{
			builder.append(documentTextList.get(i)).append("\n");
		}

		builder.append("══════╩══════════════════");

		return builder.toString();
	}
}
