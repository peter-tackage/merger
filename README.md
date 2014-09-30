Merger
======

Merger is a demonstration of Java annotation processing that allows two arbitrary objects of the same class to combine
their values into a single resultant object. The fields to be merged are determined by the use of the @Merge field annotation.

For example:

    public class Model {
        @Merge
        String name; // is merged
        
        String password; // is not merged       
    }
    
Usage:

    Model m1 = new Model();
    Model m2 = new Model();
    
    // ... modify values of m1,m2
    
    Model merged = Merger.merge(m1,m2);
    
Details
=======
The annotation processor creates a class for each class having an @Merge field annotation and defines a method that 
assigns the value of each annotated field in the primary object to that of the secondary object. In usage, this method 
is invoked by the Merger.merge() method.

The implementation is based around the approach used by Jack Wharton's ![ButterKnife](https://github.com/JakeWharton/butterknife) 
library and uses raw String concatenation to create the generated classes. 
