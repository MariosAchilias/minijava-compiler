## TODO
- [ ] LLVM IR emmiter
- [ ] run all stages
- [ ] cleanup

## Info

A BNF grammar for MiniJava can be found [here](https://cgi.di.uoa.gr/~compilers/project_files/minijava-new-2022/minijava.html).

Semantic checker (MiniJavaSemanticAnalyzer) checks MiniJava programs for semantic errors (e.g. type errors).
TODO: Add LLVM IR compiler

This repo also contains 2 small exercises from a compilers class:
* ExpressionEvaluator: a recursive descent parser for simple expressions (grammar below).
* SourceToSourceTranslator: transpiler that translates programs from a simple language (described [here](https://cgi.di.uoa.gr/~compilers/project.html#hw-1)) to a slightly simpler language (without equality and suffix operators).

## Building

### Semantic Checker
```
cd MiniJavaSemanticAnalyzer
make
java Main <file1> <file2> ...
```
### ExpressionEvaluator
```bash
cd ExpressionEvaluator
make
make execute
```
Input read from stdin.

### SourceToSourceTranslator

```bash
cd SourceToSourceTranslator
make
make execute < examples/ex1.txt
```

### Recursive descent parser (ExpressionEvaluator)

#### Grammar
```
exp -> powexp ExpTail

ExpTail -> op powexp ExpTail | ε

op -> + | -

powexp -> factor PowexpTail

PowexpTail -> ** powexp PowexpTail | ε

factor -> num | (exp)

num -> digit NumTail

NumTail -> digit NumTail | ε
```

