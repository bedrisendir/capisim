package com.ibm.research.capiblock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Chunk {
	FileChannel inChannel;

	public Chunk(FileChannel n_inChannel) {
		inChannel = n_inChannel;
	}

	public void writeBlock(long start, long blocks, ByteBuffer buf) throws IOException {
		synchronized (inChannel) {
			byte[] arr = new byte[(int) (blocks * 4096)];
			buf.rewind();
			buf.get(arr, 0, (int) blocks * 4096);
			inChannel.position(start * 4096);
			buf.position(0);
			inChannel.write(ByteBuffer.wrap(arr));
			inChannel.force(true);
		}
	}

	public void readBlock(long start, long blocks, ByteBuffer buf) throws IOException {
		synchronized (inChannel) {
			inChannel.force(true);
			ByteBuffer b = ByteBuffer.allocate((int) (blocks * 4096));
			inChannel.position(start * 4096);
			inChannel.read(b);
			b.rewind();
			buf.put(b);
			buf.position(b.position() - (int) (blocks * 4096));
		}
	}

	public void close() throws IOException {
		inChannel.close();
	}
}
