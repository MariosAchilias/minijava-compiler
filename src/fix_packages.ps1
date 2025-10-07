if (-not (Test-Path "Exceptions/ParseException.java")) {
    Move-Item "Parser/ParseException.java" "Exceptions/ParseException.java"
    $parseException = Get-Content "Exceptions/ParseException.java"
    $header = @("package Exceptions;", "import Parser.Token;")
    $header + $parseException | Set-Content "Exceptions/ParseException.java"
}

$miniJavaParser = Get-Content "Parser/MiniJavaParser.java"
@("import Exceptions.ParseException;") + $miniJavaParser | Set-Content "Parser/MiniJavaParser.java"

Get-ChildItem "Parser" -Filter "*.java" | ForEach-Object {
    $file = $_.FullName
    $content = Get-Content $file
    if ($content[0] -ne "package Parser;") {
        @("package Parser;") + $content | Set-Content $file
    }
}

$javaFiles = @(Get-ChildItem "Parser" -Filter "*.java" -Recurse) | Select-Object -Unique

foreach ($file in $javaFiles) {
    (Get-Content $file.FullName) `
        -replace "package syntaxtree", "package Parser.syntaxtree" `
        -replace "package visitor", "package Parser.visitor" `
        -replace "import syntaxtree", "import Parser.syntaxtree" `
        -replace "import visitor", "import Parser.visitor" `
        | Set-Content $file.FullName
}

if (Test-Path "Parser/syntaxtree") {
    Get-ChildItem "Parser/syntaxtree" -Filter "*.java" | ForEach-Object {
        (Get-Content $_.FullName) -creplace "visitor", "Parser.visitor" | Set-Content $_.FullName
    }
}