package merger.test;

import merger.Merger;
import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class MergeTest {

    @Test
    public void test_mergeableFields() {

        // Create the mergeable objects
        DummyModel m1 = new DummyModel();
        m1.mergeableField = "1";

        DummyModel m2 = new DummyModel();
        m2.mergeableField = "2";

        // Merge
        DummyModel result = Merger.merge(m1, m2);

        // Verify merge
        assertThat(result).isNotNull();
        Assertions.assertThat(result.mergeableField).isEqualTo("2");

    }

    @Test
    public void test_nonMergeableFields() {

        // Create the mergeable objects
        DummyModel m1 = new DummyModel();
        m1.nonMergeableField = "3";

        DummyModel m2 = new DummyModel();
        m2.nonMergeableField = "4";

        // Merge
        DummyModel result = Merger.merge(m1, m2);

        // Verify merge
        assertThat(result).isNotNull();
        Assertions.assertThat(result.nonMergeableField).isEqualTo("3");

    }


    @Test
    public void test_childClassFieldsAreMergeable() {
        DummyChildModel m1 = new DummyChildModel();
        m1.mergeableChildField = "mergeable";

        DummyChildModel m2 = new DummyChildModel();
        m2.mergeableChildField = "mergeable2";

        // Merge
        DummyChildModel result = Merger.merge(m1, m2);

        // Verify merge
        assertThat(result).isNotNull();
        assertThat(result.mergeableChildField).isEqualTo("mergeable2");

    }

    @Test
    public void test_childClassNonMergeableFields() {
        DummyChildModel m1 = new DummyChildModel();
        m1.nonMergeableChildField = "notMergeable";

        DummyChildModel m2 = new DummyChildModel();
        m2.nonMergeableChildField = "notMergeable2";

        // Merge
        DummyChildModel result = Merger.merge(m1, m2);

        // Verify merge
        assertThat(result).isNotNull();
        assertThat(result.nonMergeableChildField).isEqualTo("notMergeable");
    }

    @Test
    public void test_parentClassFieldsAreMergeable() {
        DummyChildModel m1 = new DummyChildModel();
        m1.mergeableField = "1";

        DummyChildModel m2 = new DummyChildModel();
        m2.mergeableField = "2";

        // Merge
        DummyChildModel result = Merger.merge(m1, m2);

        // Verify merge
        assertThat(result).isNotNull();
        assertThat(result.mergeableField).isEqualTo("2");
    }

    @Test
    public void test_parentClassNonMergeableFields() {
        DummyChildModel m1 = new DummyChildModel();
        m1.nonMergeableField = "1";

        DummyChildModel m2 = new DummyChildModel();
        m2.nonMergeableField = "2";

        // Merge
        DummyChildModel result = Merger.merge(m1, m2);

        // Verify merge
        assertThat(result).isNotNull();
        assertThat(result.nonMergeableField).isEqualTo("1");

    }

    @Test(expected = IllegalArgumentException.class)
    public void test_noMergeableFieldsThrowsException() {
        DummyNoMergeableFieldsModel d1 = new DummyNoMergeableFieldsModel();
        DummyNoMergeableFieldsModel d2 = new DummyNoMergeableFieldsModel();
        DummyNoMergeableFieldsModel result = Merger.merge(d1, d2); // should throw as no merger is found
    }

    @Test
    public void test_childNoMergeableFieldShouldMergeParent() {
        DummyNoMergeableFieldsChildModel m1 = new DummyNoMergeableFieldsChildModel();
        m1.mergeableField = "1";

        DummyNoMergeableFieldsChildModel m2 = new DummyNoMergeableFieldsChildModel();
        m2.mergeableField = "2";

        DummyNoMergeableFieldsChildModel result = Merger.merge(m1, m2);

        // Verify merge
        assertThat(result).isNotNull();
        assertThat(result.mergeableField).isEqualTo("2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_hetrogeneousTypesNotMergeable() {
        DummyModel m1 = new DummyModel();
        m1.mergeableField = "1";

        DummyModel m2 = new DummyChildModel();
        m2.mergeableField = "2";

        // Should throw - can't merge two different types
        DummyModel result = Merger.merge(m1, m2);

    }

    @Test
    public void test_mergeableGenericFields() {
        DummyGenericModel m1 = new DummyGenericModel();
        m1.mergeableListField = Arrays.asList("1");

        DummyGenericModel m2 = new DummyGenericModel();
        m2.mergeableListField = Arrays.asList("2");

        DummyGenericModel result = Merger.merge(m1, m2);

        // Verify merge
        assertThat(result).isNotNull();
        assertThat(result.mergeableListField).isEqualTo(Arrays.asList("2"));
    }

    // TODO Verify restrictions of where annotation can be placed
    // TODO Verify deeper hierachy with gaps
}
