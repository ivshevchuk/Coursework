import java.util.EnumSet;

public class Parser {
    private final Tokens tokens ;

    public Parser(Tokens tokens) {
        this.tokens=tokens;
    }
    private Functions functions = new Functions();

    public Node Parsing() {
        Variables vars = new Variables();
        Node node = new Node("", Integer.MAX_VALUE);
        ListOfTokens currToken = tokens.getFirstType();

        Node finalNode = parseFunction(node,  currToken, vars);
        if (finalNode == null || !(finalNode == node))
            return null;
        String errorFunc = functions.finalCheck();
        if(errorFunc != null)
            return parseError("Parsing error: function '" + errorFunc + "' called, but undefined!");
        if(!functions.containsMain())
            return parseError("Parsing error: there is no main function in the program!");
        return node;
    }

    private Node parseFunction(Node node, ListOfTokens currTokenT, Variables variables){
        String name;
        Variables currVars = variables.openBrace();
        Variables prevVars;
        //check if code have next function
        if (currTokenT.equals(ListOfTokens.KEYWORD_INT)) {
            currTokenT = tokens.getNextType();

            //if current function is the main
            if (currTokenT.equals(ListOfTokens.KEYWORD_MAIN)) {

                //syntax checking
                if (tokens.getNextType().equals(ListOfTokens.OPEN_CAST) &&
                        tokens.getNextType().equals(ListOfTokens.CLOSE_CAST)) {//  '()'
                    currTokenT = tokens.getNextType();
                    if (currTokenT.equals(ListOfTokens.SEMICOLONS) ) {// ';'
                        prevVars = currVars.skipCurBrace(0);
                        if (tokens.hasNext()) {
                            return parseFunction(node, tokens.getNextType(), prevVars);
                        } else return node;
                    }
                    else if (!currTokenT.equals(ListOfTokens.OPEN_BRACE)) {
                        return parseError("Invalid 'main' function declaration");
                    }
                }
                else return parseError("Parsing error: '()' expected after the name of the 'main' function");

                //body of main function
                name = "main";
                Node mainFunc = new Node("main_func", Integer.MAX_VALUE);
                node.addChild(mainFunc);
                mainFunc.addChild(new Node("main_name", 0));

                boolean containsCheck =  functions.foundFunc(name, 0);
                boolean noSuchBodyCheck = functions.functionBody(name, 0);
                if(!(containsCheck && noSuchBodyCheck))
                    return parseError("Parsing error: There are 2 main functions");

                Node withBody = parseFuncBody(mainFunc, currVars);
                if (withBody == null || !withBody.equals(mainFunc))
                    return null;
            }

            //not main function
            else if (currTokenT.equals(ListOfTokens.IDENTIFIER)) {
                name = tokens.currVal();
                if (tokens.getNextType().equals(ListOfTokens.OPEN_CAST)) {
                    Node paramNode = new Node(name + "_param", 10);
                    Node withParam = parseFuncParam(paramNode, currVars);
                    if (withParam == null || withParam != paramNode)
                        return null;

                    int paramCount = withParam.getChildrenCount();
                    boolean containsCheck =  functions.foundFunc(name, paramCount);

                    currTokenT = tokens.getNextType();
                    if (currTokenT.equals(ListOfTokens.SEMICOLONS)) {
                        //function declaration
                        prevVars = currVars.skipCurBrace(paramCount);
                        return safeRecursion(node, prevVars);
                    }
                    else if (currTokenT.equals(ListOfTokens.OPEN_BRACE)) {
                        //function definition
//                        node.newFunc();
                        Node currFunc = new Node(name + "_func", 3);
                        node.addChild(currFunc);
                        currFunc.addChild(new Node(name + "_name", 0));
                        currFunc.addChild(paramNode);
                        Node funcBody = new Node(name + "_body", Integer.MAX_VALUE);
                        currFunc.addChild(funcBody);

                        boolean noSuchBodyCheck = functions.functionBody(name, paramCount);
                        if(!noSuchBodyCheck)
                            return parseError("Parsing error: There are 2 '" + name + "' functions");
                        if(!containsCheck)
                            return parseError("Parsing error: Function '" + name  + "' has different list of parameters");

                        Node withBody = parseFuncBody(funcBody, currVars);
                        if (withBody == null || !withBody.equals(funcBody))
                            return null;
                    }
                    else return parseError("Parsing error: invalid syntax after parameters of the '" + name + "' function");
                } else return parseError("Parsing error: expected '(' after the name of the function");
            }
            else return parseError("Parsing error: invalid name of the function");
            prevVars = currVars.closeBrace();
        }
        else return parseError("Parsing error! Invalid type of function");

        boolean finalBrace = false;
        while (tokens.hasNext())
            if(tokens.getNextType().equals(ListOfTokens.CLOSE_BRACE)){
                finalBrace = true;
                break;
            }
        if(!finalBrace)
            return parseError("Invalid the end of the " + name + " function");
        else return safeRecursion(node, prevVars);
    }

