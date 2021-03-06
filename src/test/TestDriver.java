package test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.ibm.research.capiblock.CapiBlockDevice;
import com.ibm.research.capiblock.Chunk;

public class TestDriver {
	final static String testDev = System.getProperty("capi.test.devices");
	private static final Integer totalOps = Integer.valueOf(System.getProperty("capi.capacity.blocks"));
	static final String[] devices = (testDev != null) ? testDev.split(":") : new String[0];
	static final int[] chunks = new int[] { 1, 2, 4 };

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

		/*Test getSize() and close()*/
		for (String device : devices) {
			CapiBlockDevice dev = CapiBlockDevice.getInstance();
			Chunk testChunk = dev.openChunk(device);
			System.out.println("capi devices:" + device + " - number of blocks:" + testChunk.getSize());
			testChunk.close();
		}
		
		/*Simple write/read*/
		CapiBlockDevice dev = CapiBlockDevice.getInstance();
		Chunk testChunk = dev.openChunk(devices[0]);
		ByteBuffer outBuf = ByteBuffer.allocateDirect(CapiBlockDevice.BLOCK_SIZE);
		ByteBuffer inBuf = ByteBuffer.allocateDirect(CapiBlockDevice.BLOCK_SIZE);
		outBuf.putInt(55);
		outBuf.rewind();
		testChunk.writeBlock(10, 1,outBuf);
		testChunk.readBlock(10, 1, inBuf);
		outBuf.position(4096);
		inBuf.position(4096);
		outBuf.flip();
		inBuf.flip();
		System.out.println("Simple Test= Wrote:"+outBuf.getInt()+" Read:"+inBuf.getInt());
		compare(inBuf,outBuf);

		
		/*Run stress tests*/
		for (String device : devices) {
			for (int chunkid : chunks) {
				System.out.println("runnning " + device + " " + chunkid);
				runTests(dev, device, chunkid);
			}
		}
	}

	private static void runTests(CapiBlockDevice dev, String device, int chunkid)
			throws IOException, InterruptedException, ExecutionException {
		// 1 block sync - unique op
		assert (testWrite(dev, chunkid, true, null, totalOps, device) == true) : "Test 1.1: FAIL";
		assert (testRead(dev, chunkid, true, null, totalOps, device) == true) : "Test 1.2: FAIL";

		// 1 block async - unique op
		assert (testWrite(dev, chunkid, false, null, totalOps, device) == true) : "Test 2.1: FAIL";
		assert (testRead(dev, chunkid, false, null, totalOps, device) == true) : "Test 2.2: FAIL";

		// 1 block - 1024 bytes sync
		ByteBuffer buffer = null;
		buffer = initRandomBuffer(1024, 1);
		assert (testWrite(dev, chunkid, true, buffer, totalOps, device) == true) : "Test 1.1: FAIL";
		assert (testRead(dev, chunkid, true, buffer, totalOps, device) == true) : "Test 1.2: FAIL";

		// 1 block - 1024 bytes async
		buffer = initRandomBuffer(1024, 1);
		assert (testWrite(dev, chunkid, false, buffer, totalOps, device) == true) : "Test 2.1: FAIL";
		assert (testRead(dev, chunkid, false, buffer, totalOps, device) == true) : "Test 2.2: FAIL";

		// 1 full block - sync 
		buffer = initRandomBuffer(4096, 1);
		assert (testWrite(dev, chunkid, true, buffer, totalOps, device) == true) : "Test 3.1: FAIL";
		assert (testRead(dev, chunkid, true, buffer, totalOps, device) == true) : "Test 3.2: FAIL";

		// 1 full block  - async
		buffer = initRandomBuffer(4096, 1);
		assert (testWrite(dev, chunkid, false, buffer, totalOps, device) == true) : "Test 4.1: FAIL";
		assert (testRead(dev, chunkid, false, buffer, totalOps, device) == true) : "Test 4.2: FAIL";
		
		// 2 block sync - 5000 bytes sync
		buffer = initRandomBuffer(5000, 2);
		assert (testWrite(dev, chunkid, true, buffer, totalOps, device) == true) : "Test 5.1: FAIL";
		assert (testRead(dev, chunkid, true, buffer, 1, device) == true) : "Test 5.2: FAIL";

		// 2 block sync - 5000 bytes async
		buffer = initRandomBuffer(5000, 2);
		assert (testWrite(dev, chunkid, false, buffer, totalOps, device) == true) : "Test 6.1: FAIL";
		assert (testRead(dev, chunkid, false, buffer, totalOps, device) == true) : "Test 6.2: FAIL";

		// 2 full block sync
		buffer = initRandomBuffer(8192, 2);
		assert (testWrite(dev, chunkid, true, buffer, totalOps, device) == true) : "Test 7.1: FAIL";
		assert (testRead(dev, chunkid, true, buffer, totalOps, device) == true) : "Test 7.2: FAIL";

		// 2 full block async
		buffer = initRandomBuffer(8192, 2);
		assert (testWrite(dev, chunkid, false, buffer, totalOps, device) == true) : "Test 8.1: FAIL";
		assert (testRead(dev, chunkid, false, buffer, totalOps, device) == true) : "Test 8.2: FAIL";
		
		System.out.println("All tests passed!");
	}

	/**
	 * @param dev
	 * @param chunks
	 *            number of chunks to open
	 * @param isSync
	 *            type of call to use on capiblock
	 * @param inBuf
	 *            pre-generated buffer, if any
	 * @param opCount
	 *            number of sequential operations to perform
	 * @param devPath
	 *            path to device
	 * @return
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private static boolean testWrite(final CapiBlockDevice dev, int chunks, boolean isSync, ByteBuffer inBuf,
			int opCount, String devPath) throws IOException, InterruptedException, ExecutionException {
		ArrayList<Chunk> chunkList = new ArrayList<Chunk>();
		int lastUsedChunk = 0;
		boolean hasContent = (inBuf != null) ? true : false;
		ByteBuffer buf = (inBuf != null) ? inBuf : ByteBuffer.allocateDirect(CapiBlockDevice.BLOCK_SIZE);
		int blockCount = buf.capacity() / CapiBlockDevice.BLOCK_SIZE;

		for (int i = 0; i < chunks; i++) {
			chunkList.add(dev.openChunk(devPath));
		}

		for (int i = 0; i < opCount; i += blockCount) {
			if (!hasContent) {
				buf.putInt(i);
			}
			Chunk ch = chunkList.get(lastUsedChunk++ % chunks);
			long retVal = (isSync) ? ch.writeBlock(i, blockCount, buf) : ch.writeBlockAsync(i, blockCount, buf).get();
			if (retVal != blockCount) {
				System.err.println("Block count does not match!");
				return false;
			}
			/* capiblock should not change buffer' state */// TODO
			if (!hasContent) {
				buf.clear();
			}
		}
		return true;
	};

	private static boolean testRead(final CapiBlockDevice dev, int chunks, boolean isSync, ByteBuffer inBuf,
			int opCount, String devPath) throws IOException, InterruptedException, ExecutionException {
		ArrayList<Chunk> chunkList = new ArrayList<Chunk>();
		int lastUsedChunk = 0;
		boolean hasContent = (inBuf != null) ? true : false;
		ByteBuffer correct = (inBuf != null) ? inBuf : ByteBuffer.allocateDirect(CapiBlockDevice.BLOCK_SIZE);
		int blockCount = correct.capacity() / CapiBlockDevice.BLOCK_SIZE;
		ByteBuffer read = ByteBuffer.allocateDirect(blockCount * CapiBlockDevice.BLOCK_SIZE);
		for (int i = 0; i < chunks; i++) {
			chunkList.add(dev.openChunk(devPath));
		}

		for (int i = 0; i < opCount; i += blockCount) {
			if (!hasContent) {
				correct.putInt(i);
			}
			Chunk ch = chunkList.get(lastUsedChunk++ % chunks);
			long retVal = (isSync) ? ch.readBlock(i, blockCount, read) : ch.readBlockAsync(i, blockCount, read).get();
			//TODO buffer returned from readblock should be 0,0,0
			read.position(blockCount*4096);
			correct.position(blockCount*4096);
			
			if(!compare(read, correct)){
				return false;
			};
			if (retVal != blockCount) {
				System.err.println("Block count does not match!");
				return false;
			}
			/* capiblock should not change buffer' state */// TODO
			if (!hasContent) {
				correct.clear();
			}
			read.clear();
		}
		return true;
	};

	private static ByteBuffer initRandomBuffer(int size, int blocks) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(blocks * CapiBlockDevice.BLOCK_SIZE);
		byte[] b = new byte[size];
		new Random().nextBytes(b);
		buffer.put(b);
		buffer.rewind();
		return buffer;
	}

	private static boolean compare(ByteBuffer read, ByteBuffer correct) {
		/* change to read mode */
		read.flip();
		correct.flip();

		/* compare */
		if (read.compareTo(correct) == 0) {
			return true;
		}

		System.err.println(read);
		System.err.println(correct);
		return false;
	}
}
