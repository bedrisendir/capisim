package com.ibm.research.capiblock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CapiBlockDevice {
	RandomAccessFile f;
	static CapiBlockDevice dev = new CapiBlockDevice();

	public static CapiBlockDevice getInstance() {
		return dev;
	}

	public synchronized Chunk openChunk(String s) throws IOException {
		boolean flag = false;
		if (!new File(s).exists()) {
			flag = true;
		}
		f = new RandomAccessFile(s, "rws");
		if (flag) {
			f.setLength(1024 * 1024 * 1024 * 4);
		}
		Chunk retval = new Chunk(f.getChannel());
		if (flag) {
			init(retval);
		}
		return retval;
	}

	// initialize few blocks from start to zero
	private void init(Chunk ch) {
		//zeroes first 1024 block if number of bookkeeping segments bigger than 1024 need to explicitly zero the blocks
		for (int i = 0; i < 1024; i++) {
			ByteBuffer b = ByteBuffer.allocate(4096);
			b.putLong(0);
			try {
				ch.writeBlock(i, 1, b);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
