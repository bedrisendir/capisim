package test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import com.ibm.research.capiblock.CapiBlockDevice;
import com.ibm.research.capiblock.Chunk;

public class TestDriver {

	public static void main(String[] args) {
		// open a device
		CapiBlockDevice dev = CapiBlockDevice.getInstance();
		try {

			if (args.length > 0 && args[0].equals("print")) {
				Chunk ch = dev.openChunk("/tmp/clog.txt");
				// read 128 blocks
				for (long i = 0; i < 128; i++) {
					ByteBuffer f = ByteBuffer.allocate(4096);
					ch.readBlock(i, 1, f);
					f.rewind();
					System.out.println(f.getLong());
				}
				System.exit(0);
			}

			// test nonexistant file
			Chunk emptychunk = dev.openChunk("/tmp/null.txt");
			// read 512 blocks
			for (long i = 0; i < 512; i++) {
				ByteBuffer f = ByteBuffer.allocate(4096);
				emptychunk.readBlock(i, 1, f);
				assert f.getLong() == 0;
			}

			ArrayList<Long> times = new ArrayList<Long>();
			Chunk chunk = dev.openChunk("/tmp/file.txt");
			// write 128 longs
			for (long i = 0; i < 128; i++) {
				ByteBuffer f = ByteBuffer.allocate(4096);
				long time = System.currentTimeMillis();
				f.putLong(time);
				times.add(time);
				Thread.sleep(5);
				chunk.writeBlock(i, 1, f);
			}
			// read 128 longs
			for (int i = 0; i < 128; i++) {
				ByteBuffer f = ByteBuffer.allocate(4096);
				chunk.readBlock(i, 1, f);
				assert f.getLong() == times.get(i);
			}

			// put 1024 random bytes into a block
			ByteBuffer f = ByteBuffer.allocate(1024 * 1024 * 32);
			byte[] b = new byte[1024];
			new Random().nextBytes(b);
			f.put(b);
			f.rewind();

			// simulate large buff write
			chunk.writeBlock(129, 1, f);

			// simulate large buff read
			ByteBuffer f2 = ByteBuffer.allocate(1024 * 1024 * 32);
			chunk.readBlock(129, 1, f2);

			// Compare bb's
			f.rewind();
			assert f2.equals(f);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
