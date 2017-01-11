###Building Cassandra CAPI-Rowcache with CAPISIM

1. clone hhorii/trunk

2. compile/build capisim and copy generated capiblock.jar in cassandra/lib

3. compile cassandra

4. in cassandra/conf/jvm.options add following properties. 
  ```
  -Dcapi.devices=/dev/shm/test.txt:0:30 #uses 30GB for CAPI Row-Cache
  -Dcapi.capacity.blocks=10000000 
  ```
5. replace jna.4.0 jar with 4.2.2 in cassandra/lib
6. -Xss per thread stack size might cause an exception in ppc64le. I set it to 512 in conf/jvm.options

7. enable capi rowcache in cassandra/conf/cassandra.yaml. not sure **row_cache_size_in_mb** defines the size or any value bigger than 0 is ok?
  `row_cache_class_name: org.apache.cassandra.cache.CapiRowCacheProvider`
  `row_cache_size_in_mb: 24000`
8. start cassandra. you shoud be able to see a message like below..
```
capicache: device=/dev/shm/test.txt, start=0, size=30.0gb
```
