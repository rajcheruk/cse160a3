import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;


public class ParallelSegController {
	
	public static int numThreads = 1;
    public static int randomSeed = -1; // -1 = use time as seed

    private int curNumThreads;
    private ParallelSegmentation[] threads;
    private BufferedImage segmentedImage; // output image
    private int imagePixels[]; // Array of pixel values from input image, one int for each color/pixel.
    private SegmentImg gui;
    private int labels[];
    private int width;
    private int height;
    private int pixelWidth;
    
    public ParallelSegController(){
    }
    
    public void parallelSegInit(BufferedImage image, SegmentImg gui) {

    	threads = new ParallelSegmentation[numThreads];
    	// create representation of image
    	width = image.getWidth();
    	height = image.getHeight();
    	segmentedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    	imagePixels = image.getRaster().getPixels(0, 0, width, height, (int[])null);
    	this.gui = gui;

       	// Space between two pixels in the imagePixel array.
    	pixelWidth = image.getSampleModel().getNumDataElements();

    	// Apply initial labels (each label is it's own index in the label array + 1)
    	labels = new int[(width)*height];//padded, to avoid cache interference.
    	
    	for (int i = 0; i < (width)*height; i++) { 
    		labels[i] = 0; 
    	}

    	// Label each pixel as a separate label.
    	for (int i = 0; i < height; i++) {
    		for (int j = 0; j < width; j++) {
    		
    			int idx = (i*(width) + j);
    			int idx3 = idx*pixelWidth;
    			
    			// Comment this line if you want to label background pixels
    			if (imagePixels[idx3] == 0) 
    				continue;
    			
    			labels[idx] = idx+1;
    			
    		}
    	}	
    }
    
    public void doSegmentation(){
    	ParallelSegmentation.numThreads.set(numThreads);
    	ParallelSegmentation.threadsInPhase1.set(numThreads);
    	ParallelSegmentation.threads = threads;
    	for (curNumThreads = 0; curNumThreads < numThreads; curNumThreads++){
    		ParallelSegmentation cur = new ParallelSegmentation(curNumThreads, this);
    		cur.setLabels(labels);
    		cur.setImage(imagePixels);
    		cur.setWidth(width);
    		cur.setHeightRange(curNumThreads*height/numThreads,
    				(curNumThreads+1)*height/numThreads);
    		cur.setPixelWidth(pixelWidth);
    		cur.start();
    		threads[curNumThreads] = cur;
    	}
    	try {threads[0].join();}
    	catch (InterruptedException e){
    		System.err.println("Segmentation not able to complete due to interruption.");
    		System.err.println(e.getStackTrace());
    	}
    }
    
    HashMap<Integer,Integer> red = new HashMap<Integer,Integer>();
    HashMap<Integer,Integer> green = new HashMap<Integer,Integer>();
    HashMap<Integer,Integer> blue = new HashMap<Integer,Integer>();

    public BufferedImage getSegmentedImage() {


        Random random = new Random();
        // if -R <n> is supplied, use it as seed
        if (randomSeed != -1) {
            random.setSeed(Segmentation.randomSeed);
        }

        int array[] = new int[(width * height) * 3];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int label = labels[i*width+j];
                if (label == 0) {
                    red.put(label, 0);
                    green.put(label, 0);
                    blue.put(label, 0);
                }

                if (!red.containsKey(label)) {
                    red.put(label, (int)(random.nextDouble()*255));
                    green.put(label, (int)(random.nextDouble()*255));
                    blue.put(label, (int)(random.nextDouble()*255));
                }

                array[(i*width+j)*3+0] = red.get(label);
                array[(i*width+j)*3+1] = green.get(label);
                array[(i*width+j)*3+2] = blue.get(label);
            }
        }
 
        // Store pixels in BufferedImage
        segmentedImage.getRaster().setPixels(0, 0, width, height, array);

        return segmentedImage;
    }
    
    protected void updateSegmentedImage(int phase) {
    	// (Updates the GUI after each phase. Slow, useful for visual debugging)
        // comment it out for parallel version
    	gui.freezeTimer();
    	gui.updateTimer(phase);
        // comment it out for parallel version
        gui.updateSegmentedImage(getSegmentedImage(), false);
        gui.unFreezeTimer();
    }

}
