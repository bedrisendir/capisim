package test;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.ibm.research.capiblock.CapiBlockDevice;
import com.ibm.research.capiblock.Chunk;

public class CleanBlocks {
	final static String testDev = System.getProperty("capi.test.devices");
	private static final Integer totalOps = Integer.valueOf(System.getProperty("capi.capacity.blocks"));
	static final String[] devices = (testDev != null) ? testDev.split(":") : new String[0];

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("usage CleanBlocks <start addr> <totalblocks>");
		}
		int start = Integer.valueOf(args[0]);
		int num_blocks = Integer.valueOf(args[1]);

		CapiBlockDevice dev = CapiBlockDevice.getInstance();
		Chunk testChunk = dev.openChunk(devices[0]);
		ByteBuffer outBuf = ByteBuffer.allocateDirect(CapiBlockDevice.BLOCK_SIZE);
		outBuf.putLong(0);
		for (int i = start; i < (start + num_blocks); i++) {
			testChunk.writeBlock(i, 1, outBuf);
		}
		
		for (int i = start; i < (start + num_blocks); i++) {
			outBuf.clear();
			testChunk.readBlock(i, 1, outBuf);
			//TODO
			outBuf.position(4096);
			outBuf.flip();
			if (outBuf.getLong() != 0) {
				System.out.println("error clearing book-keeping blocks");
			}
		}
		System.out.println("Cleared "+num_blocks + " starting from address="+start);
	}

}
