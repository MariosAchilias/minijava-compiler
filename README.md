## TODO
- [ ] LLVM IR emitter
- [ ] cleanup

## Info
Compiler front-end for MiniJava, an educational Java-like language.

Perfroms basic semantic checks (e.g. use before declaration, type errors) and outputs LLVM IR.

Support for basic language features like dynamic dispatch and array bounds checking.

A BNF grammar for MiniJava can be found [here](http://web.archive.org/web/20221012221007/https://cgi.di.uoa.gr/~compilers/project_files/minijava-new-2022/minijava.html).

## Building

### Semantic Checker
```
cd MiniJavaSemanticAnalyzer
make
java Main <file1> <file2> ...
```