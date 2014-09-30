package merger.test;

import merger.Condition;
import org.fest.util.Strings;

public class IsNotEmpty implements Condition<String> {
    @Override
    public boolean is(String obj1, String obj2) {
        return Strings.isNullOrEmpty(obj2);
    }
}