    private Node safeRecursion(Node node, Variables variables){
        if(tokens.hasNext()){
            return parseFunction(node, tokens.getNextType(), variables);
        }
        //the end of recursion (without exceptions)
        else return node;
    }

    private Node parseFuncParam(Node node, Variables vars){
        ListOfTokens currTokenT = tokens.getNextType();
        while (!currTokenT.equals(ListOfTokens.CLOSE_CAST)){
            if(!Tokens.typeKeyword.contains(currTokenT)){
                return parseError("Parsing error: type '" + currTokenT + "' is undefined");
            }
            if(!tokens.getNextType().equals(ListOfTokens.IDENTIFIER)){
                return parseError("Parsing error: parameter '" + currTokenT + "' is unexpected");
            }
            vars.addVar(tokens.currVal() + "_var");
            Node paramNode = new Node(tokens.currVal() + "_var", 0);
            paramNode.isParam();
            paramNode.setPoint(vars.getPoint(tokens.currVal() + "_var"));
            node.addChild(paramNode);

            currTokenT = tokens.getNextType();
            if(currTokenT.equals(ListOfTokens.COMMA)) {
                currTokenT = tokens.getNextType();
                continue;
            }else if(!currTokenT.equals(ListOfTokens.CLOSE_CAST))
                return parseError("Parsing error: type of '" + currTokenT + "' is undefined");
        }
        return node;

    }

    private Node parseError(String msg) {
        System.out.println(msg);
        System.out.println("Current lexem is " + tokens.currVal());
        return null;
    }

    private Node callFunction(String name, Node currNode, Variables vars){
        Node callNode = new Node(name + "_call", 10);
        currNode.addChild(callNode);
        ListOfTokens currTokenT = tokens.getNextType();
        int argumentsCount = 0;
        try {
            while (!currTokenT.equals(ListOfTokens.CLOSE_CAST)) {
                argumentsCount ++;
                Node pargNode = parseStatement(callNode, ListOfTokens.COMMA, vars, currTokenT, false);
                if (pargNode == null || !pargNode.equals(callNode))
                    return parseError("Error while parsing function argument");
                currTokenT = tokens.getNextType();
            }
        }catch (NullPointerException e){
            return parseError("Parsing error: there is wrong arguments syntax while calling '" + name + "' function");
        }
        if(!functions.activateFunction(name, argumentsCount))
            return parseError("Parsing error: '"+ name + "' with " + argumentsCount + " parameters was not declared!");
        return currNode;
    }

