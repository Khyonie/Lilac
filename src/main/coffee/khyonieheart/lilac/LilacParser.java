/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolType;

class LilacParser
{
	private final Document document;
	private final Deque<Symbol<?>> symbols;
	private final Deque<ParserContext> context = new ArrayDeque<>();
	private final Deque<Map<String, Object>> openMap = new ArrayDeque<>();
	private final Deque<List<Object>> openArray = new ArrayDeque<>();
	private final Deque<Deque<String>> openKeySet = new ArrayDeque<>();
	private final TomlTableBuilder tables = new TomlTableBuilder();

	private boolean newlineRequired = false;
	private Symbol<?> symbolStarting = null;

	LilacParser(
		Document document,
		Deque<Symbol<?>> originalSymbols
	) {
		this.document = document;
		this.symbols = filterSymbols(originalSymbols);
		this.context.push(ParserContext.ROOT);
		this.openMap.push(tables.createRootMap());
	}

	Map<String, Object> parse()
	{
		while (!symbols.isEmpty())
		{
			switch (context.peek())
			{
				case ROOT -> parseRootSymbol(symbols.pop());
				case INLINE_TABLE -> parseInlineTableSymbol(symbols.pop());
				case ARRAY -> parseArraySymbol(symbols.pop());
			}
		}

		switch (context.peek())
		{
			case ROOT -> {}
			case ARRAY -> throw TomlSyntaxException.of("Unterminated array", document);
			case INLINE_TABLE -> throw TomlSyntaxException.of("Unterminated table", document);
		}

		return openMap.removeLast();
	}

	private void parseRootSymbol(
		Symbol<?> symbol
	) {
		switch (symbol.getType())
		{
			case KEY -> readKeySet(symbol);
			case VALUE -> putValue(symbol);
			case ARRAY_START -> startArray();
			case INLINE_TABLE_START -> startInlineTable();
			case TABLE_ARRAY_START -> readTableKeys(SymbolType.TABLE_ARRAY_END);
			case TABLE_ARRAY_END -> openArrayTable();
			case TABLE_START -> {
				symbolStarting = symbol;
				readTableKeys(SymbolType.TABLE_END);
			}
			case TABLE_END -> openTable();
			case COMMENT -> {}
			case NEWLINE -> newlineRequired = false;
			default -> throw new IllegalStateException("Invalid type \"" + symbol.getType().name() + "\" in root context");
		}
	}

	private void parseInlineTableSymbol(
		Symbol<?> symbol
	) {
		switch (symbol.getType())
		{
			case KEY -> readKeySet(symbol);
			case VALUE -> putValue(symbol);
			case INLINE_TABLE_START -> startInlineTable();
			case INLINE_TABLE_END -> finishInlineTable(symbol);
			case ARRAY_START -> startArray();
			case NEWLINE -> {}
			case ARRAY_SEPARATOR -> newlineRequired = false;
			default -> throw new IllegalStateException("Invalid type \"" + symbol.getType().name() + "\" in inline table context");
		}
	}

	private void parseArraySymbol(
		Symbol<?> symbol
	) {
		switch (symbol.getType())
		{
			case VALUE -> openArray.peek().add(symbol.getValue());
			case INLINE_TABLE_START -> startInlineTable();
			case ARRAY_START -> startArray();
			case ARRAY_END -> finishArray(symbol);
			case NEWLINE -> {}
			case ARRAY_SEPARATOR -> newlineRequired = false;
			default -> throw new IllegalStateException("Invalid type \"" + symbol.getType().name() + "\" in array context");
		}
	}

	private void readKeySet(
		Symbol<?> symbol
	) {
		if (newlineRequired)
		{
			throw TomlSyntaxException.of("Key \"" + symbol.getValue() + "\" must have a newline or start of document preceeding it", document.getDocument(), symbol);
		}

		openKeySet.push(new ArrayDeque<>());
		openKeySet.peek().push((String) symbol.getValue());
		while (symbols.peek().getType() == SymbolType.KEY)
		{
			openKeySet.peek().push((String) symbols.pop().getValue());
		}
	}

