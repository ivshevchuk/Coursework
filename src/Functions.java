import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Functions{
    private final Map<String, Integer> allFunc = new HashMap<>();
    private final ArrayList<String> defNames = new ArrayList<>();//has a body
    private final ArrayList<String> activatedFunc = new ArrayList<>();//has a body

    public boolean foundFunc(String name, int arguments){
        if(allFunc.containsKey(name)){
            return allFunc.get(name) == arguments;
        }
        else {
            allFunc.put(name, arguments);
            return true;
        }
    }

    public boolean functionBody(String name, int arguments){
        if(defNames.contains(name))
            return false;
        else
            return defNames.add(name);
    }

    public boolean containsFunc(String name){
        return allFunc.containsKey(name);
    }

    public boolean activateFunction(String name, int arguments){
        activatedFunc.add(name);
        return allFunc.containsKey(name) && allFunc.get(name) == arguments;
    }

    public boolean containsMain() {
        return defNames.contains("main");
    }

    public String finalCheck() {
        for (String currName : activatedFunc) {
            if (!defNames.contains(currName)) {
                return currName;
            }
        }
        return null;
    }
}