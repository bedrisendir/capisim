capisim implements same method signatures and mimics the behavior of original capiblock API. 

### Steps to build Cassandra with capisim.
1. clone bedrisendir/cassandra/aug1-capiflashcommitlog

2. compile/build capisim and create the capiblock.jar. Remove the original capiblock.jar if it exists in cassandra/lib folder.


3. copy created capiblock.jar to cassandra/lib. 


4. build cassandra (simply run ant)


5. set parameters in cassandra.yaml. I am revising some parameters in my next commit. But for now, you can set the parameters below. For example,
    ```
    commitlog_type: CAPIFlashCommitLog
    capiflashcommitlog_devices:
        - /dev/shm/test.txt
    capiflashcommitlog_start_offset: 0
    capiflashcommitlog_number_of_segments: 128
    capiflashcommitlog_segments_size_in_blocks: 64000
    capiflashcommitlog_buffer_allocator_type : FixedSizeAllocationStrategy
    capiflashcommitlog_preallocated_buffer_size_in_blocks : 16
    capiflashcommitlog_preallocated_buffer_count : 256
    capiflashcommitlog_async_calls_per_chunk : 128
    capiflashcommitlog_number_of_chunks : 4
    capiflashcommitlog_chunkmanager_type : AsyncSemaphoreChunkManager 
    capiflashcommitlog_number_of_concurrent_writeBlock : 128
    ```
6. create and set property variable in `cassandra/conf/jvm.options` for capisim to use. This will pre-allocate the file, if file not exists. 
    ```
    -Dcapi.capacity.blocks=10000000
    ```
7. run `ant cleansim` in `capisim/src/test`. This will clear book-keeping blocks by putting 0 to each block. Otherwise, Cassandra will try to recover the log entries. Please, make sure that number of defined book-keeping blocks and start address matches with `capisim/src/test/build.xml`. You have to perform this step everytime you start Cassandra.
Example:
    ```
    cleansim:
        [java] Cleared 128 starting from address=0
    ```

8. run cassandra `cassandra/bin/cassandra -f`




