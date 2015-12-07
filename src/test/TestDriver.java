package test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import capiblocksim.CapiBlockDevice;
import capiblocksim.Chunk;

public class TestDriver {

	public static void main(String[] args) {
		//open a device
		CapiBlockDevice dev = CapiBlockDevice.getInstance();
		try {
			ArrayList<Long> times = new ArrayList<Long>();
			Chunk chunk = dev.openChunk("file.txt");
			//write 128 longs
			for(long i =0 ;i< 128;i++){
				ByteBuffer f= ByteBuffer.allocate(4096);
				long time = System.currentTimeMillis();
				f.putLong(time);
				times.add(time);
				Thread.sleep(5);
				chunk.writeBlock(i,1,f);
				
			}

			//read 128 longs
			for(long i =0 ;i< 128;i++){
				ByteBuffer f= ByteBuffer.allocate(4096);
				chunk.readBlock(i,1,f);
				assert f.getLong() == times.get(0);
			}
			
			//put 1024 random bytes into a block
			ByteBuffer f = ByteBuffer.allocate(1024*1024*32);
			byte[] b = new byte[1024];
			new Random().nextBytes(b);
			f.put(b);
			f.rewind();
			
			//simulate large buff write
			chunk.writeBlock(129,1,f);
			
			//simulate large buff read
			ByteBuffer f2= ByteBuffer.allocate(1024*1024*32);
			chunk.readBlock(129,1,f2);
			
			//Compare bb's
			f.rewind();
			if(f2.equals(f)){
				System.out.println("equal");
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
