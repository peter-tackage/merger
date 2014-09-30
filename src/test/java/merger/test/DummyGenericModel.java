package merger.test;

import merger.Merge;

import java.util.List;

/**
 * This class has a mergeable field with a generic parameter.
 *
 * It should be mergeable.
 */
public class DummyGenericModel {
    @Merge
    List<String> mergeableListField;
}
