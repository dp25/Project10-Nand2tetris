import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private FileWriter output;
    private String spacing;
    private int f=0;

    public CompilationEngine(String filename) throws IOException {
        tokenizer = new JackTokenizer(filename);
        String outputName = "YX" + filename.split("\\.")[0] + ".xml";
        output = new FileWriter(new File(outputName));
        spacing = "";
	}

    public void compileClass() throws IOException {
        tokenizer.advance();
        if (tokenizer.keyword() == JackTokens.CLASS) {
            // write class
            output.write(spacing + "<tokens>");
            output.write('\n');
            increaseSpacing();

            writeTag(tokenizer.token(), "keyword");

            // write class name
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal class name identifier");
                return;
            }

            // write {
            tokenizer.advance();
            if (!checkSymbol("{")) {
                System.out.println("no openning { for class");
                return;
            }

            // parse potential classVarDec
            tokenizer.advance();
            while ( tokenizer.keyword() == JackTokens.STATIC ||
                    tokenizer.keyword() == JackTokens.FIELD) {
                compileClassVarDec();
                tokenizer.advance();
            }

            // parse potential subroutineDec
            while ( tokenizer.keyword() == JackTokens.CONSTRUCTOR ||
                    tokenizer.keyword() == JackTokens.FUNCTION ||
                    tokenizer.keyword() == JackTokens.METHOD) {
                compileSubRoutine();
                tokenizer.advance();
            }

            // write }
            if (!checkSymbol("}")) {
                System.out.printf("%s %d %d: is not closing } for class\n", tokenizer.token(), tokenizer.tokenType(), tokenizer.keyword());
                return;
            }

            if (tokenizer.hasMoreTokens()) {
                System.out.println("addtional tokens after closing }");
            }

            // write close tag of class
            decreaseSpacing();
            output.write(spacing + "</tokens>");
            output.write('\n');
        } else {
            System.out.println("does not start with class");
            return;
        }
    }

    public void compileClassVarDec() throws IOException {
        // we already know the current token is legit, so directy write it out.
        //output.write(spacing + "<classVarDec>");
        //output.write('\n');
        //increaseSpacing();

        writeTag(tokenizer.token(), "keyword");

        // match type
        tokenizer.advance();
        if (!checkAndWriteType()) {
            System.out.println("illegal type for class var dec");
            return;
        }

        // match varName
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal classVar identifier");
            return;
        }

        // match potential ", varName" part
        tokenizer.advance();
        while (tokenizer.symbol().equals(",")) {
            writeTag(",", "symbol");
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal classVar identifier");
                return;
            }
            tokenizer.advance();
        }

        // match ;
        if (tokenizer.symbol().equals(";")) {
            writeTag(";", "symbol");
        } else {
            System.out.println("no ending ;");
            return;
        }

        // decreaseSpacing();
        // output.write(spacing + "</classVarDec>");
        // output.write('\n');
    }

    private boolean checkAndWriteType() throws IOException {
        if (tokenizer.keyword() == JackTokens.INT ||
            tokenizer.keyword() == JackTokens.CHAR ||
            tokenizer.keyword() == JackTokens.BOOLEAN) {
            writeTag(tokenizer.token(), "keyword");
            return true;
        } else if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
            return true;
        } else {
            return false;
        }
    }

    public void compileSubRoutine() throws IOException {
        // write subroutineDec tag
        // output.write(spacing + "<subroutineDec>");
        // output.write('\n');

        // // New level
        // increaseSpacing();

        // already know that the current token start with constructor, function or method
        writeTag(tokenizer.token(), "keyword");

        // match return type
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokens.KEYWORD &&
            tokenizer.token().equals("void")) {
            writeTag("void", "keyword");
        } else if (!checkAndWriteType()) {
            System.out.println("Illegal type name for subroutine");
            return;
        }

        // match subroutine identifier
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal subroutine name");
            return;
        }

        // match parameter list
        tokenizer.advance();
        if (tokenizer.symbol().equals("(")) {
            writeTag("(", "symbol");
            compileParameterList();
        } else {
            System.out.println("no () after function name");
            return;
        }

        // match the closing ) for the paramater list
        if (tokenizer.symbol().equals(")")) {
            writeTag(")", "symbol");
        } else {
            System.out.println("no () after function name");
            return;
        }

        // match subroutine body
        tokenizer.advance();
        if (tokenizer.symbol().equals("{")) {
            compileSubroutineBody();
        } else {
            System.out.println("no { after function parameters");
            return;
        }

        // the closing } is matched in compileSubroutineBody()

        // decrease spacing
        // decreaseSpacing();

        // // write close subrountine tag
        // output.write(spacing + "</subroutineDec>");
        // output.write('\n');
    }

    public void compileParameterList() throws IOException {
        // output.write(spacing + "<parameterList>");
        // output.write('\n');
        // increaseSpacing();

        // write type
        tokenizer.advance();
        if (checkAndWriteType()) {
            // match varName
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal identifier in parameter list");
                return;
            }

            // match other arguments
            tokenizer.advance();
            while (tokenizer.symbol().equals(",")) {
                writeTag(",", "symbol");
                tokenizer.advance();
                if (!checkAndWriteType()) {
                    System.out.println("illegal type name");
                    return;
                }
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                    writeTag(tokenizer.identifier(), "identifier");
                } else {
                    System.out.println("illegal identifier name");
                    return;
                }
                tokenizer.advance();
            }
        }

        // decreaseSpacing();
        // output.write(spacing + "</parameterList>");
        // output.write('\n');
    }

    public void compileSubroutineBody() throws IOException {
        // output.write(spacing + "<subroutineBody>");
        // output.write('\n');
        // increaseSpacing();

        writeTag("{", "symbol");
        System.out.println(f++);
        tokenizer.advance();
        while ( tokenizer.tokenType() == JackTokens.KEYWORD &&
                tokenizer.token().equals("var")) {
            compileVarDec();
            tokenizer.advance();
        }

        compileStatements();

        // match }
        if (!checkSymbol("}")) {
            System.out.println("no } found to close subroutine call");
            System.out.printf("current token is : %s\n", tokenizer.token());
        }

        // decreaseSpacing();
        // output.write(spacing + "</subroutineBody>");
        // output.write('\n');
    }

    public void compileVarDec() throws IOException {
        // output.write(spacing + "<varDec>");
        // output.write('\n');
        // increaseSpacing();

        // write var
        writeTag("var", "keyword");

        // check type
        tokenizer.advance();
        if (!checkAndWriteType()) {
            System.out.println("illegal type for var");
            return;
        }

        // check varName
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal identifier for var");
            return;
        }

        tokenizer.advance();
        while (tokenizer.symbol().equals(",")) {
            writeTag(",", "symbol");

            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal identifier for var");
                return;
            }

            tokenizer.advance();
        }

        if (tokenizer.symbol().equals(";")) {
            writeTag(";", "symbol");
        } else {
            System.out.println("varDec doesn't end with ;");
            return;
        }

        // decreaseSpacing();
        // output.write(spacing + "</varDec>");
        // output.write('\n');
    }

    public void compileStatements() throws IOException {
        // output.write(spacing + "<statements>");
        // output.write('\n');
        // increaseSpacing();
        
        while (tokenizer.tokenType() == JackTokens.KEYWORD) {
            int keyword_type = tokenizer.keyword();
            // compileIf needs to do one token look ahead to check "else",
            // so no more advance here.
            switch(keyword_type) {
                case JackTokens.LET:    compileLet(); tokenizer.advance(); break;
                case JackTokens.IF:     compileIf(); break;
                case JackTokens.WHILE:  compileWhile(); tokenizer.advance(); break;
                case JackTokens.DO:     compileDo(); tokenizer.advance(); break;
                case JackTokens.RETURN: compileReturn(); tokenizer.advance(); break;
                default: System.out.println("illegal statement"); return;
            }
        }

        // decreaseSpacing();
        // output.write(spacing + "</statements>");
        // output.write('\n');
    }

    private boolean checkIdentifier() throws IOException {
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
            return true;
        } else {
            return false;
        }
    }

    private boolean checkSymbol(String s) throws IOException {
        if (s.equals("<")) { s = "&lt;"; }
        else if (s.equals(">")) { s = "&gt;"; }
        else if (s.equals("&")) { s = "&amp;"; }

        if (tokenizer.symbol().equals(s)) {
            writeTag(s, "symbol");
            return true;
        } else {
            return false;
        }
    }

    private boolean checkKeyword(String k) throws IOException {
        if (tokenizer.tokenType() == JackTokens.KEYWORD &&
            tokenizer.token().equals(k)) {
            writeTag(k, "keyword");
            return true;
        } else {
            return false;
        }
    }

    public void compileLet() throws IOException {
        // output.write(spacing + "<letStatement>");
        // output.write('\n');
        // increaseSpacing();

        writeTag("let", "keyword");

        tokenizer.advance();
        if (!checkIdentifier()) {
            System.out.println("Illegal identifier");
            return;
        }

        tokenizer.advance();
        if (checkSymbol("[")) {
            tokenizer.advance();
            compileExpression();

            if(!checkSymbol("]")) {
                System.out.printf("No closing ], current: %s\n", tokenizer.token());
                return;
            }
            // if has [], advance and next should be =
            tokenizer.advance();
        }

        if (!checkSymbol("=")) {
            System.out.println("No = found");
            return;
        }

        tokenizer.advance();
        compileExpression();

        // No need to advance because compileExpression does one token look ahead
        if (!checkSymbol(";")) {
            System.out.println("No ; found at the end of statement");
            return;
        }

        // decreaseSpacing();
        // output.write(spacing + "</letStatement>");
        // output.write('\n');
    }

    public void compileIf() throws IOException {
        // output.write(spacing + "<ifStatement>");
        // output.write('\n');
        // increaseSpacing();

        writeTag("if", "keyword");

        tokenizer.advance();
        if (!checkSymbol("(")) {
            System.out.println("No openning ( for if statement");
            return;
        }

        tokenizer.advance();
        compileExpression();

        //tokenizer.advance();
        if (!checkSymbol(")")) {
            System.out.println("No closing ) for if statement");
            return;
        }

        tokenizer.advance();
        if (!checkSymbol("{")) {
            System.out.println("No { for if statement");
            return;
        }

        tokenizer.advance();
        compileStatements();

        if (!checkSymbol("}")) {
            System.out.println("No } for if statement");
            System.out.printf("the current symbol is %s\n", tokenizer.token());
            return;
        }

        tokenizer.advance();
        if (checkKeyword("else")) {
            tokenizer.advance();
            if (!checkSymbol("{")) {
                System.out.println("No { for else statment");
                return;
            }

            tokenizer.advance();
            compileStatements();

            //tokenizer.advance();
            if (!checkSymbol("}")) {
                System.out.println("No } for if statement");
                return;
            }
            tokenizer.advance();
        }

        // decreaseSpacing();
        // output.write(spacing + "</ifStatement>");
        // output.write('\n');
    }

    public void compileWhile() throws IOException {
        // output.write(spacing + "<whileStatement>");
        // output.write('\n');
        // increaseSpacing();

        writeTag("while", "keyword");

        tokenizer.advance();
        if (!checkSymbol("(")) {
            System.out.println("No ( in while statement");
            return;
        }

        tokenizer.advance();
        compileExpression();

        //tokenizer.advance();
        if (!checkSymbol(")")) {
            System.out.println("No ) in while statement");
            return;
        }

        tokenizer.advance();
        if (!checkSymbol("{")) {
            System.out.println("No { in while statement");
            return;
        }

        tokenizer.advance();
        compileStatements();

        //tokenizer.advance();
        if (!checkSymbol("}")) {
            System.out.println("No } in while statement");
            return;
        }

        // decreaseSpacing();
        // output.write(spacing + "</whileStatement>");
        // output.write('\n');
    }

    public void compileDo() throws IOException {
        // output.write(spacing + "<doStatement>");
        // output.write('\n');
        // increaseSpacing();

        writeTag("do", "keyword");

        tokenizer.advance();
        // Before call compileSubRoutineCall, first check if the current
        // token is valid identifier. Then advance again and check if the it is . or (
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");

            tokenizer.advance();
            if (checkSymbol(".") || checkSymbol("(")) {
                compileSubRoutineCall();
            } else {
                System.out.println("Not valid subroutine call");
                return;
            }
        } else {
            System.out.println("Not a valid identifier for do statement");
            return;
        }

        tokenizer.advance();
        if (!checkSymbol(";")) {
            System.out.println("No closing ;");
            return;
        }

        // decreaseSpacing();
        // output.write(spacing + "</doStatement>");
        // output.write('\n');
    }

    public void compileReturn() throws IOException {
        // output.write(spacing + "<returnStatement>");
        // output.write('\n');
        // increaseSpacing();

        writeTag("return", "keyword");

        tokenizer.advance();
        // if the following is not ; then try to parse argument
        if (!checkSymbol(";")) {
            compileExpression();

            // after the expresison, it should end with ;
            if (!checkSymbol(";")) {
                System.out.println("return statement not ending with ;");
            }
        }

        // decreaseSpacing();
        // output.write(spacing + "</returnStatement>");
        // output.write('\n');
    }

    public void compileExpression() throws IOException {
        // output.write(spacing + "<expression>");
        // output.write('\n');
        // increaseSpacing();

        compileTerm();

        // compileTerm needs to do one token look ahead, so no advance here.
        while (checkSymbol("+") || checkSymbol("-") || checkSymbol("*") || checkSymbol("/") ||
               checkSymbol("&") || checkSymbol("|") || checkSymbol("<") || checkSymbol(">") ||
               checkSymbol("=")) {
            tokenizer.advance();
            compileTerm();
            // no advance here, because compileTerm needs to do one token look ahead
        }

        // decreaseSpacing();
        // output.write(spacing + "</expression>");
        // output.write('\n');
    }

    public void compileTerm() throws IOException {
        // output.write(spacing + "<term>");
        // output.write('\n');
        // increaseSpacing();

        if (tokenizer.tokenType() == JackTokens.INT_CONST) {
            writeTag(Integer.toString(tokenizer.intVal()), "integerConstant");
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokens. STRING_CONST) {
           writeTag(tokenizer.stringVal(), "stringConstant");
           tokenizer.advance();
        } else if (checkKeyword("true") || checkKeyword("false") || checkKeyword("null") ||
           checkKeyword("this")) {
            tokenizer.advance();
        } else if (checkSymbol("-") || checkSymbol("~")) {
            tokenizer.advance();
            compileTerm();
        } else if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
            tokenizer.advance();
            if (checkSymbol("[")) {
                compileArrayTerm();
                tokenizer.advance();
            } else if (checkSymbol("(") || checkSymbol(".")) {
                compileSubRoutineCall();
                tokenizer.advance();
            }
            // if doesn't match [, (, or ., it is a normal identifier
        } else if (tokenizer.tokenType() == JackTokens.SYMBOL) {
            if (checkSymbol("(")) {
                tokenizer.advance();
                compileExpression();
                if (checkSymbol(")")) {
                    tokenizer.advance();
                } else {
                    System.out.println("no closing bracket for term");
                }
            }

        } else {
            System.out.printf("illegal varName: %s\n", tokenizer.token());
            return;
        }

        // decreaseSpacing();
        // output.write(spacing + "</term>");
        // output.write('\n');
    }

    public void compileArrayTerm() throws IOException {
        tokenizer.advance();
        compileExpression();

        if (!checkSymbol("]")) {
            System.out.println("No closing ] for the array expression");
        }
    }

    public void compileSubRoutineCall() throws IOException {
        if (tokenizer.symbol().equals("(")) {
            tokenizer.advance();
            compileExpressionList();

            if (!checkSymbol(")")) {
                System.out.println("No closing ) for the expressionlist");
                return;
            }
        } else {
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal identifier for subroutine call");
                return;
            }

            tokenizer.advance();
            if (!checkSymbol("(")) {
                System.out.println("Expecting a open bracket in subroutine call");
                return;
            }

            tokenizer.advance();
            compileExpressionList();

            if (!checkSymbol(")")) {
                System.out.println("No closing ) for the expressionlist");
                return;
            }
        }
    }

    public void compileExpressionList() throws IOException {
        // output.write(spacing + "<expressionList>");
        // output.write('\n');
        // increaseSpacing();

        if (!tokenizer.symbol().equals(")")) {
            compileExpression();

            // because compileExpression did 1 token look ahead, no advance here
            while (checkSymbol(",")) {
                tokenizer.advance();
                compileExpression();
            }
        }

        // decreaseSpacing();
        // output.write(spacing + "</expressionList>");
        // output.write('\n');
    }

    private void writeTag(String word, String type) throws IOException {
        output.write(spacing + "<" + type + "> " + word + " </" + type + ">");
        output.write('\n');
        //System.out.println("<" + type + "> " + word + " </" + type + ">");
    }

    private void increaseSpacing() {
        spacing += "\t";
    }

    private void decreaseSpacing() {
        spacing = spacing.substring(1);
    }

    public static void main(String[] args) throws IOException {
        String filename = args[0];
        CompilationEngine engine = new CompilationEngine(filename);
        engine.compileClass();
    }
}