    private Node parseFuncBody(Node currNode, Variables vars) {
        Node mainNode = currNode;
        ListOfTokens currTokenT = tokens.getNextType();
        boolean hasType;
        int forPoint = 0;

        while (!currTokenT.equals(ListOfTokens.KEYWORD_RETURN)) {
            hasType = false;
            if (Tokens.typeKeyword.contains(currTokenT)) {
                hasType = true;
                currTokenT = tokens.getNextType();
            }
            if (currTokenT.equals(ListOfTokens.IDENTIFIER)) {
                Node withIdentif = parseIdentifier(vars, hasType, currNode, ListOfTokens.SEMICOLONS);
                if (withIdentif == null || !withIdentif.equals(currNode))
                    return null;
            }
            else if (hasType) {
                return parseError("Error while parsing! There is keyword and no variable after that");
            }
            else if (currTokenT.equals(ListOfTokens.KEYWORD_FOR)) {
                forPoint++;
                vars = vars.openBrace();
                if (!tokens.getNextType().equals(ListOfTokens.OPEN_CAST))
                    return parseError("Parsing error: there is no '(' after 'for'");

                // initial clause
                //int i = 0;
                //i = 0;
                //;
                currTokenT = tokens.getNextType();
                if (Tokens.typeKeyword.contains(currTokenT)) {
                    hasType = true;
                    currTokenT = tokens.getNextType();
                }
                if (currTokenT.equals(ListOfTokens.IDENTIFIER)) {
                    Node withIdentif = parseIdentifier(vars, hasType, currNode, ListOfTokens.SEMICOLONS);
                    if (withIdentif == null || !withIdentif.equals(currNode))
                        return parseError("Error while parsing identifier for 'for' loop");
                } else if (!currTokenT.equals(ListOfTokens.SEMICOLONS)) {
                    return parseError("Parsing error: expected initial clause after 'for(' or nothing before ';'");
                }

                Node forNode = new Node("for", 4);
                currNode.addChild(forNode);
                forNode.addChild(new Node("for_start" + forPoint, 0));

                // controlling expression
                // i <= 0
                // ;
                // statement
                currTokenT = tokens.getNextType();
                ListOfTokens nextTokenT = tokens.getNextType();
                tokens.indexMinus(1);
                if (currTokenT.equals(ListOfTokens.IDENTIFIER) && vars.totalContains(tokens.currVal() + "_var") &&
                        nextTokenT.equals(ListOfTokens.LESS_THAN)) {
                    Node identNode = new Node(tokens.currVal() + "_val_for", 2);
                    identNode.setPoint(vars.getVal(tokens.currVal() + "_var"));
                    forNode.addChild(identNode);
                    tokens.getNextType();
                    currTokenT = tokens.getNextType();
                    if (currTokenT.equals(ListOfTokens.EQUALS)) {
                        forNode.addChild(new Node("minusOne", 0));
                        currTokenT = tokens.getNextType();
                    }

                    Node lessNode = new Node("less" + forPoint, 2); //2 for statement function
                    currNode.addChild(lessNode);
                    Node withStat = parseStatement(lessNode, ListOfTokens.SEMICOLONS, vars, currTokenT, false);
                    if (withStat == null || !withStat.equals(lessNode))
                        return parseError("Error while parsing statement after '<' in 'for' loop");
                } else if (currTokenT.equals(ListOfTokens.IDENTIFIER) && vars.totalContains(tokens.currVal() + "_var") &&
                        nextTokenT.equals(ListOfTokens.MORE_THAN)) {
                    Node identNode = new Node(tokens.currVal() + "_val_for", 2);
                    identNode.setPoint(vars.getVal(tokens.currVal() + "_var"));
                    forNode.addChild(identNode);
                    tokens.getNextType();
                    currTokenT = tokens.getNextType();
                    if (currTokenT.equals(ListOfTokens.EQUALS)) {
                        forNode.addChild(new Node("plusOne", 0));
                        currTokenT = tokens.getNextType();
                    }

                    Node moreNode = new Node("more" + forPoint, 2); //2 for statement function
                    forNode.addChild(moreNode);
                    Node withStat = parseStatement(moreNode, ListOfTokens.SEMICOLONS, vars, currTokenT, false);
                    if (withStat == null || !withStat.equals(moreNode))
                        return parseError("Error while parsing statement after '>' in 'for' loop");
                } else if (currTokenT.equals(ListOfTokens.SEMICOLONS)) {
                    forNode.addChild(new Node("1", 0));
                } else {
                    Node statNode = parseStatement(forNode, ListOfTokens.SEMICOLONS, vars, currTokenT, false);
                    if (statNode == null || !statNode.equals(forNode))
                        return parseError("Parsing error: expected ';' after controlling expression with '>' in 'for' loop");
                }

                // post-expression
                Node childNode = new Node(forPoint + "for_{", 2);
                currNode.addChild(childNode);
                Node forBodyNode = new Node(forPoint + "for_body", 100);
                childNode.addChild(forBodyNode);
                currTokenT = tokens.getNextType();
                if (currTokenT.equals(ListOfTokens.IDENTIFIER)) {
                    Node withPostExpr = parseIdentifier(vars, false, childNode, ListOfTokens.CLOSE_CAST);
                    if (withPostExpr == null || !withPostExpr.equals(childNode)) {
                        return parseError("Error while parsing 'for' cycle: unexpected statement in 'post-expression' section");
                    }
                } else if (!currTokenT.equals(ListOfTokens.CLOSE_CAST)) {
                    return parseError("Parsing error! Unexpected post-expression in 'for' cycle");
                }
                if (!tokens.getNextType().equals(ListOfTokens.OPEN_BRACE)) {
                    return parseError("Parsing error! Expected '{' after 'for(...)'");
                }
                currNode = forBodyNode;
            }
            else if (currTokenT.equals(ListOfTokens.KEYWORD_BREAK)) {
                currNode.addChild(new Node("BREAK", 0));
                if (!tokens.getNextType().equals(ListOfTokens.SEMICOLONS))
                    return parseError("Parsing error! Unexpected symbol '" + tokens.currVal() + "' after keyword 'break'");
            }
            else if (currTokenT.equals(ListOfTokens.KEYWORD_CONTINUE)) {
                currNode.addChild(new Node("CONTINUE", 0));
                if (!tokens.getNextType().equals(ListOfTokens.SEMICOLONS))
                    return parseError("Parsing error! Unexpected symbol '" + tokens.currVal() + "' after keyword 'continue'");
            }
            else if (currTokenT.equals(ListOfTokens.KEYWORD_IF)) {
                if (!tokens.getNextType().equals(ListOfTokens.OPEN_CAST)) {
                    System.out.println("error: expected '(' after 'if'");
                }

                Node lastNode = new Node("if", 2);
                currNode.addChild(lastNode);

                Node node1 = new Node("if_pop", 2);
                Node statNode = parseStatement(node1, ListOfTokens.CLOSE_CAST, vars, tokens.getNextType(), true);
                if (statNode == null || !statNode.equals(node1))
                    return parseError("Error while parsing statement");
                Node node2 = null, node3;
                boolean shouldHaveConst = false;

                currTokenT = tokens.getNextType(); //< > = or )
                if (currTokenT.equals(ListOfTokens.CLOSE_CAST)) {
                    node2 = new Node("if_with_0", 1);
                    node3 = new Node("if_neq", 1);
                }
                else if (currTokenT.equals(ListOfTokens.LESS_THAN)) {
                    node3 = new Node("if_less", 1);
                    shouldHaveConst = true;
                }
                else if (currTokenT.equals(ListOfTokens.MORE_THAN)) {
                    node3 = new Node("if_more", 1);
                    shouldHaveConst = true;
                }
                else if (currTokenT.equals(ListOfTokens.EQUALS)) {
                    if (!tokens.getNextType().equals(ListOfTokens.EQUALS))
                        return parseError("Parsing error! There is only one '=' in cast after 'if'!");
                    node3 = new Node("if_eq", 1);
                    shouldHaveConst = true;
                } else return parseError("Parsing error occurs while parsing second statement in cast after 'if'!");

                if(shouldHaveConst){
                    currTokenT = tokens.getNextType();
                    String name = tokens.currVal();
                    int index = vars.getVal( name + "_var");

                    if (currTokenT.equals(ListOfTokens.INT_CONSTANT)){
                        node2 = new Node("if_with_" + tokens.currVal(), 1);
                    } else if (index != -1) {
                        index = (index + 3) * 4;
                        node2 = new Node("if_with_[ebp-" + index + "]", 1);
                    }  else return parseError("Parsing error occurs while parsing second statement in cast after 'if'!");
                }

                node2.addChild(node1);
                node3.addChild(node2);
                lastNode.addChild(node3);
                if(!tokens.getNextType().equals(ListOfTokens.CLOSE_CAST)){
                    return parseError("Parsing error occurs in cast after 'if'. There is no close cast");
                }
            }
            else if (currTokenT.equals(ListOfTokens.KEYWORD_ELSE)){
                try {
                    if (!((currNode.getTailChild(1).getValue().equals("{") &&
                            currNode.getTailChild(1).getTailChild(1).getValue().equals("if_else") ||
                            currNode.getTailChild(1).getValue().matches("[a-zA-Z_][a-zA-Z_0-9]*_var")) &&
                            currNode.getTailChild(2).getValue().equals("if"))) {
                        return parseError("Error! 'else' without a previous 'if' or inappropriate value between it!");
                    }

                } catch (IndexOutOfBoundsException e){
                    return parseError("Error! 'else' without a previous 'if' or inappropriate value between it!");
                }
                currNode.addChild(new Node("else", 0));
            }
            else if(currTokenT.equals(ListOfTokens.OPEN_BRACE)) {
                vars = vars.openBrace();
                Node childNode = new Node("{", 100);
                try {
                    if (currNode.getTailChild(1).getValue().equals("else")) {
                        childNode.switchAfterElse();
                    }
                } catch (IndexOutOfBoundsException e){/*ok*/}
                currNode.addChild(childNode);
                currNode = childNode;
            }
            else if(currTokenT.equals(ListOfTokens.CLOSE_BRACE)){
                vars = vars.closeBrace();
                Node childNode;
                String prevVal = currNode.getParent().getTailChild(2).getValue();
                if(prevVal.equals("if")){
                    currTokenT = tokens.getNextType();
                    if(currTokenT.equals(ListOfTokens.KEYWORD_ELSE))
                        childNode = new Node ("if_else", 0);
                    else
                        childNode = new Node ("if_end", 0);
                    tokens.indexMinus(1);
                } else if(prevVal.equals("else")){
                    childNode = new Node ("else_end", 0);
                } else childNode = new Node("}", 0);
                currNode.addChild(childNode);

                if(currNode.getValue().equals("{")){
                    currNode = currNode.getParent();
                } else if(currNode.getValue().matches("[0-9]for_body")){
                    currNode = currNode.getParent().getParent();
                } else return parseError("Error while parsing '{}'. There are '}', but no '{' before it!");
            } else return parseError("Parsing error! Unrecognised symbol '" + currTokenT +"'.");
            currTokenT = tokens.getNextType();
        }

        Node retNode = new Node("return", 2);
        mainNode.addChild(retNode);
        Node newRet = parseStatement(retNode, ListOfTokens.SEMICOLONS, vars, tokens.getNextType(), false);
        if (newRet == null || !newRet.equals(retNode)) {
            return parseError("Error while parsing statement after 'return'");
        }
        return mainNode;
    }

