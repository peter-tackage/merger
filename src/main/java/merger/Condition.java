package merger;

public interface Condition<T> {

    public boolean is(T obj1, T obj2);
}
