Marios-Efstratios Achilias sdi2000015

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

PowexpTail -> ** factor PowexpTail | ε

factor -> num | (exp)

num -> digit NumTail

NumTail -> digit NumTail | ε
```