    private Node parseIdentifier(Variables vars, boolean hasType, Node currNode, ListOfTokens lastTokenT){
        String name = tokens.currVal();
        ListOfTokens currTokenT = tokens.getNextType();
        if (currTokenT.equals(ListOfTokens.OPEN_CAST)) {
            if(hasType)
                return parseError("Parsing error: there is an identifier before function call!");

            Node callNode = callFunction(name, currNode, vars);
            if (callNode == null || !callNode.equals(currNode))
                return null;
            if(!tokens.getNextType().equals(ListOfTokens.SEMICOLONS))
                return parseError("Error after parsing call of '" + name + "' function");
        } else {
            boolean canBeDecl = vars.contains(name + "_var");
            boolean canBeInit = vars.totalContains(name + "_var");
            if (hasType) {
                if (canBeDecl) {
                    return parseError("The variable " + name + " is declarated several times!");
                }
                vars.addVar(name + "_var");
            } else if (!canBeInit) {
                return parseError("Variable " + name + " initialized, but not declarated");
            }

            Node var = new Node( name + "_var", 2);
            currNode.addChild(var);
            currNode = var;
            currNode.setPoint(vars.getPoint(name + "_var"));

            if (currTokenT.equals(ListOfTokens.SEMICOLONS)) {
                currNode = currNode.getParent();
            } else if (currTokenT.equals(ListOfTokens.EQUALS)) {
                Node statNode = parseStatement(currNode, lastTokenT, vars, tokens.getNextType(), false);
                if (statNode == null || !statNode.equals(currNode))
                    return parseError("Error while parsing statement");

                if (!vars.addVal(currNode.getValue())) {
                    return parseError("Error while parsing! Variable " + currNode.getValue() + " is not initialised!");
                }
                currNode = currNode.getParent();
            } else if(currTokenT.equals(ListOfTokens.DIVISION)){
                tokens.getNextType();
                Node divNode = new Node("/", 2);
                currNode.addChild(divNode);

                Node childNode = new Node(name + "_val", 0);
                int index = vars.getVal( name + "_var");
                childNode.setPoint(index);
                divNode.addChild(childNode);

                Node dividerNode = new Node("((", 1);
                divNode.addChild(dividerNode);
                Node statNode = parseStatement(dividerNode, ListOfTokens.SEMICOLONS, vars, tokens.getNextType(), false);
                if (statNode == null || !statNode.equals(dividerNode))
                    return parseError("Error while parsing statement");

                if (!vars.addVal(currNode.getValue())) {
                    return parseError("Error while parsing! Variable " + currNode.getValue() + " is not initialised!");
                }
                currNode = currNode.getParent();

            }else return parseError("Error occurred after variable " + name);
        }
        return currNode;
    }

