/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import coffee.khyonieheart.lilac.configuration.ArrayTypeContext;
import coffee.khyonieheart.lilac.configuration.TableTypeContext;
import coffee.khyonieheart.lilac.configuration.TomlLinkedHashMap;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.TomlEndOfDocument;
import coffee.khyonieheart.lilac.symbol.TomlStartOfDocument;

public class LilacDecoder implements TomlDecoder
{
	private Deque<ParserContext> context = new ArrayDeque<>();
	private boolean[] warnings = new boolean[TomlWarning.values().length];
	private BiConsumer<TomlWarning, String> warningHandler = null;
	private TomlVersion version;

	public LilacDecoder(
		TomlVersion version
	) {
		this.version = Objects.requireNonNull(version);
		this.enableWarnings(TomlWarning.values());
	}

	@Override
	public Map<String, Object> decode(Document document) 
	{
		context.clear();

		// Lexical analysis
		Symbol<?> documentStart = new TomlStartOfDocument();
		Deque<Symbol<?>> symbols = new ArrayDeque<>();
		symbols.push(documentStart);

		if (!document.hasNext())
		{
			symbols.push(new TomlEndOfDocument());
		} else {
			documentStart.tryParseRecursive(document, this, symbols);
		}

		if (symbols.peek().getClass() != TomlEndOfDocument.class)
		{
			StringBuilder builder = new StringBuilder();
			Iterator<Class<? extends Symbol<?>>> iter = symbols.peek().getNextSymbols(this).iterator();
			while (iter.hasNext())
			{
				builder.append(Symbol.getSymbol(iter.next()).getClass().getSimpleName());

				if (iter.hasNext())
				{
					builder.append(", ");
				}
			}

			while (document.getPointer() >= document.getDocument().length())
			{
				document.hold();
			}

			throw TomlSyntaxException.of("Expected one of [ " + builder.toString() + " ], found \"" + document.getCharAtPointer() + "\"", document);
		}

		return parse(document, symbols);
	}

