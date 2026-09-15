## G2-Background
```
implementation("io.github.laysar:G2Background:1.5.0")
```

### Compatibility

The library works from Android 8 (26) through Android 17 (37)

### XML

The library currently provides hybrid elements for XML:

```xml
G2LinearLayout
G2ConstraintLayout
```

Other important elements will be added later only

### Java

To use the library in Java instead of XML, start with a `G2Background` object:

```java
G2Background VARIABLE_NAME = new G2Background(Context);
VARIABLE_NAME.*(...);
```