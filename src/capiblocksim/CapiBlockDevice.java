package capiblocksim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class CapiBlockDevice {
	RandomAccessFile f;
	FileChannel inChannel;
	static CapiBlockDevice dev = new CapiBlockDevice();

	public static CapiBlockDevice getInstance() {
		return dev;
	}

	public Chunk openChunk(String s) throws IOException {
		f = new RandomAccessFile(s, "rws");
		f.setLength(1024 * 1024 * 1024 * 4);
		inChannel = f.getChannel();
		return new Chunk(inChannel);
	}
}
