# Lilac
Lilac is a high-performance TOML configuration encoder and decoder for Java, with user configurations as the primary goal. 

## Usage
### Decoding
To decode a document into a Map, create a `TomlDecoder` object with the specified TOML specification version:
```java
TomlDecoder decoder = new LilacDecoder(TomlVersion.V1_0_0);
```
Then you may pass a file or string representing a TOML document to the decoder:
```java
File configurationFile = new File("configuration.toml");
Map<String, Object> configuration = decoder.decode(configurationFile);
```

### Encoding
Similarly to decoding, to encode a configuration into a TOML string, create a `TomlEncoder` object:
```java
TomlEncoder encoder = new LilacEncoder();
```
Then encode the desired configuration:
```java
String tomlConfiguration = encoder.encode(configuration);
```
---
## Compliance and testing
Lilac is tested using [BurntSushi's toml-test suite](https://github.com/toml-lang/toml-test), and tests both v1.0 and v1.1 TOML specification versions.
To run the tests yourself:
1) Clone and build the project with the "test" folder added as a source.
2) Copy the toml-test's "tests" folder into the project directory.
3) Run:
```
$ java -jar Lilac-<version>-testing.jar <(scope) | all> <valid | invalid | all> <v1_0_0 | v1_1_0>
```
