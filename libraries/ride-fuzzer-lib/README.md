# Fuzzer Test Util Library

This library is a set of classes that allows users to fuzz various portions of their REST calls for fault testing.  When utilized the classes will throw all fuzzed data sets at the target and ensure that a 4xx response is returned.

There is currently one active fuzzer library:

- Metadata Fuzzer - Used to fuzz the json payload of a REST API call.

There are a number of Work in progress fuzzer libraries: 
- Path Fuzzer - used to fuzz arbitrary urls.
- Header Fuzzer - used to fuzz arbitrary header
- (Coming Soon) Query Param Fuzzer - used to fuzz arbitrary query parameters.

### Basic Usage

There are 3 main things that need to be added to any test that uses the fuzzer lib

* TargetService name
* The TestNG Factory annotation to test methods
* The test method needs to return at the minimum an Object array.  

The example code below shows how to use each one in practice.


#### Fuzz metadata

This class constructor takes one argument - an entity instance.

```


public class FuzzArticleMetadata_IT {

    private static Base base = Base.INSTANCE;

    @Factory
    public MetadataFuzzer[] testObjectMetadata_IT() throws Exception{
        String objId = base.getId();
        MyCustomModelObject myObj = new MyCustomModelObject(false, objId, UUID.randomUUID().toString());
        return new MetadataFuzzer[]{
            new MetadataFuzzer(myObj)
        };
    }

}

```

### Point of Contact

tedcasey@adobe.com


### Future

Future refinements include smarter fuzzing, determining the proper response for any type of fuzz target and data.
