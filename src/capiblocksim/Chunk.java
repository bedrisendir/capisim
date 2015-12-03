package capiblocksim;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Chunk {
	FileChannel inChannel;

	public Chunk(FileChannel n_inChannel) {
		inChannel = n_inChannel;
	}

	public void writeBlock(long start, long blocks, ByteBuffer buf) throws IOException {
		System.err.println(start+" "+ blocks+" "+buf.capacity());
		byte[] arr = new byte[(int) (blocks * 4096)];
		buf.rewind();
		buf.get(arr, 0,(int) blocks * 4096);
		inChannel.position(start * 4096);
		buf.position(0);
		inChannel.write(ByteBuffer.wrap(arr));
	}

	public void readBlock(long start, long blocks, ByteBuffer buf)
			throws IOException {
		ByteBuffer b = ByteBuffer.allocate((int) (blocks * 4096));
		inChannel.position(start * 4096);
		inChannel.read(b);
		buf = b;
	}
	public void close() throws IOException{
		inChannel.close();
	}
}
