package com.ibm.research.capiblock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Chunk implements AutoCloseable {
	FileChannel inChannel;
	@SuppressWarnings("unused")
	private final CapiBlockDevice cblk;
	final int chunk_id;
	private String filename;

	Chunk(final CapiBlockDevice cblk, final int chunk_id) {
		this.cblk = cblk;
		this.chunk_id = chunk_id;
	}

	public Chunk(final CapiBlockDevice cblk, final int chunk_id, FileChannel inChannel, String filename) {
		this.inChannel = inChannel;
		this.cblk = cblk;
		this.chunk_id = chunk_id;
		this.filename = filename;
	}

	/**
	 * @param lba
	 * @param nBlocks
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public long writeBlock(final long lba, final long nBlocks, final ByteBuffer buf) throws IOException {
		validateBuffer(nBlocks, buf);
		buf.rewind();
		buf.limit((int) nBlocks * 4096);
		synchronized (inChannel) {
			inChannel.write(buf, (long) lba * 4096);
		}
		return nBlocks;
	}

	/**
	 * @param lba
	 * @param nBlocks
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public long readBlock(final long lba, final long nBlocks, final ByteBuffer buf) throws IOException {
		validateBuffer(nBlocks, buf);
		synchronized (inChannel) {
			buf.clear();
			int oldLimit = buf.limit();
			buf.limit((int) (nBlocks * 4096));
			synchronized (inChannel) {
				inChannel.read(buf, (long) lba * 4096);
			}
			buf.limit(oldLimit);
			buf.rewind();
		}
		return nBlocks;
	}

	/**
	 * @param lba
	 * @param nBlocks
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public Future<Long> readBlockAsync(final long lba, final long nBlocks, final ByteBuffer buf) throws IOException {
		long retVal = readBlock(lba, nBlocks, buf);
		CompletableFuture<Long> future = new CompletableFuture<Long>();
		future.complete(retVal);
		return (Future<Long>) future;
	}

	/**
	 * @param lba
	 * @param nBlocks
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public Future<Long> writeBlockAsync(final long lba, final long nBlocks, final ByteBuffer buf) throws IOException {
		long retVal = writeBlock(lba, nBlocks, buf);
		CompletableFuture<Long> future = new CompletableFuture<Long>();
		future.complete(retVal);
		return (Future<Long>) future;
	}

	/**
	 * get the size of the chunk in blocks.
	 *
	 * @return the size of the chunk as a number of blocks.
	 * @throws IOException
	 *             when the operation failed.
	 */
	public long getSize() throws IOException {
		return inChannel.size() / CapiBlockDevice.BLOCK_SIZE;
	}

	/*
	 * closes the chunk
	 * 
	 */
	public void close() throws IOException {
		synchronized (CapiBlockDevice.getInstance()) {
			CapiBlockDevice.chunkids.remove(chunk_id);
			if(CapiBlockDevice.chunkids.size()==0){
				CapiBlockDevice.files.remove(filename);
				inChannel.close();
			}
		}
	}

	/**
	 * @param blocks
	 * @param buf
	 * @return
	 */
	private boolean validateBuffer(long blocks, ByteBuffer buf) {
		if (buf == null) {
			System.err.println("Buffer not initialized!");
			return false;
		}
		if (!buf.isDirect()) {
			System.err.println("Direct buffer required!");
			return false;
		}
		if (buf.capacity() < blocks * CapiBlockDevice.BLOCK_SIZE) {
			System.err.println("Insufficient capacity! [has:" + buf.remaining() + " required:"
					+ blocks * CapiBlockDevice.BLOCK_SIZE + "]");
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	public Stats getStats() {
		System.err.println("Not implemented!");
		return new Stats();
	}

	// TODO
	class Stats {
		public Stats() {
		}
	}

}
