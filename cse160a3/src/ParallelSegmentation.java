import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelSegmentation extends Thread {
	
	public static AtomicInteger numThreads = new AtomicInteger();
	public static AtomicInteger threadsInPhase1 = new AtomicInteger();
	public static ParallelSegmentation[] threads;
	public static int threshold = 4;
	public static int root = 0;
	public static Semaphore arrival = new Semaphore(1);
	public static Semaphore departure = new Semaphore(0);
	public static int count = 0;
	public static Semaphore arrivalP = new Semaphore(1);//for the partial barrier
	public static Semaphore departureP = new Semaphore(0);//for the partial barrier
	public static int countP = 0;//for the partial barrier
	
	private int tid;
	private int[] labels;
	private int[] imagePixels;
	private int width;
	private int lowHeight;//where in the image this will start.
	private int highHeight;//where in the image this will end.
	private int pixelWidth;
	private ParallelSegController gui;
	private boolean changed;

	public ParallelSegmentation(int tid, ParallelSegController gui) {
		super();
		this.gui = gui;
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
	
	public void setPixelWidth(int width){
		pixelWidth = width;
	}
	
	@SuppressWarnings("static-access")
	public void run() {
		try {
			doSegmentation();
		} catch (InterruptedException e) {
			System.err.print("Segmentation interrupted in thread ");
			System.err.println(tid);
			e.printStackTrace();
		}
		SegmentImg.setPart1Time(System.currentTimeMillis());
		try {
			reduce();
		} catch (InterruptedException e) {
			System.err.print("Reduction distrupted in thread ");
			System.err.println(tid);
			e.printStackTrace();
		}
	}
	
	public void doSegmentation() throws InterruptedException{
        int pix[] = imagePixels;
        int maxN = Math.max(width, highHeight-lowHeight);

        int phases = (int) Math.ceil(Math.log(maxN) / Math.log(2)) + 1;
        System.out.print("Ok, " + (phases+1) + " phases scheduled for");
        System.out.println(" thread " + tid);

        changed = true;
        for (int pp = 0; pp <= phases && changed; pp++) {
        	changed = false;//reset for the round.

            // pass one. Find neighbors with better labels.
            for (int i = highHeight - 1; i >= lowHeight; i--) {
                for (int j = width - 1; j >= 0; j--) {
                    int idx = i*width + j;
                    int idx3 = idx*pixelWidth;

                    if (labels[idx] == 0) 
                    	continue;

                    int ll = labels[idx]; // save previous label
                    int max;

                    // pixels are stored as 3 ints in "pix" array. we just use the first of them. 
                    // Compare with each neighbor
                    if (i != highHeight - 1 && 
                            Math.abs(pix[((i+1)*width + j)*pixelWidth] - pix[idx3]) < threshold){
                        max = Math.max(labels[idx], labels[(i+1)*width + j]);
                        if (max != labels[idx])
                        	changed = true;
                        labels[idx] = max;
                    }

                    if (i != lowHeight && 
                            Math.abs(pix[((i-1)*width + j)*pixelWidth] - pix[idx3]) < threshold){ 
                        max = Math.max(labels[idx], labels[(i-1)*width + j]);
                        if (max != labels[idx])
                        	changed = true;
                        labels[idx] = max;
                    }

                    if (i != highHeight - 1 && j != width - 1 && 
                            Math.abs(pix[((i+1)*width + j + 1)*pixelWidth] - pix[idx3]) < threshold){
                        max = Math.max(labels[idx], labels[(i+1) * width + j + 1]);
                        if (max != labels[idx])
                        	changed = true;
                        labels[idx] = max;
                    }

                    if (i != lowHeight && j != width - 1 && 
                            Math.abs(pix[((i-1) * width + j + 1)*pixelWidth] - pix[idx3]) < threshold){
                        max = Math.max(labels[idx], labels[(i-1) * width + j + 1]);
                        if (max != labels[idx])
                        	changed = true;
                        labels[idx] = max;
                    }

                    if (i != highHeight - 1 && j != 0 && 
                            Math.abs(pix[((i+1) * width + j - 1)*pixelWidth] - pix[idx3]) < threshold){
                        max = Math.max(labels[idx], labels[(i+1) * width + j - 1]);
                        if (max != labels[idx])
                        	changed = true;
                        labels[idx] = max;
                    }

                    if (i != lowHeight && j != 0 && 
                            Math.abs(pix[((i-1) * width + j - 1)*pixelWidth] - pix[idx3]) < threshold){
                        max = Math.max(labels[idx], labels[(i-1) * width + j - 1]);
                        if (max != labels[idx])
                        	changed = true;
                        labels[idx] = max;
                    }

                    if (j != 0 && 
                            Math.abs(pix[(i*width + j - 1)*pixelWidth] - pix[idx3]) < threshold){
                        max = Math.max(labels[idx], labels[i*width + j - 1]);
                        if (max != labels[idx])
                        	changed = true;
                        labels[idx] = max;
                    }

                    if (j != width - 1 && 
                            Math.abs(pix[(i*width + j + 1)*pixelWidth] - pix[idx3]) < threshold){
                        max = Math.max(labels[idx], labels[i*width + j + 1]);
                        if (max != labels[idx])
                        	changed = true;
                        labels[idx] = max;
                    }

                    // if label assigned to this pixel during "follow the pointers" step is worse than label
                    // of one of its neighbors, then that means that we're converging to local maximum instead
                    // of global one. To correct this, we replace our root pixel's label with better newly found one.
                    if (ll < labels[idx]) {
                        if (labels[ll - 1] < labels[idx])
                            labels[ll - 1] = labels[idx];
                    }
                }
            }

            // pass two. propagates the updated label of the parent to the children.
            for (int i = 0; i < highHeight; i++) {
                for (int j = 0; j < width; j++) {
                    int idx = i*width + j;
                    if (labels[idx] != 0) {
                        labels[idx] = Math.max(labels[idx], labels[labels[idx] - 1]); 
                        // subtract 1 from pixel's label to convert it to array index
                    }
                }
            }

            System.out.println( "Phase " + (pp) + " done in thread" + tid);
            
			barrier(threadsInPhase1.get());
			if(tid==root){
				gui.updateSegmentedImage(pp);
				if (!changed)//will exit before next phase
					for (int i = 0; i < numThreads.get(); i++)//find replacement
						if (threads[i].changed){//if this thread will continue next phase
							root = i;
							break;
						}
			}
			barrier(threadsInPhase1.get());
        }
        threadsInPhase1.decrementAndGet();
        
        // comment it out for parallel version
        //gui.updateDoneLabel();
    }

	
	private void reduce() throws InterruptedException{
		barrier();//waits for all threads to finish before reducing.
		boolean reduces = false;
		int newHigh = -1;
		if (numThreads.get() == 1){
			numThreads.set(0);
			return;}//returns if there is only one thread left.
		if (tid % 2 == 0 && tid + 1 == numThreads.get()){//if final thread unmatched.
			threads[tid] = null;
			tid = tid/2;//new index
			while (threads[tid] != null){}//wait for the slot to be empty.
			threads[tid] = this;
		} else if(tid % 2 == 0){//even tids will combine the following odd tid into it.
			reduces = true;
			newHigh = threads[tid+1].highHeight;
			threads[tid] = null;
			threads[tid+1] = null;
			reducePhase(newHigh);
			highHeight = newHigh;
			tid = tid/2;//new index
			while (threads[tid] != null){}//wait for the slot to be empty.
			threads[tid] = this;
		}
		barrier();//need all threads to be reduced
		if (this != threads[tid])//this has been merged.
			return;
		if (reduces){
			numThreads.decrementAndGet();
		}
		
		reduce();//until there is only one thread left.
	}
	
	/*
	 * Reduce this threads portion of the image.
	 */
	private void reducePhase(int newHigh) throws InterruptedException{
		int top, bottom, idxt, idxb;
		int changeFrom, changeTo;
		
		for(int i=0; i < width; i++) {
			top = ((highHeight-1)*width+i)*pixelWidth;
			bottom = (highHeight*width+i)*pixelWidth;
			idxt = top/pixelWidth;
			idxb = bottom/pixelWidth;
			if (labels[idxt] == 0){
				continue;
			}
			//bottom left
			if(i!=0 && labels[idxb-1] != 0 &&
					Math.abs(imagePixels[top] - imagePixels[bottom-pixelWidth]) < threshold){
				changeFrom = labels[idxt];
				labels[labels[idxt]-1] = labels[idxb-1];
				changeTo = labels[idxb-1];
				
				//traverse thru labels that point to daddy and make them point to grandma
				if (changeFrom != changeTo)
					updateLabels(newHigh, changeFrom, changeTo);
			}
			//bottom
			if(labels[idxb] != 0 &&
					Math.abs(imagePixels[top] - imagePixels[bottom]) < threshold){
				changeFrom = labels[idxt];
				labels[labels[idxt]-1] = labels[idxb];
				changeTo = labels[idxb];
				
				//traverse thru labels that point to daddy and make them point to grandma
				if (changeFrom != changeTo)
					updateLabels(newHigh, changeFrom, changeTo);
			}
			//bottom right
			if(i!=width-1 && labels[idxb-1] != 0 &&
					Math.abs(imagePixels[top] - imagePixels[bottom+pixelWidth]) < threshold){
				changeFrom = labels[idxt];
				labels[labels[idxt]-1] = labels[idxb+1];
				changeTo = labels[idxb+1];
				
				//traverse thru labels that point to daddy and make them point to grandma
				if (changeFrom != changeTo)
					updateLabels(newHigh, changeFrom, changeTo);
			}
			
		}
	}
	
	private void updateLabels(int newHigh, int changeFrom, int changeTo) throws InterruptedException {
		for(int i=lowHeight; i<newHigh; i++) {
			for(int whitefang=0; whitefang<width; whitefang++) {
				if(labels[i*width+whitefang] == changeFrom) {
					labels[i*width+whitefang] = changeTo;
				}
			}
		}
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
	
	/*
	 * Partial barrier.  Waits for only waitFor threads to arrive,
	 * not necessarily all of them.
	 */
	private void barrier(int waitFor) throws InterruptedException{
		arrivalP.acquire();
		countP++;
		if (countP < waitFor)
			arrivalP.release();
		else
			departureP.release();
		departureP.acquire();
		countP--;
		if (countP > 0)
			departureP.release();
		else
			arrivalP.release();
		return;
	}

}
