## Info
Compiler front-end for MiniJava, an educational Java-like language.

Performs basic semantic checks (e.g. use before declaration, type errors) and outputs LLVM IR.

Support for basic language features like dynamic dispatch and runtime array bounds checking.

A BNF grammar for MiniJava can be found [here](http://web.archive.org/web/20221012221007/https://cgi.di.uoa.gr/~compilers/project_files/minijava-new-2022/minijava.html).

## Building
```
cd src
make
```

## Usage
e.g.
```
java Main source.java # Compile to LLVM IR
clang source.ll -o executable # Output native binary
./executable
```

## TODOs
- [ ] Arrays
- [X] Method calls
- [X] Allocation statements
- [X] Assignments
- [ ] Misc (e.g. "this" pointer)
- [ ] Fix problems with main method (e.g. argv)
- [X] Fix method names in IR (append class name)
- [ ] General code cleanup