	private Map<String, Object> parse(
		Document document,
		Deque<Symbol<?>> originalSymbols
	) {
		// Parser setup
		Deque<ParserContext> context = new ArrayDeque<>();
		context.push(ParserContext.ROOT);
		Deque<Map<String, Object>> openMap = new ArrayDeque<>();

		TomlLinkedHashMap rootMap = new TomlLinkedHashMap();
		openMap.push(rootMap); // Set up root table

		Deque<List<Object>> openArray = new ArrayDeque<>();
		Deque<Deque<String>> openKeySet = new ArrayDeque<>();

		// Language rules
		Map<List<Object>, ArrayTypeContext> arrayTypes = new IdentityHashMap<>();
		Map<Map<String, Object>, TableTypeContext> tableTypes = new IdentityHashMap<>();

		rootMap.setContext(tableTypes, arrayTypes);
		
		// Filter unnecessary tokens
		Deque<Symbol<?>> symbols = new ArrayDeque<>(
			originalSymbols.stream()
			.filter((symbol) -> {
				return switch (symbol.getType()) {
					case KEY_SEPARATOR -> false;
					case COMMENT -> false;
					case EQUALS -> false;
					case DOCUMENT_START -> false;
					case DOCUMENT_END -> false;
					default -> true;
				};
			}
			).toList())
			.reversed();

		// Begin parsing
		boolean newlineRequired = false;
		Symbol<?> symbol = null;
		Symbol<?> symbolStarting = null; // Start symbol for when a symbol needs to process its own following (I.E tables)
		while (!symbols.isEmpty())
		{
			switch (context.peek())
			{
				//
				// Root context
				//
				case ROOT -> {
					symbol = symbols.pop();
					switch (symbol.getType())
					{
						// Take all keys and push them onto the stack
						case KEY -> {
							if (newlineRequired)
							{
								throw TomlSyntaxException.of("Key \"" + symbol.getValue() + "\" must have a newline or start of document preceeding it", document.getDocument(), symbol);
							}

							openKeySet.push(new ArrayDeque<>());
							openKeySet.peek().push((String) symbol.getValue());
							while (symbols.peek().getType() == SymbolType.KEY)
							{
								symbol = symbols.pop();
								openKeySet.peek().push((String) symbol.getValue());
							}
						}

						// Take value and put it in top map
						case VALUE -> {
							newlineRequired = true;
							try {
								put(openMap.peek(), tableTypes, arrayTypes, KeyPlaceContext.VALUE, TableTypeContext.KEY_VALUE_IMPLICIT, openKeySet.pop(), symbol.getValue());
							} catch (TomlRedefineKeyException e) {
								throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbol, e);
							}
						}

						// Set up new array
						case ARRAY_START -> {
							openArray.push(new ArrayList<>());
							context.push(ParserContext.ARRAY);
						}

						// Set up new table
						case INLINE_TABLE_START -> {
							openMap.push(new LinkedHashMap<>());
							openKeySet.push(new ArrayDeque<>());
							context.push(ParserContext.INLINE_TABLE);
						}

						// Set up new array of tables
						case TABLE_ARRAY_START -> {
							openKeySet.push(new ArrayDeque<>());
							while (symbols.peek().getType() != SymbolType.TABLE_ARRAY_END)
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

						// Attempt to create or target existing array
						case TABLE_ARRAY_END -> {
							Deque<String> keys = openKeySet.pop();
							Map<String, Object> topMap = new LinkedHashMap<>();

							// Strip down to root configuration
							while (openMap.size() > 1)
							{
								openMap.pop();
							}

							if (!openArray.isEmpty())
							{
								openArray.pop();
							}

							openArray.push(getOrPutArray(openMap.peek(), tableTypes, arrayTypes, keys));
							openArray.peek().add(topMap);
							openMap.push(topMap);
							newlineRequired = true;
						}

						// Set up new table keys
						case TABLE_START -> {
							openKeySet.push(new ArrayDeque<>());
							symbolStarting = symbol;
							while (symbols.peek().getType() != SymbolType.TABLE_END)
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

						// Take keys and set the top map to the new map
						case TABLE_END -> {
							if (openMap.size() > 1)
							{
								openMap.pop();
							}
							Deque<String> keys = openKeySet.pop();
							Map<String, Object> topMap = new LinkedHashMap<>();
							try {
								Map<String, Object> existing = put(openMap.peek(), tableTypes, arrayTypes, KeyPlaceContext.TABLE, TableTypeContext.EXPLICIT, keys, topMap);
								openMap.push(existing == null ? topMap : existing);
							} catch (TomlRedefineKeyException e) {
								throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbolStarting, e);
							}

							newlineRequired = true;
						}

						case COMMENT -> {

						}

						case NEWLINE -> newlineRequired = false;
						default -> throw new IllegalStateException("Invalid type \"" + symbol.getType().name() + "\" in root context");
					}
				}

				//
				// Inline table context
				//
				case INLINE_TABLE -> {
					symbol = symbols.pop();
					switch (symbol.getType())
					{
						// Take all keys and push them onto the stack
						case KEY -> {
							openKeySet.push(new ArrayDeque<>());
							openKeySet.peek().push((String) symbol.getValue());
							while (symbols.peek().getType() == SymbolType.KEY)
							{
								symbol = symbols.pop();
								openKeySet.peek().push((String) symbol.getValue());
							}
						}

						// Take value and put it in top map
						case VALUE -> {
							try {
								put(openMap.peek(), tableTypes, arrayTypes, KeyPlaceContext.VALUE, TableTypeContext.KEY_VALUE_IMPLICIT, openKeySet.pop(), symbol.getValue());
							} catch (TomlRedefineKeyException e) {
								throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbol, e);
							}
						}

						// Set up new table
						case INLINE_TABLE_START -> {
							openMap.push(new LinkedHashMap<>());
							openKeySet.push(new ArrayDeque<>());
							context.push(ParserContext.INLINE_TABLE);
						}

						// Take table and put it in target destination
						case INLINE_TABLE_END -> {
							Map<String, Object> table = openMap.pop();
							openKeySet.pop();
							context.pop();

							switch (context.peek())
							{
								case ARRAY -> openArray.peek().add(table);
								default -> {
									try {
										put(openMap.peek(), tableTypes, arrayTypes, KeyPlaceContext.VALUE, TableTypeContext.INLINE, openKeySet.pop(), table);
									} catch (TomlRedefineKeyException e) {
										throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbol, e);
									}
								}
							}
						}

						// Set up new array
						case ARRAY_START -> {
							openArray.push(new ArrayList<>());
							context.push(ParserContext.ARRAY);
						}
						case NEWLINE -> {}
						case ARRAY_SEPARATOR -> {}
						default -> throw new IllegalStateException("Invalid type \"" + symbol.getType().name() + "\" in inline table context");
					}
				}

				//
				// Array context
				//
				case ARRAY -> {
					symbol = symbols.pop();
					switch (symbol.getType())
					{
						// Take value and put it in top map
						case VALUE -> openArray.peek().add(symbol.getValue());

						// Set up new table
						case INLINE_TABLE_START -> {
							openMap.push(new LinkedHashMap<>());
							openKeySet.push(new ArrayDeque<>());
							context.push(ParserContext.INLINE_TABLE);
						}

						// Set up new array
						case ARRAY_START -> {
							openArray.push(new ArrayList<>());
							context.push(ParserContext.ARRAY);
						}

						// Take array and put it in target destination
						case ARRAY_END -> {
							List<Object> array = openArray.pop();
							context.pop();

							switch (context.peek())
							{
								case ARRAY -> openArray.peek().add(array);
								default -> {
									try {
										put(openMap.peek(), tableTypes, arrayTypes, KeyPlaceContext.VALUE, TableTypeContext.KEY_VALUE_IMPLICIT, openKeySet.pop(), array);
									} catch (TomlRedefineKeyException e) {
										throw TomlSyntaxException.of(e.getMessage(), document.getDocument(), symbol, e);
									}
								}
							}
						}
						case NEWLINE -> {}
						case ARRAY_SEPARATOR -> {}
						default -> throw new IllegalStateException("Invalid type \"" + symbol.getType().name() + "\" in array context");
					}
				}
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

	private static String keysToString(
		Deque<String> keys
	) {
		StringBuilder builder = new StringBuilder();
		Deque<String> keysCopy = new ArrayDeque<>(keys).reversed();

		while (!keysCopy.isEmpty())
		{
			builder.append(keysCopy.pop());

			if (!keysCopy.isEmpty())
			{
				builder.append('.');
			}
		}

		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	private static List<Object> getOrPutArray(
		Map<String, Object> map,
		Map<Map<String, Object>, TableTypeContext> tableTypes,
		Map<List<Object>, ArrayTypeContext> arrayTypes,
		Deque<String> keys
	) {
		Deque<String> keysCopy = new ArrayDeque<>(keys).reversed();

		Map<String, Object> targetMap = map;
		String key;
		while (keysCopy.size() > 1)
		{
			key = keysCopy.pop();
			if (!targetMap.containsKey(key))
			{
				Map<String, Object> nextMap = new LinkedHashMap<>();
				targetMap.put(key, nextMap);
				targetMap = nextMap;
				tableTypes.put(nextMap, TableTypeContext.TABLE_IMPLICIT);
				continue;
			}

			// Target array of tables
			if (targetMap.get(key) instanceof List)
			{
				Object target = ((List<Object>) targetMap.get(key)).getLast();

				if (!(target instanceof Map))
				{
					throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as an array");
				}

				targetMap = (Map<String, Object>) target;
				continue;
			}

			if (!(targetMap.get(key) instanceof Map))
			{
				throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as a table");
			}

			targetMap = (Map<String, Object>) targetMap.get(key);
		}

		key = keysCopy.pop();
		if (targetMap.containsKey(key))
		{
			if (targetMap.get(key) instanceof List)
			{
				if (arrayTypes.get((List<Object>) targetMap.get(key)) != ArrayTypeContext.ARRAY_OF_TABLES)
				{
					throw new TomlRedefineKeyException("Cannot extend regular array \"" + key + "\" as an array of tables");
				}
				return (List<Object>) targetMap.get(key);
			}

			throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as an array");
		}

		List<Object> array = new ArrayList<>();
		targetMap.put(key, array);
		arrayTypes.put(array, ArrayTypeContext.ARRAY_OF_TABLES);
		return array;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> put(
		Map<String, Object> root,
		Map<Map<String, Object>, TableTypeContext> tableTypes,
		Map<List<Object>, ArrayTypeContext> arrayTypes,
		KeyPlaceContext keyContext,
		TableTypeContext tableContext,
		Deque<String> keys,
		Object value
	) {
		Deque<String> keysCopy = new ArrayDeque<>(keys).reversed();

		Map<String, Object> targetMap = root;
		TableTypeContext previousTableContext = null;
		String key;
		while (keysCopy.size() > 1)
		{
			key = keysCopy.pop();
			if (!targetMap.containsKey(key))
			{
				Map<String, Object> nextMap = new LinkedHashMap<>();
				targetMap.put(key, nextMap);
				targetMap = nextMap;

				tableTypes.put(nextMap, tableContext == TableTypeContext.EXPLICIT ? TableTypeContext.TABLE_IMPLICIT : TableTypeContext.KEY_VALUE_IMPLICIT);
				previousTableContext = tableTypes.get(nextMap);
				continue;
			}

			// Target array of tables
			if (targetMap.get(key) instanceof List)
			{
				Object target = ((List<Object>) targetMap.get(key)).getLast();

				if (!(target instanceof Map))
				{
					throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as an array");
				}

				if (!arrayTypes.containsKey(targetMap.get(key)) || arrayTypes.get(targetMap.get(key)) != ArrayTypeContext.ARRAY_OF_TABLES)
				{
					throw new TomlRedefineKeyException("Cannot extend static table \"" + key + "\"");
				}

				targetMap = (Map<String, Object>) target;

				if (keyContext == KeyPlaceContext.VALUE)
				{
					throw new TomlRedefineKeyException("Cannot redefine existing array of tables \"" + key + "\" outside of an array-of-tables definition");
				}
				continue;
			}

			if (!(targetMap.get(key) instanceof Map))
			{
				throw new TomlRedefineKeyException("Cannot redefine existing key \"" + keysToString(keys) + "\" with value of type " + targetMap.get(key).getClass() + " as a table");
			}

			if (tableTypes.get((Map<String, Object>) targetMap.get(key)) == TableTypeContext.INLINE)
			{
				throw new TomlRedefineKeyException("Cannot extend inline table \"" + key + "\"");
			}

			if (tableContext == TableTypeContext.KEY_VALUE_IMPLICIT && previousTableContext == TableTypeContext.TABLE_IMPLICIT && tableTypes.get(targetMap.get(key)) == TableTypeContext.EXPLICIT)
			{
				throw new TomlRedefineKeyException("Dotted keys cannot insert into already defined explicit tables, see https://github.com/toml-lang/toml/issues/846");
			}

			previousTableContext = tableTypes.get(targetMap.get(key));

			targetMap = (Map<String, Object>) targetMap.get(key);
		}

		key = keysCopy.pop();
		if (targetMap.containsKey(key))
		{
			if (targetMap.get(key) instanceof Map && value instanceof Map)
			{
				Map<String, Object> target = (Map<String, Object>) targetMap.get(key);
				if (tableTypes.get(target) == TableTypeContext.TABLE_IMPLICIT && tableContext != TableTypeContext.EXPLICIT)
				{
					throw new TomlRedefineKeyException("Duplicate table \"" + keysToString(keys) + "\"");
				}

				if (tableTypes.get(target) == TableTypeContext.KEY_VALUE_IMPLICIT && tableContext == TableTypeContext.EXPLICIT)
				{
					throw new TomlRedefineKeyException("Cannot extend an implicitly created table \"" + key + "\" with an explicit table definition");
				}

				if (tableTypes.get(target) == TableTypeContext.EXPLICIT && tableContext == TableTypeContext.EXPLICIT)
				{
					throw new TomlRedefineKeyException("Duplicate explicit table \"" + key + "\"");
				}

				if (tableContext == TableTypeContext.INLINE)
				{
					throw new TomlRedefineKeyException("Cannot redefine implicitly defined table \"" + key + "\" as an inline table");
				}

				tableTypes.put((Map<String, Object>) targetMap.get(key), TableTypeContext.EXPLICIT);
				return (Map<String, Object>) targetMap.get(key);
			}
			throw new TomlRedefineKeyException("Cannot redefine existing key \"" + key + "\" with value of type " + targetMap.get(key).getClass().getName() + " as value with type " + value.getClass().getName());
		}

		if (value instanceof Map)
		{
			tableTypes.put((Map<String, Object>) value, tableContext);
		}

		if (value instanceof List)
		{
			arrayTypes.put((List<Object>) value, ArrayTypeContext.REGULAR);
		}
		targetMap.put(key, value);
		return null;
	}

	@Override
	public void addContext(ParserContext context) 
	{
		this.context.push(context);
	}

	@Override
	public ParserContext getContext() 
	{
		return this.context.peek();
	}

	@Override
	public void removeContext() 
	{
		this.context.pop();
	}

	@Override
	public TomlDecoder disableWarnings(TomlWarning... warnings) 
	{
		for (TomlWarning lint : warnings)
		{
			this.warnings[lint.ordinal()] = false;
		}

		return this;
	}

	@Override
	public TomlDecoder enableWarnings(TomlWarning... warnings) 
	{
		for (TomlWarning lint : warnings)
		{
			this.warnings[lint.ordinal()] = true;
		}

		return this;
	}

	@Override
	public boolean isWarningEnabled(TomlWarning warningType) 
	{
		Objects.requireNonNull(warningType);

		return this.warnings[warningType.ordinal()];
	}

	@Override
	public TomlDecoder setTomlVersion(TomlVersion version) 
	{
		this.version = Objects.requireNonNull(version);

		return this;
	}

	@Override
	public TomlVersion getVersion() 
	{
		return this.version;
	}

	@Override
	public LilacDecoder setWarningHandler(BiConsumer<TomlWarning, String> handler) 
	{
		this.warningHandler = handler;

		return this;
	}

	@Override
	public void sendWarning(TomlWarning warning, String message) 
	{
		Objects.requireNonNull(warning);
		Objects.requireNonNull(message);

		if (this.warningHandler != null)
		{
			this.warningHandler.accept(warning, message);
		}
	}

	private static enum KeyPlaceContext
	{
		TABLE,
		VALUE
		;
	}
}
