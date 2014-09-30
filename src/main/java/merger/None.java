package merger;

public class None implements Condition {
    @Override
    public boolean is(Object obj1, Object obj2) {
        return false;
    }
}
