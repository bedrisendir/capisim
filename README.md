## Summary
capisim emulates capiblock API by using RandomAccessFile which replaces CAPI-Flash address space.
O_SYNC flag is used to open the RandomAccessFile. So, each operation on the file will be synchronous. 
For this reason, you might want to use tmpfs filesystem/RamDisk to speed-up operations. Otherwise, it will be slow.

 
## Building 
Simply run ant on top level. This will generate the capiblock.jar in capisim folder.
```
ant 
```


## Testing
Test cases are implemented in src/test and includes a separate build file. **Please see L8 to L22 of src/test/build.xml for configurables.**

#### Test capisim
1. Set variables 

2. 
```
ant testsim
```

#### Test original capiblock.jar
1. copy original capiblock.jar in capisim/
2. Set variables 

3. 
```
ant testcapi
```

## Utility Functions
If you are not testing recovery module. You might want to clean book-keeping blocks of CAPI-Flash Commitlog before starting Cassandra. 
In order to achieve this, I included a simple program in `capisim/src/test`.

#### Clean capisim file
1. Set variables 

2. 
```
ant cleansim
```


#### Clean original CAPI-Flash address space
1. copy original capiblock.jar in capisim/
2. Set variables 

3.
```
ant cleancapi
```