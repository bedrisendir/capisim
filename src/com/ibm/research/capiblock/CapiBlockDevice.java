package com.ibm.research.capiblock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;

public class CapiBlockDevice {
	private static final Integer capacity = Integer.valueOf(System.getProperty("capi.capacity.blocks"));
	// TODO HASHMAP HERE WITH DEVICE
	private static final AtomicInteger chunk_id = new AtomicInteger(0);
	private static CapiBlockDevice instance;
	public static final int BLOCK_SIZE = 4096;

	public static CapiBlockDevice getInstance() {
		if (instance == null) {
			instance = new CapiBlockDevice();
				if(capacity==null){
					System.err.println("Please define capi.capacity.blocks env variable");
					System.exit(0);
				}
		}
		return instance;
	}

	public Chunk openChunk(final String path) throws IOException {
		return openChunk(path, 0);
	}

	@SuppressWarnings("resource")
	public synchronized Chunk openChunk(String s, int maxRequests) throws IOException {
		RandomAccessFile f;
		boolean flag = false;
		if (!new File(s).exists()) {
			flag = true;
		}
		f = new RandomAccessFile(s, "rws");
		if (flag) {
			f.setLength(BLOCK_SIZE * capacity);
		}
		Chunk retval = new Chunk(instance, chunk_id.getAndIncrement(), f.getChannel());
		//System.out.println("[CAPISIM = ]"+capacity);
		return retval;
	}
}
