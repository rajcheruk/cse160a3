import java.awt.image.BufferedImage;


public class ParallelSegController {
	
	public static int numThreads = 1;
    public static int threshold = 4;
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
    	labels = new int[(width+32)*height];//padded, to avoid cache interference.
    	
    	for (int i = 0; i < (width+32)*height; i++) { 
    		labels[i] = 0; 
    	}

    	// Label each pixel as a separate label.
    	for (int i = 0; i < height; i++) {
    		for (int j = 0; j < width; j++) {
    		
    			int idx = (i*(width+32) + j);
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
    	ParallelSegmentation.threads = threads;
    	for (curNumThreads = 0; curNumThreads < numThreads; curNumThreads++){
    		ParallelSegmentation cur = new ParallelSegmentation(curNumThreads);
    		cur.setLabels(labels);
    		cur.setImage(imagePixels);
    		cur.setWidth(width);
    		cur.setHeightRange(curNumThreads*height/numThreads,
    				(curNumThreads+1)*height/numThreads);
    		cur.start();
    		threads[curNumThreads] = cur;
    	}
    	try {threads[curNumThreads].join();}
    	catch (InterruptedException e){
    		System.err.println("Segmentation not able to complete due to interruption.");
    		System.err.println(e.getStackTrace());
    	}
    }
    
}
