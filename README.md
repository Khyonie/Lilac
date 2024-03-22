# Lilac 🌸
Lilac is a high-performance Java 17 TOML language library with support for serialization and deserialization.

## Compliance
Lilac is fully compliant with the [v1.0.0 TOML language specification](https://toml.io/en/v1.0.0), with the only absence being inline tables and date/time types, which will be implemented at a later date.
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
assert tomlData.get("a_long").get().getClass().equals(Long.TYPE); # Perfection.
```
The allowed types are `byte`, `short`, `integer` (which is the default, and not included in serialization), `long`, `float` (default) and `double`. This feature is not turned on by default, as to remain 100% compliant with the TOML spec.

### Respects your comments and newlines
Several popular configuration libraries do not store comment and newline information. Lilac, by contrast, preserves positional comments and all empty lines. This feature is not turned on by default.
## Usage
Start by creating a TOML Builder:
```java
TomlBuilder toml = Toml.builder();
```
The TOML builder is used to serialize and deserialize:
```java
try {
  // Deserialize with a File object or String filepath:
  Map<String, TomlObject<?>> deserializedConfiguration = toml.parseDocument("config.toml");

  // You may also deserialize directly from a String:
  Map<String, TomlObject<?>> stringConfiguration = toml.parseString(this.tomlConfiguration);

  // And when you're done:
  String configuration = toml.toToml(stringConfiguration);
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
TomlBuilder toml = Toml.builder()
  .setPreservesComments(true)
  .setUppercaseHexadecimal(true)
  .setStoreJavaTypes(true);
```
## Performance
Lilac is written to be very high-performance, compiled regexes are cached whenever possible, and no lookahead/backtracking is ever performed, so the worst time complexity should be O(n) with respect to the document length.

### Thread safety
No single instance of a `TomlBuilder` is expected to be thread-safe. For multithreaded scenarios, you should create a `TomlBuilder` for each thread you need one in, or not run the same builder in parallel.

## TODO
- Inline tables
- Date/time types
- Arbitrary object serialization