    private Node parseStatement(Node currNode, ListOfTokens stopTokenT, Variables vars, ListOfTokens currTokenT, boolean inIf) {
        EnumSet<ListOfTokens> binaryOp = EnumSet.of(ListOfTokens.DIVISION, ListOfTokens.MULTIPLICATION, ListOfTokens.LOGICAL_AND, ListOfTokens.ADDITION, ListOfTokens.MODULO);
        Node basic = currNode;

        while (!currTokenT.equals(stopTokenT) || currNode.getValue().equals("(")) {
            if (currTokenT.equals(ListOfTokens.NEGATION)) {
                if (currNode.getValue().equals("(") || currNode.equals(basic)) {
                    Node childNode = new Node("-", 1);
                    currNode.addChild(childNode);
                    currNode = childNode;
                } else
                    return parseError("Error while parsing '-': unary operation is without ()");
            }
            else if (binaryOp.contains(currTokenT)) {
                if (currTokenT.equals(ListOfTokens.ADDITION)) {
                    while (currNode.getTailChild(1).getValue().equals("&&") &&
                            !currNode.getValue().equals("(")) {
                        currNode = currNode.getTailChild(1);
                    }
                } else if (currTokenT.equals(ListOfTokens.DIVISION) || currTokenT.equals(ListOfTokens.MODULO)) {
                    while ((currNode.getTailChild(1).getValue().equals("&&") || currNode.getTailChild(1).getValue().equals("+"))&&
                            !currNode.getValue().equals("(")) {
                        currNode = currNode.getTailChild(1);
                    }
                }
                if (currNode.getChildrenCount() != 0) {
                    Node binaryNode = new Node(tokens.currVal(), 2);
                    Node child = currNode.removeLastChild();
                    binaryNode.addChild(child);
                    currNode.addChild(binaryNode);
                    currNode = binaryNode;
                } else return parseError("Invalid binary operation syntax");
            }
            else if (currTokenT.equals(ListOfTokens.OPEN_CAST)) {
                Node childNode = new Node("(", 1);
                currNode.addChild(childNode);
                currNode = childNode;
            }
            else if (currTokenT.equals(ListOfTokens.CLOSE_CAST)) {
                try {
                    while (!currNode.getValue().equals("(")) {
                        currNode = currNode.getParent();
                    }
                } catch (NullPointerException e) {
                    if(stopTokenT.equals(ListOfTokens.COMMA)) {
                        tokens.indexMinus(1);
                        return basic;
                    }
                    else return parseError("Error while parsing '()' There are ')', but no '(' before it!");
                }
                if (currNode.hasMaxChildren()) {
                    Node parent = currNode.getParent();
                    parent.replaceChild(currNode, currNode.getTailChild(1));
                    currNode = parent;
                }
            }
            else if(inIf && (currTokenT.equals(ListOfTokens.EQUALS) || currTokenT.equals(ListOfTokens.LESS_THAN) || currTokenT.equals(ListOfTokens.MORE_THAN))){
                if(!currNode.equals(basic)){
                    return parseError("Error while parsing statement in 'if' condition! There is open cast  one '=' symbol.");
                }
                tokens.indexMinus(1);
                return currNode;
            }
            else {
                currNode = parseValue(currNode, currTokenT, vars);
                if(currNode == null) return null;
            }
            try {
                while (currNode.hasMaxChildren() && !(currNode.getValue().equals("(")  ||
                        currNode.getValue().equals("((") || currNode.getValue().equals("if"))) {
                    currNode = currNode.getParent();
                }
            } catch (NullPointerException e){
                return parseError("Error while parsing after token " + tokens.currVal());
            }
            currTokenT = tokens.getNextType();
        }
        return currNode;
    }

