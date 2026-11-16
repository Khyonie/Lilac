/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.Deque;
import java.util.Objects;

import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolParsers;
import coffee.khyonieheart.lilac.util.IntStack;

/**
 * Represents a text document, with a pointer position to read character-by-character.
 * Has a utility to pin and rewind the pointer to the most recently pinned position.
 */
public class Document
{
	private String document;
	private int pointer = 0;
	//private Deque<Integer> pins = new ArrayDeque<>();
	private IntStack pins = new IntStack(8);

	// Character constants
	private static final Character SPACE = ' ';
	private static final Character TAB = '\t';

	/**
	 * Creates a document cursor over the given source text.
	 *
	 * @param document TOML source text.
	 */
	public Document(
		String document
	) {
		this.document = Objects.requireNonNull(document);

		if (this.document.endsWith("\n"))
		{
			this.document += '\n';
		}
	}

	/**
	 * Gets the source text backing this document.
	 *
	 * @return Source text backing this document.
	 */
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

	/**
	 * Moves the pointer backward by one character.
	 *
	 * @return This document.
	 * @throws IllegalStateException Thrown if the pointer would move before the beginning of the document.
	 */
	public Document hold()
	{
		if (pointer == 0)
		{
			throw new IllegalStateException("Cannot decrement pointer into negatives");
		}
		pointer--;

		return this;
	}

	/**
	 * Moves the pointer backward by a given number of characters.
	 *
	 * @param delta Number of characters to move backward.
	 *
	 * @return This document.
	 * @throws IllegalStateException Thrown if the pointer would move before the beginning of the document.
	 */
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

	/**
	 * Gets the most recently pinned pointer position without removing it.
	 *
	 * @return The most recently pinned pointer position.
	 */
	public int getPin()
	{
		return this.pins.peek();
	}

	/**
	 * Gets the number of active pointer pins.
	 *
	 * @return Current pin stack depth.
	 */
	public int getPinDepth()
	{
		return this.pins.size();
	}

	/**
	 * Discards pins created after a saved stack depth, without moving the pointer.
	 * Parsing methods use this in a finally block to release their remaining pins
	 * on success, failure, or exception while preserving their caller's pins.
	 *
	 * @param depth Pin depth saved on entry to the parsing method.
	 * @throws IllegalArgumentException If depth is negative.
	 * @throws IllegalStateException If pins below the saved depth were removed.
	 */
	public void discardPinsAfter(int depth)
	{
		if (depth < 0)
		{
			throw new IllegalArgumentException("Pin depth cannot be negative");
		}
		if (this.pins.size() < depth)
		{
			throw new IllegalStateException("Parser removed its caller's pins");
		}
		while (this.pins.size() > depth)
		{
			this.pins.pop();
		}
	}

	/**
	 * Moves the pointer back to the most recently pinned position and removes that pin.
	 *
	 * @throws IllegalStateException Thrown if no pinned positions are available.
	 */
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

	/**
	 * Gets the character at the current pointer position.
	 *
	 * @return Character at the current pointer position.
	 */
	public char getCharAtPointer()
	{
		return this.document.charAt(pointer);
	}

	/**
	 * Gets the character at the current pointer position, then increments the pointer.
	 *
	 * @return Character at the current pointer position before incrementing.
	 */
	public char getCharAndIncrement()
	{
		return this.document.charAt(pointer++);
	}

	public boolean matchsSubstringAtPointer(String substring)
	{
		return this.document.startsWith(substring, this.pointer);
	}

	/**
	 * Gets the length of the source text backing this document.
	 *
	 * @return Source text length.
	 */
	public int getDocumentLength()
	{
		return this.document.length();
	}

	/**
	 * Checks whether the pointer is still inside the source text.
	 *
	 * @return {@code true} if another character can be read, {@code false} otherwise.
	 */
	public boolean hasNext()
	{
		return this.pointer < this.document.length();
	}

	/**
	 * Skips to the next non-whitespace character. Automatically adds newline symbols to the symbols queue.
	 *
	 * @param symbols Symbol stack to receive parsed newline symbols.
	 * @param decoder Decoder whose parser context and warning settings are used while scanning.
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
			if (SymbolParsers.NEWLINE.tryParse(this, decoder, symbols) != null)
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
	 *
	 * @return Whether or not the end of the document was reached.
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
