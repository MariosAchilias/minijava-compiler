## TODO
- [ ] LLVM IR emitter
- [ ] run all stages
- [ ] cleanup

## Info
Compiler front-end for MiniJava, an educational Java-like language.

Outputs LLVM IR.

A BNF grammar for MiniJava can be found [here](http://web.archive.org/web/20221012221007/https://cgi.di.uoa.gr/~compilers/project_files/minijava-new-2022/minijava.html).

Semantic checker (MiniJavaSemanticAnalyzer) checks MiniJava programs for semantic errors (e.g. type errors).
TODO: Add LLVM IR emitter

## Building

### Semantic Checker
```
cd MiniJavaSemanticAnalyzer
make
java Main <file1> <file2> ...
```