    private Node parseValue(Node currNode, ListOfTokens currToken, Variables vars) {
        String name = tokens.currVal();
        int index = vars.getVal( name + "_var");
        Node childNode;
        if (currToken.equals(ListOfTokens.INT_CONSTANT)) {
            if (!(tokens.getNextType().equals(ListOfTokens.DOT) & tokens.getNextType().equals(ListOfTokens.INT_CONSTANT))) {
                tokens.indexMinus(2);
            }
            childNode = new Node(name, 0);
            currNode.addChild(childNode);
        } else if (currToken.equals(ListOfTokens.INT_BIN_CONSTANT)) {
            String val = name.substring(1);
            val = Integer.toString(Integer.parseInt(val, 2));
            childNode = new Node(val, 0);
            currNode.addChild(childNode);
        }
        else if (index != -1) {
            String val =  tokens.currVal() + "_val";
            childNode = new Node(val, 0);
            childNode.setPoint(index);
            currNode.addChild(childNode);
        } else if(functions.containsFunc(name) && tokens.getNextType().equals(ListOfTokens.OPEN_CAST)){
            Node callNode = callFunction(name, currNode, vars);
            if (callNode == null || !callNode.equals(currNode))
                return null;
        }
        else return parseError("Error while parsing value " + tokens.currVal());

        return currNode;
    }
}
