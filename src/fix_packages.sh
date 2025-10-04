if [ ! -e Exceptions/ParseException.java ]; then
    mv Parser/ParseException.java Exceptions/
    sed -i '1ipackage Exceptions;\nimport Parser.Token;' Exceptions/ParseException.java
fi
sed -i '1iimport Exceptions.ParseException;' Parser/MiniJavaParser.java
for f in Parser/*.java; do
    grep -qF -- "package Parser;" $f || sed -i '1ipackage Parser;' $f
done
sed -i 's/package\ syntaxtree/package\ Parser.syntaxtree/g' Parser/*.java Parser/syntaxtree/*.java Parser/visitor/*.java
sed -i 's/package\ visitor/package\ Parser.visitor/g' Parser/*.java Parser/syntaxtree/*.java Parser/visitor/*.java
sed -i 's/import\ syntaxtree/import\ Parser.syntaxtree/g' Parser/*.java Parser/syntaxtree/*.java Parser/visitor/*.java
sed -i 's/import\ visitor/import\ Parser.visitor/g' Parser/*.java Parser/syntaxtree/*.java Parser/visitor/*.java
sed -i 's/visitor/Parser.visitor/g' Parser/syntaxtree/*.java
