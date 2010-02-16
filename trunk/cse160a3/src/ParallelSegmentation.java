import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelSegmentation extends Thread {
	
	public static AtomicInteger numThreads;
	public static ParallelSegmentation[] threads;
	public static Semaphore arrival = new Semaphore(1);
	public static Semaphore departure = new Semaphore(0);
	public static int count = 0;
	
	private int tid;
	private int[] labels;
	private int[] imagePixels;
	private int width;
	private int lowHeight;//where in the image this will start.
	private int highHeight;//where in the image this will end.

	public ParallelSegmentation(int tid) {
		super();
		this.tid = tid;
	}

	public void setLabels(int[] labels) {
		this.labels = labels;
		
	}

	public void setImage(int[] imagePixels) {
		this.imagePixels = imagePixels;
		
	}

	public void setWidth(int width) {
		this.width = width;
		
	}

	public void setHeightRange(int low, int high) {
		this.lowHeight = low;
		this.highHeight = high;
	}

	private void reduce() throws InterruptedException{
		barrier();//waits for all threads to finish before reducing.
		if (numThreads.get() == 1)
			return;
		if (tid % 2 == 0 && tid + 1 == numThreads.get()){//if final thread unmatched.
			threads[tid] = null;
			tid = tid/2;//new index
			while (threads[tid] != null){}//wait for the slot to be empty.
			threads[tid] = this;
		} else if(tid % 2 == 0){//even tids will combine the following odd tid into it.
			highHeight = threads[tid+1].highHeight;
			threads[tid] = null;
			threads[tid+1] = null;
			reducePhase();
			tid = tid/2;//new index
			while (threads[tid] != null){}//wait for the slot to be empty.
			threads[tid] = this;
		}
	}
	
	/*
	 * Reduce this threads portion of the image.
	 */
	private void reducePhase(){
		//do later...........................
		//....
	}
	
	private void barrier() throws InterruptedException{
		arrival.acquire();
		count++;
		if (count < numThreads.get())
			arrival.release();
		else
			departure.release();
		departure.acquire();
		count--;
		if (count > 0)
			departure.release();
		else
			arrival.release();
		return;
	}

}
