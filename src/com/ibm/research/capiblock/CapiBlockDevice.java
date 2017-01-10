package com.ibm.research.capiblock;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CapiBlockDevice {
	private static final Integer capacity = Integer.valueOf(System.getProperty("capi.capacity.blocks"));
	static Map<String, RandomAccessFile> files = new HashMap<String, RandomAccessFile>();
	static Set<Integer> chunkids = new HashSet<Integer>();
	private static final AtomicInteger chunk_id = new AtomicInteger(0);
	private static CapiBlockDevice instance;
	public static final int BLOCK_SIZE = 4096;

	public static CapiBlockDevice getInstance() {
		if (instance == null) {
			instance = new CapiBlockDevice();
			if (capacity == null) {
				System.err.println("Please define capi.capacity.blocks env variable");
				System.exit(0);
			}
		}
		return instance;
	}

	public Chunk openChunk(final String path) throws IOException {
		return openChunk(path, 0);
	}

	public Chunk openChunk(String file, int maxRequests) throws IOException {
		synchronized (CapiBlockDevice.getInstance()) {
			RandomAccessFile f = null;
			if (files.containsKey(file)) {
				f = files.get(file);
			} else {
				f = new RandomAccessFile(file, "rws");
				f.setLength((long) BLOCK_SIZE * capacity);
			}
			Chunk retval = new Chunk(instance, chunk_id.getAndIncrement(), f.getChannel(), file);
			chunkids.add(retval.chunk_id);
			return retval;
		}
	}
}
