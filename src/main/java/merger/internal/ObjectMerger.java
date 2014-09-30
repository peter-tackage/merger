package merger.internal;

import java.util.ArrayList;
import java.util.List;

public class ObjectMerger {

    private String parentMergerClassName;
    private final String classPackage; // The package name of generated merger class
    private final String className; // The class name of the generated merger
    private final String targetClass; // The class name we are merging (target)
    private final List<String> fields; // The fields name we are merging

    ObjectMerger(String classPackage, String className, String targetClass) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.fields = new ArrayList<String>();
    }

    String brewJava() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code by Merger. Do not modify!\n");
        if(classPackage.length() > 0) builder.append("package ").append(classPackage).append(";\n\n");
        builder.append("public class ").append(className).append(" {\n");
        emitMerges(builder);
        builder.append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    private void emitMerges(StringBuilder builder) {
        builder.append(" public static ").append(targetClass).append(" merge(")
                .append("final ").append(targetClass).append(" obj1,")
                .append("final ").append(targetClass).append(" obj2)")
                .append("{\n");

        if(parentMergerClassName != null) {
            builder.append(parentMergerClassName).append(".merge(obj1,obj2);");
        }
        // Loop over each mergeable field and emit it.
        for (String name : fields) {
            builder.append("obj1.").append(name).append(" = ").append("obj2.").append(name).append(";\n");
        }
        builder.append("return obj1;\n");
        builder.append(" }\n");
    }

    // The fully qualified name of the target generated class
    public String getFqcn() {
        return classPackage.length() == 0 ? className : classPackage + "." + className;
    }

    public void addField(String field) {
        this.fields.add(field);
    }

    public void setParentMerger(String parentMergerClass) {
        this.parentMergerClassName = parentMergerClass;
    }
}
