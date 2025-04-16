# Marios-Efstratios Achilias sdi2000015

# Part 1

Basic grammar:

```
exp -> num | exp op exp | (exp)

op -> + | - | **

num -> digit | digit num

digit -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
```

Modified to support operation precedence: (Not LL(1) yet)
```
exp -> powexp | powexp op factor

powexp -> factor | factor ** factor

op -> + | -

factor -> num | (exp)

num -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
```

Remove left recursion:

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

Build: (input read from stdin)

```bash
cd ExpressionEvaluator
make
make execute
```

# Part 2

Produces expected output on all 3 given examples.

Build and run examples as such:
```bash
cd SourceToSourceTranslator
make
make execute < examples/ex1.txt
```

The prefix and reverse operators are implemented in java code using String.startsWith() and StringBuilder.reverse(), respectively.

"if-else" expressions are converted to java ternary expressions.

Source "if-else" expressions with equality comparisons are converted in IR code to a nested if-else expression as such:
```
if (e1 == e2)
    e3
else
    e4
```

Becomes:

```
if (e1 prefix e2)
    if (e2 prefix e1)
        e3
    else
        e4
else
    e4
```

The suffix operator is converted to equivalent IR as such:
``e1 suffix e2`` becomes: ``reverse e1 prefix reverse e2``