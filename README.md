# monkey-kotlin

An interpreter for the Monkey programming language written in Kotlin

## What’s Monkey?

Monkey has a C-like syntax, supports **variable bindings**, **prefix** and **infix operators**, has **first-class** and **higher-order functions**, can handle **closures** with ease and has **integers**, **booleans**, **arrays** and **hashes** built-in.

There is a book about learning how to make an interpreter: [Writing An Interpreter In Go](https://interpreterbook.com/#the-monkey-programming-language). This is where the Monkey programming language come from.

## Instruction

### Test

```bash
$ ./gradlew test
```

### Running the REPL

```bash
$ ./gradlew -q run
```

### Running the Interpreter

```bash
$ ./gradlew -q run -PappArgs=example/helloworld.mk
```

## License

[Apache-2.0](LICENSE)