	private void readTableKeys(
		SymbolType endType
	) {
		openKeySet.push(new ArrayDeque<>());
		while (symbols.peek().getType() != endType)
		{
			switch (symbols.peek().getType())
			{
				case KEY -> {}
				case KEY_SEPARATOR -> {}
				default -> throw TomlSyntaxException.of("Unexpected symbol \"" + symbols.peek().getValue() + "\"", document.getDocument(), symbols.peek());
			}
			openKeySet.peek().push((String) symbols.pop().getValue());
		}
	}

	private void putValue(
		Symbol<?> symbol
	) {
		newlineRequired = true;
		try {
			tables.putValue(openMap.peek(), openKeySet.pop(), symbol.getValue());
		} catch (TomlRedefineKeyException e) {
			throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbol, e);
		}
	}

	private void openTable()
	{
		if (openMap.size() > 1)
		{
			openMap.pop();
		}

		Deque<String> keys = openKeySet.pop();
		Map<String, Object> topMap = new LinkedHashMap<>();
		try {
			Map<String, Object> existing = tables.putTable(openMap.peek(), keys, topMap);
			openMap.push(existing == null ? topMap : existing);
		} catch (TomlRedefineKeyException e) {
			throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbolStarting, e);
		}

		newlineRequired = true;
	}

	private void openArrayTable()
	{
		Deque<String> keys = openKeySet.pop();
		Map<String, Object> topMap = new LinkedHashMap<>();

		while (openMap.size() > 1)
		{
			openMap.pop();
		}

		if (!openArray.isEmpty())
		{
			openArray.pop();
		}

		openArray.push(tables.getOrPutArray(openMap.peek(), keys));
		openArray.peek().add(topMap);
		openMap.push(topMap);
		newlineRequired = true;
	}

	private void startInlineTable()
	{
		openMap.push(new LinkedHashMap<>());
		openKeySet.push(new ArrayDeque<>());
		context.push(ParserContext.INLINE_TABLE);
	}

	private void finishInlineTable(
		Symbol<?> symbol
	) {
		Map<String, Object> table = openMap.pop();
		openKeySet.pop();
		context.pop();

		switch (context.peek())
		{
			case ARRAY -> openArray.peek().add(table);
			default -> {
				try {
					tables.putInlineTable(openMap.peek(), openKeySet.pop(), table);
				} catch (TomlRedefineKeyException e) {
					throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbol, e);
				}
			}
		}

		newlineRequired = true;
	}

	private void startArray()
	{
		openArray.push(new ArrayList<>());
		context.push(ParserContext.ARRAY);
	}

	private void finishArray(
		Symbol<?> symbol
	) {
		List<Object> array = openArray.pop();
		context.pop();

		switch (context.peek())
		{
			case ARRAY -> openArray.peek().add(array);
			default -> {
				try {
					tables.putValue(openMap.peek(), openKeySet.pop(), array);
				} catch (TomlRedefineKeyException e) {
					throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbol, e);
				}
			}
		}

		newlineRequired = true;
	}

	private static Deque<Symbol<?>> filterSymbols(
		Deque<Symbol<?>> originalSymbols
	) {
		Deque<Symbol<?>> filtered = new ArrayDeque<>();
		Iterator<Symbol<?>> originalSymbolsIterator = originalSymbols.descendingIterator();
		while (originalSymbolsIterator.hasNext())
		{
			Symbol<?> candidate = originalSymbolsIterator.next();
			switch (candidate.getType())
			{
				case KEY_SEPARATOR -> {}
				case COMMENT -> {}
				case EQUALS -> {}
				case DOCUMENT_START -> {}
				case DOCUMENT_END -> {}
				default -> filtered.addLast(candidate);
			}
		}

		return filtered;
	}
}
