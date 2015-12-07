package capiblocksim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CapiBlockDevice {
	RandomAccessFile f;
	FileChannel inChannel;
	static CapiBlockDevice dev = new CapiBlockDevice();

	public static CapiBlockDevice getInstance() {
		return dev;
	}

	public Chunk openChunk(String s) throws IOException {
		boolean flag = false;
		if (!new File(s).exists()) {
			flag = true;
		}
		f = new RandomAccessFile(s, "rws");
		f.setLength(1024 * 1024 * 1024 * 4);
		inChannel = f.getChannel();
		Chunk retval = new Chunk(inChannel);
		if (flag) {
			init(retval);
		}
		return retval;
	}

	//initialize few blocks from start to zero
	private void init(Chunk ch) {
		for (int i = 0; i < 512; i++) {
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
