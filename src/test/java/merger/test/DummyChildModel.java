package merger.test;

import merger.Merge;

/*
 * This class has its own mergeable fields and those in its parent.
 *
 * It should be mergeable.
 */
public class DummyChildModel extends DummyModel {
    @Merge
    String mergeableChildField;
    String nonMergeableChildField;
}
