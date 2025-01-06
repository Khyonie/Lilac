/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.TomlNewline;

/**
 * Represents a text document, with a pointer position to read character-by-character.
 * Has a utility to pin and rewind the pointer to the most recently pinned position.
 */
public class Document
{
	private String document;
	private int pointer = 0;
	private Deque<Integer> pins = new ArrayDeque<>();

	// Character constants
	private static final Character SPACE = ' ';
	private static final Character TAB = '\t';

	public Document(
		String document
	) {
		this.document = Objects.requireNonNull(document);

		if (this.document.endsWith("\n"))
		{
			this.document += '\n';
		}
	}

	public String getDocument()
	{
		return this.document;
	}

	/**
	 * Returns the current pointer position.
	 * 
	 * @return The current pointer position.
	 */
	public int getPointer()
	{
		return this.pointer;
	}

	/**
	 * Increments the pointer by one.
	 *
	 * @return This document.
	 */
	public Document incrementPointer()
	{
		pointer++;

		return this;
	}

	public Document hold()
	{
		if (pointer == 0)
		{
			throw new IllegalStateException("Cannot decrement pointer into negatives");
		}
		pointer--;

		return this;
	}

	public Document hold(
		int delta
	) {
		if (pointer - delta < 0)
		{
			throw new IllegalStateException("Cannot decrement pointer into negatives");
		}
		pointer -= delta;

		return this;
	}

	/**
	 * Sets the pin position to the current pointer position.
	 *
	 * @return This document.
	 */
	public Document pinPointer()
	{
		this.pins.push(pointer);

		return this;
	}

	public int getPin()
	{
		return this.pins.peek();
	}

	public void rewindToPin()
	{
		if (pins.isEmpty())
		{
			throw new IllegalStateException("No pin positions are available");
		}

		pointer = this.pins.pop();
	}

	/**
	 * Removes the most recent pin without setting the pointer position.
	 *
	 * @return The most recent pin
	 */
	public int removePin()
	{
		if (pins.isEmpty())
		{
			throw new IllegalStateException("No pin positions are available");
		}

		return this.pins.pop();
	}

	public char getCharAtPointer()
	{
		return this.document.charAt(pointer);
	}

	public char getCharAndIncrement()
	{
		return this.document.charAt(pointer++);
	}

	public int getDocumentLength()
	{
		return this.document.length();
	}

	public boolean hasNext()
	{
		return this.pointer < this.document.length();
	}

	/**
	 * Skips to the next non-whitespace character. Automatically adds newline symbols to the symbols queue.
	 *
	 * @return Whether or not the end of the document was reached.
	 */
	public boolean skipThroughWhitespace(
		Deque<Symbol<?>> symbols,
		TomlDecoder decoder
	) {
		Objects.requireNonNull(symbols);

		while (this.hasNext())
		{
			if (Symbol.getSymbol(TomlNewline.class).tryParse(this, decoder, symbols))
			{
				continue;
			}

			if (!Character.isWhitespace(this.getCharAtPointer()))
			{
				return false;
			}

			this.incrementPointer();
		}

		return true;
	}

	/**
	 * Skips to the next non-space/tab character.
	 */
	public boolean skipToNextImportant()
	{
		while (this.hasNext())
		{
			if (this.getCharAtPointer() == SPACE || this.getCharAtPointer() == TAB)
			{
				this.incrementPointer();
				continue;
			}

			return false;
		}

		return true;
	}
}
