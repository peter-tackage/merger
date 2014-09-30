package merger;

import merger.internal.MergeProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Merger {

    private static boolean DEBUG = true;

    static final String TAG = "Merger";
    static final Map<Class<?>, Method> MERGERS = new LinkedHashMap<Class<?>, Method>();
    static final Method NO_OP = null;

    @SuppressWarnings("unchecked")
    public static <T> T merge(T obj1, T obj2) {
        Class<?> targetClass = obj2.getClass();
        try {
            if (DEBUG) Logger.getLogger(TAG).info("Looking up object merger for " + targetClass.getName());
            // Call the merger and return the result
            Method merger = findMergerForClass(targetClass);
            if(merger != null) {
                return (T) merger.invoke(null, obj1, obj2);
            } else {
                throw new IllegalArgumentException("Unable to merge objects:" + obj1 + "and " + obj2 + " (no mergeable fields)");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            Throwable t = e;
            if (t instanceof InvocationTargetException) {
                t = t.getCause();
            }
            throw new RuntimeException("Unable to merge objects for " + obj1 + "and " + obj2, t);
        }
    }

    private static Method findMergerForClass(Class<?> cls) throws NoSuchMethodException {
        Method mergeMthd = MERGERS.get(cls);
        if (mergeMthd != null) {
            if (DEBUG) Logger.getLogger(TAG).info("HIT: Cached in merger map.");
            return mergeMthd;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            if (DEBUG) Logger.getLogger(TAG).info("MISS: Reached framework class. Abandoning search.");
            return NO_OP;
        }
        try {
            if (DEBUG) Logger.getLogger(TAG).info("Searching for merger class: " + clsName + MergeProcessor.SUFFIX);
            Class<?> mergerCls = Class.forName(clsName + MergeProcessor.SUFFIX);
            mergeMthd = mergerCls.getMethod("merge", cls, cls);
            if (DEBUG) Logger.getLogger(TAG).info("HIT: Class loaded merger class.");
        } catch (ClassNotFoundException e) {
            if (DEBUG) Logger.getLogger(TAG).info("Not found. Trying superclass " + cls.getSuperclass().getName());
            mergeMthd = findMergerForClass(cls.getSuperclass());
        }
        MERGERS.put(cls, mergeMthd);
        return mergeMthd;
    }


}
