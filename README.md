# DBRNG

A tool for populating PostgresSQL databases with random data

## Usage:

### Building:

Due to testcontainers usage, tests require a docker environment so it's recommended to skip tests when building.

```shell
./gradlew build -x test
```

### CLI:
```bash
java dbrng.jar -U username -P password -c 10
```

flags:

```
-h          host     (default=localhost)
-p          port     (default=5432)
-d          database (default=postgres)
-U          username
-P          password
-c          count of rows to generate
```

It's also possible to specify a custom generator for columns

### Custom:

```java
Configuration config = new Configuration(
        "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres",
        20
)
        .with(
                "public.customer.name",
                () -> "myCustomGenerator-%s".formatted(StringGenerator.generateString(4))
        )
        .with(
                "public.customer.email",
                () -> "%s@gmail.com".formatted(StringGenerator.generateString(4))
        );

Populator.populate(config);
```

## TODO:

- [ ] custom rows count per table
- [ ] CHECK constraint support
- [ ] Support all postgres data types