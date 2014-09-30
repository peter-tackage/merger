package merger.internal;

import merger.Merge;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes("merger.Merge")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MergeProcessor extends AbstractProcessor {

    // Class name suffix for generating merging classes
    public static final String SUFFIX = "$$ObjectMerger";

    private static final boolean DEBUG = false;

    private Elements elementUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env) {

        Map<TypeElement, ObjectMerger> targetClassMap = findAndParseTargets(env);

        for (Map.Entry<TypeElement, ObjectMerger> entry : targetClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            ObjectMerger objectMerger = entry.getValue();

            try {
                debug(typeElement, "Generating class: %s ", objectMerger.getFqcn());
                JavaFileObject jfo = filer.createSourceFile(objectMerger.getFqcn(), typeElement);
                Writer writer = jfo.openWriter();
                writer.write(objectMerger.brewJava());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                error(typeElement, "Unable to write merger for type %s: %s", typeElement, e.getMessage());
            }
        }

        return true;
    }

    private Map<TypeElement, ObjectMerger> findAndParseTargets(RoundEnvironment env) {
        Map<TypeElement, ObjectMerger> targetClassMap = new LinkedHashMap<TypeElement, ObjectMerger>();

        // Process each @Merge element
        // TODO Is this filter necessary? (there's an annotation on the class)
        for (Element element : env.getElementsAnnotatedWith(Merge.class)) {
            try {
                parseObjectMerger(element, targetClassMap);
            } catch (Exception e) {
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                error(element, "Unable to generate object merger class for @Merge.\n\n%s", stackTrace);
            }
        }

        // Try to find a parent merger for each merger.
        for (Map.Entry<TypeElement, ObjectMerger> entry : targetClassMap.entrySet()) {
            // Need to find the nearest parent that has an ObjectMerger
            String parentClassFqcn = findParentFqcn(entry.getKey(), targetClassMap);
            if (parentClassFqcn != null) {
                debug(entry.getKey(), "Setting parent merger to: %s", parentClassFqcn);
                entry.getValue().setParentMerger(parentClassFqcn + SUFFIX);
            }
        }

        return targetClassMap;
    }

    private void parseObjectMerger(Element element, Map<TypeElement,
            ObjectMerger> targetClassMap) {

        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify common generated code restrictions.
        hasError |= isValidForGeneratedCode(Merge.class, "fields", element);

        if (hasError) {
            return;
        }

        // Assemble information on the merge.
        String fieldName = element.getSimpleName().toString();
        ObjectMerger objectMerger = getOrCreateTargetClass(targetClassMap, enclosingElement);
        objectMerger.addField(fieldName);

    }

    private ObjectMerger getOrCreateTargetClass(Map<TypeElement, ObjectMerger> targetClassMap,
                                                TypeElement enclosingElement) {
        ObjectMerger objectMerger = targetClassMap.get(enclosingElement);
        if (objectMerger == null) {
            String targetType = enclosingElement.getQualifiedName().toString();
            String classPackage = getPackageName(enclosingElement);
            String className = getClassName(enclosingElement, classPackage) + SUFFIX;

            debug(enclosingElement, "targetType: %s, classPackage: %s, className: %s", targetType, classPackage, className);

            objectMerger = new ObjectMerger(classPackage, className, targetType);
            targetClassMap.put(enclosingElement, objectMerger);
        }
        return objectMerger;
    }

    private static String getClassName(TypeElement type, String packageName) {
        return packageName.length() == 0 ?
                type.getQualifiedName().toString() :
                type.getQualifiedName().toString().substring(packageName.length() + 1).replace('.', '$');
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private boolean isValidForGeneratedCode(Class<? extends Annotation> annotationClass,
                                            String targetThing, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify field modifiers.
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
            error(element, "@%s %s must not be private or static. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing type.
        if (enclosingElement.getKind() != CLASS) {
            error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing class visibility is not private.
        if (enclosingElement.getModifiers().contains(PRIVATE)) {
            error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        return hasError;
    }

    /** Finds the parent merger type in the supplied set, if any. */
    private String findParentFqcn(TypeElement typeElement, Map<TypeElement, ObjectMerger> parents) {
        TypeMirror type;
        while (true) {
            type = typeElement.getSuperclass();
            if (type.getKind() == TypeKind.NONE) {
                return null;
            }
            typeElement = (TypeElement) ((DeclaredType) type).asElement();
            if (parents.containsKey(typeElement)) {
                String packageName = getPackageName(typeElement);
                return packageName.length() == 0 ?
                        getClassName(typeElement, packageName)
                        : packageName + "." + getClassName(typeElement, packageName);
            }
        }
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    private void debug(Element element, String message, Object... args) {
        if(!DEBUG) return;

        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(NOTE, message, element);
    }

}
