public class GenCode {
    public GenCode() {
    }

    public String generationCode(Node node) {
        node.setMainLast();
        StringBuilder code = new StringBuilder();
        code.append(".386\n")
                .append(".model flat, stdcall\n")
                .append("option casemap :none\n\n")

                .append("include D:\\masm32\\include\\kernel32.inc\n")
                .append("include D:\\masm32\\include\\user32.inc\n\n")

                .append("includelib D:\\masm32\\lib\\kernel32.lib\n")
                .append("includelib D:\\masm32\\lib\\user32.lib\n\n")

                .append("main PROTO\n\n")

                .append(".data\n")
                .append("msg_title db \"Result\", 0\n")
                .append("buffer db 128 dup(?)\n")
                .append("format db \"%d\",0\n\n")

                .append(".code\n")
                .append("start:\n")
                .append("\tinvoke main")
                .append("\n\tinvoke wsprintf, addr buffer, addr format, eax\n")
                .append("\tinvoke MessageBox, 0, addr buffer, addr msg_title, 0\n")
                .append("\tinvoke ExitProcess, 0\n\n");

        boolean success = Node.startGenerate(node, code);
        if(!success) return null;

        code.append("main ENDP\n")
                .append("END start\n");
        return code.toString();
    }
}
