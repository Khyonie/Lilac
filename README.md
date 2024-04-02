# Lilac 🌸
Lilac is a high-performance Java 17 TOML language library, similarly to YAML and JSON, while focusing on readability.

Javadoc can be found [here](http://www.khyonieheart.coffee/javadoc/lilac/index.html).

## Compliance and testing
Lilac is *not* fully compliant with the TOML specification right now. Testing is performed using [toml-lang's toml-test suite](https://github.com/toml-lang/toml-test). For the most part, Lilac encodes and decodes correctly and pretty much all documents should work.

Contributions are welcome! ❤️
## Why would I want to use Lilac?
Lilac is written specifically to address some common shortcomings of other popular configuration languages and Java libraries:
### Inline types
By default, all integers will be serialized and deserialized as a `int` type, but this may not be the desired type, and an unwanted cast may be needed.
```toml
# TOML
a_long = 10 # I don't want to be an int!
```
```java
// Java
assert tomlData.get("a_long").get().getClass().equals(Integer.TYPE); // We can't have that, can we?
```
Lilac bends the TOML spec to optionally include inline type information:
```toml
# TOML
a_long: long = 10 # Ahh, much better.
```
```java
// Java
assert tomlData.get("a_long").get().getClass().equals(Long.TYPE); // Perfection.
```
The allowed types are `byte`, `short`, `integer` (which is the default, and not included in serialization), `long`, `float` (default) and `double`. This feature is not turned on by default, as to remain compliant with the TOML spec.

### Respects your comments and newlines
Several popular configuration libraries do not store comment and newline information. Lilac, by contrast, preserves positional comments and all empty lines. 
Even as you add keys and tables, your comments and whitespace will remain relationally consistent to the object above them (this does mean leading whitespace at the top of a document will be trimmed!). This feature is not turned on by default.
## Usage
Start by creating a TOML parser:
```java
TomlParser toml = Lilac.newBuilder();
```
The TOML parser is used to serialize and deserialize:
```java
try {
  // Deserialize with a String
  TomlConfiguration deserializedConfiguration = toml.getDecoder().decode(this.configuration);

  // And when you're done:
  configuration = toml.getEncoder().encode(deserializedConfiguration.getBacking());
} catch (TomlSyntaxException error) {
  error.printStackTrace();
  return;
}
```
### Default settings
The default `TomlBuilder` implementation `LilacTomlBuilder` does not preserve comments, serializes all hexadecimal numbers lowercase, and *does not store inline type information*. 
To enable these features, use `TomlBuilder#setPreservesComments(true)`, `TomlBuilder#setUppercaseHexadecimal(true)`, and/or `TomlBuilder.setStoreJavaTypes(true)`. 
These methods all return the builder instance being modified, so method chaining is possible:
```java
TomlParser toml = Lilac.newBuilder()
  .setPreserveComments(true)
  .setUppercaseHex(true)
  .setStoreInlineTypes(true);
```
## Performance
Lilac is written to be very high-performance, compiled regexes are cached whenever possible, and no lookahead/backtracking is ever performed, so the worst time complexity should be O(n) with respect to the document length.

### Thread safety
No single instance of a `TomlParser` is expected to be thread-safe. For multithreaded scenarios, you should create a `TomlParser` for each thread you need one in, or not run the same builder in parallel.
