package merger.test;

import merger.Merge;

public class DummyModel {
    @Merge
    String mergeableField;
    String nonMergeableField;
}
