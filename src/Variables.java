import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Variables {

    private final Map<String, Boolean> varList = new HashMap<>();
    private static final ArrayList<String> variables = new ArrayList<>();
    private Variables parrent;
    private final ArrayList<Variables> children = new ArrayList<>();

    public boolean contains(String var) {
        return varList.containsKey(var);
    }

    public boolean totalContains(String var) {
        return variables.contains(var);
    }

    public int getPoint(String var) {
        return variables.lastIndexOf(var);
    }

    public void addVar(String var) {
        varList.put(var, false);
        variables.add(var);
    }

    public boolean addVal(String var) {
        Variables currVars = this;
        try {
            while (!currVars.varList.containsKey(var)) {
                currVars = currVars.parrent;
            }
            currVars.varList.put(var, true);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public int getVal(String var) {
        return variables.lastIndexOf(var);
    }

    public Variables openBrace() {
        Variables child = new Variables();
        this.children.add(child);
        child.parrent = this;
        return child;
    }

    public Variables skipCurBrace(int count) {
        int varIndex = variables.size() - 1;
        for (int i = 0; i < count; i++) {
            variables.remove(varIndex);
            varIndex--;
        }
        int index = this.parrent.children.size() - 1;
        this.parrent.children.remove(index);
        return this.parrent;
    }

    public Variables closeBrace() {
        for (int i = 0; i < varList.size(); i++) {
            variables.remove(variables.size() - 1);
        }
        return this.parrent;
    }
}
