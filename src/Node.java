import java.util.ArrayList;
import java.util.Collections;

public class Node {
    private Node parent;
    private final ArrayList<Node> children;
    private final String value;
    private int index;
    private final int maxChildren;
    private int childrenCount;
    private int point;
    private static int loopCount = 0;
    private boolean afterElse = false;
    private static int divCount = 0;
    private static int andCount = 0;
    private boolean isParam = false;
    private static int curr_param_count = 0;
    private static int forCycleCount = 0;

    public Node(String value, int maxChildren) {
        this.children = new ArrayList<>(1);
        this.index = 0;
        this.childrenCount = 0;
        this.value = value;
        this.maxChildren = maxChildren;
    }

    private boolean codeGenerate(StringBuilder code) {
        if (value.equals("return")) {
            code.append("\tpop eax ;here is the result\n")
                    .append("\tmov esp, ebp\t;restore ESP\n")
                    .append("\tpop ebp\t;restore old EBP\n")
                    .append("\tret " + curr_param_count + "\n\n");
        }
        else if(value.equals("for")){
            forCycleCount ++;
        }
        else if(value.equals("CONTINUE")){
            if(forCycleCount < 1){
                System.out.println("Error while generation code! Keyword 'continue' is out of the cycle!");
                return false;
            }
            code.append("jmp _for_start" + forCycleCount + "\t;continue\n");
        }else if(value.equals("BREAK")){
            if(forCycleCount < 1){
                System.out.println("Error while generation code! Keyword 'break' is out of the cycle!");
                return false;
            }
            code.append("jmp _for_end" + forCycleCount + "\t;break\n");
        }
        else if(value.matches("for_start[0-9]")){
            code.append("_" + value + ":\n");
        }
        else if(value.matches("[0-9]for_\\{")){
            forCycleCount --;
            code.append("\tjmp _for_start" + value.charAt(0) + "\n");
            code.append("_for_end" + value.charAt(0) + ":\n");
        }
        else if(value.matches("less[0-9]")){
            code.append("\tpop eax\t;expression\n")
                    .append("\tcmp eax, ebx\n")
                    .append("\tjle _for_end" + value.charAt(4) + "\t;if value less than expr\n\n");
        }
        else if(value.matches("more[0-9]")){
            code.append("\tpop eax\t;expression\n")
                    .append("\tcmp ebx, eax\n")
                    .append("\tjle _for_end" + value.charAt(4) + "\t;if value more than expr\n\n");
        }
        else if(value.equals("minusOne")){
            code.append("\tsub ebx, 1\n");
        }
        else if(value.equals("plusOne")){
            code.append("\tadd ebx, 1\n");
        }
        else if (value.equals("&&")) {
            andCount ++;
            code.append("\tpop ECX\n")
                    .append("\tpop EAX\n")
                    .append("\tcmp eax, 0   ; check if e1 is true\n")
                    .append("\tjne _clause" + andCount + "\t;e1 is not 0, evaluate clause 2\n")
                    .append("\tjmp _end_and" + andCount + "\n")
                    .append("\t_clause" + andCount + ":\n")
                    .append("\t\tcmp ecx, 0 ; check if e2 is true\n")
                    .append("\t\tmov eax, 0\n")
                    .append("\t\tsetne al\n\n")

                    .append("\t_end_and" + andCount + ":\n")
                    .append("\t\tpush eax\n\n");

        }
        else if (value.equals("*")) {

            code.append("\tmov edx, 0\n")
                    .append("\tpop ECX\n")
                    .append("\tpop EAX\n")
                    .append("\timul ECX\n")
                    .append("\tpush EAX\n\n");

        }
        else if (value.equals("%")) {

            code.append("\tpop ECX\n")
                    .append("\tpop EAX\n")
                    .append("\tmov EBX, EAX\n")
                    .append("\tshr EBX, 31\n")
                    .append("\tcmp EBX, 0\n")

                    .append("\tje _D" + divCount + "\n")
                    .append("\tmov edx, 0ffffffffh\n")
                    .append("\tjmp _D" + (divCount + 1) + "\n")
                    .append("_D" + divCount + ":\n")
                    .append("\tmov edx, 0\n")
                    .append("_D" + (divCount + 1) + ":\n")

                    .append("\tidiv ECX\n")
                    .append("\tpush EDX\n\n");
            divCount += 2;
        }
        else if (value.equals("/")) {

            code.append("\tpop ECX\n")
                    .append("\tpop EAX\n")
                    .append("\tmov EBX, EAX\n")
                    .append("\tshr EBX, 31\n")
                    .append("\tcmp EBX, 0\n")

                    .append("\tje _D" + divCount + "\n")
                    .append("\tmov edx, 0ffffffffh\n")
                    .append("\tjmp _D" + (divCount + 1) + "\n")
                    .append("_D" + divCount + ":\n")
                    .append("\tmov edx, 0\n")
                    .append("_D" + (divCount + 1) + ":\n")

                    .append("\tidiv ECX\n")
                    .append("\tpush EAX\n\n");
            divCount += 2;
        }
        else if (value.equals("-")) {

            code.append("\tpop EBX\n")
                    .append("\tneg EBX\n")
                    .append("\tpush EBX\n\n");

        }
        else if (value.equals("+")) {

            code.append("\tpop EAX\n")
                    .append("\tpop EBX\n")
                    .append("\tadd EAX, EBX\n")
                    .append("\tpush EAX\n");

        }
        else if (value.equals("if_pop")) {
            loopCount += 2;
            code.append("pop eax\t;if\n" +
                    "cmp eax, ");
        }
        else if (value.matches("if_with_[0-9]+") || value.matches("if_with_\\[ebp-[0-9]+]")) {
            code.append(value.substring(8) + "\n");
        }
        else if (value.equals("if_eq")) {
            code.append("jne _L" + loopCount + "\n\n");
        }
        else if (value.equals("if_less")) {
            code.append("jge _L" + loopCount + "\n\n");
        }
        else if (value.equals("if_more")) {
            code.append("jle _L" + loopCount + "\n\n");
        }
        else if (value.equals("if_neq")) {
            code.append("je _L" + loopCount + "\n\n");
        }
        else if (value.equals("if_else")) {
            code.append("\tjmp _L" + (loopCount + 1) + "\n_L" + loopCount + ":\n");
        }
        else if (value.equals("if_end")) {
            code.append("_L" + loopCount + ":\n");
        }
        else if (value.equals("else_end")) {
            code.append("_L" + (loopCount + 1) + ":\n");
        }
        else if (value.matches("[0-9]+")) {
            code.append("\tpush ").append(value).append("\n");
        }
        else if (value.matches("[a-zA-Z_][a-zA-Z_0-9]*_var")) {
            if (this.childrenCount > 0) {
                code.append("\tpop " + "[ebp-").append(point).append("];\t" + value +"\n");
            }
            if(isParam){
                code.append("\tmov eax, [ebp+" + (point-4) + "]\n");
                code.append("\tmov [ebp-" + point + "], eax\t;" + value + "\n");
            }
        }
        else if (value.matches("[a-zA-Z_][a-zA-Z_0-9]*_val")) {
            code.append("\tpush [ebp-").append(point).append("]     ;").append(value).append("\n");
        }
        else if (value.matches("[a-zA-Z_][a-zA-Z_0-9]*_val_for")) {
            code.append("\tmov ebx, [ebp-" + point + "]\n");
        }
//        else if(afterElse && value.matches("\\{")) {
//            code.append("_L" +  (loopCount +1) + ":\n");
//        }
        else if(value.matches("[a-zA-Z_][a-zA-Z_0-9]*_name")){
            if(value.matches("main_name"))
                curr_param_count = 0;
            else {
                ArrayList<Node> params = parent.children.get(1).children;
                Collections.reverse(params);
            }
            code.append(value.substring(0, value.lastIndexOf("_")) + " proc\n")
                    .append("\tpush ebp\n\tmov ebp, esp\n");
        }
        else if(value.matches("[a-zA-Z_][a-zA-Z_0-9]*_call")){
            code.append("call " + value.substring(0, value.lastIndexOf("_")) + "\n")
                    .append("\tpush eax\n");

        }
        else if(value.matches("[a-zA-Z_][a-zA-Z_0-9]*_body")){
            code.append(value.substring(0, value.lastIndexOf("_")) + " endp\n");
        }
        else if(value.matches("[a-zA-Z_][a-zA-Z_0-9]*_param")){
            curr_param_count = this.children.size();
            code.append(";" + value + " \n");
        }
        return true;
    }
    public void isParam(){
        isParam = true;
    }
    public int getChildrenCount(){
        return childrenCount;
    }
    public String getValue() {
        return this.value;
    }
    private void setParent(Node p) {
        this.parent = p;
    }
    public Node getParent() {
        return this.parent;
    }
    public void addChild(Node ch) {
        ch.setParent(this);
        childrenCount++;
        this.children.add(ch);
    }
    public boolean hasMaxChildren() {
        return maxChildren == childrenCount;
    }
    public void replaceChild(Node child, Node newChild) {
        int i = this.children.indexOf(child);
        this.children.set(i, newChild);
        newChild.setParent(this);
    }
    public void setMainLast(){
        for (Node childNode :
                children) {
            if(childNode.getValue().equals("main_func")) {
                children.remove(childNode);
                children.add(childNode);
                return;
            }
        }
    }
    public void switchAfterElse(){
        afterElse = !afterElse;
    }
    public Node removeLastChild() {
        childrenCount--;
        return this.children.remove(childrenCount);
    }
    public void setPoint(int val){
        point = (val + 3) * 4;
    }
    public Node getTailChild(int place) {
        int index = this.children.size() - place;
        return this.children.get(index);
    }

    public static boolean startGenerate(Node node, StringBuilder code){
        Node curr = node;
        boolean done = false;
        while (!done) {
            if (curr.hasNextChild()) {
                curr = curr.getChild();
            } else {
                if(!curr.codeGenerate(code)) return false;
                if (curr == node) done = true;
                else curr = curr.getParent();
            }
        }
        return true;
    }
    private Node getChild() {
        Node child = this.children.get(this.index);
        index++;
        return child;
    }
    private boolean hasNextChild() {
        return this.index < this.children.size();
    }
}
