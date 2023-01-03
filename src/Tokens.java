import java.util.ArrayList;
import java.util.EnumSet;
import java.util.regex.Pattern;

class Tokens {
    public static EnumSet<ListOfTokens> typeKeyword = EnumSet.of(ListOfTokens.KEYWORD_FLOAT, ListOfTokens.KEYWORD_INT);
    public final ArrayList<String> tokens = new ArrayList<>(); //ArrayList of tokens in current row
    public final ArrayList<ListOfTokens> types = new ArrayList<>(); //ArrayList of their types
    private int index = -1;
    private String prev = "";

    public boolean setPair(String curr) {
        index++;
        if (prev.equals("0") && getType(curr).equals(ListOfTokens.INT_BIN_CONSTANT)) {
            index--;
        }
        tokens.add(index, curr);
        types.add(index, getType(curr));
        System.out.print("" + types.get(index) + ", ");
        prev = curr;
        return types.get(index) != ListOfTokens.UNKNOWN;
    }

    private ListOfTokens getType(String next) {
        for (ListOfTokens id : ListOfTokens.values()) {
            if (Pattern.matches(id.pattern, next)) {
                return id;
            }
        }
        return ListOfTokens.UNKNOWN;
    }

    public void indexMinus(int i) {
        index = index - i;
    }

    public ListOfTokens getFirstType() {
        index = 0;
        return types.get(index);
    }

    public ListOfTokens getNextType() {
        index++;
        return types.get(index);
    }

    public String currVal() {
        return tokens.get(index);
    }

    public boolean hasNext(){
        return types.size() > index + 1;
    }
}
