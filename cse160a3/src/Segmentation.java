import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

public class Segmentation {

    public static int threshold = 4;
    public static int randomSeed = -1; // -1 = use time as seed

    private BufferedImage segmentedImage; // output image
    private int imagePixels[]; // Array of pixel values from input image, one int for each color/pixel.
    private SegmentImg gui;
    private int labels[];
    private int width;
    private int height;
    private int pixelWidth;

    public Segmentation() {

    }

    public void SegmentationInit(BufferedImage image, SegmentImg gui) {

    	// create representation of image
    	width = image.getWidth();
    	height = image.getHeight();
    	segmentedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    	imagePixels = image.getRaster().getPixels(0, 0, width, height, (int[])null);
    	this.gui = gui;

       	// Space between two pixels in the imagePixel array.
    	pixelWidth = image.getSampleModel().getNumDataElements();

    	// Apply initial labels (each label is it's own index in the label array + 1)
    	labels = new int[width*height];
    	
    	for (int i = 0; i < width*height; i++) { 
    		labels[i] = 0; 
    	}

    	// Label each pixel as a separate label.
    	for (int i = 0; i < height; i++) {
    		for (int j = 0; j < width; j++) {
    		
    			int idx = (i*width + j);
    			int idx3 = idx*pixelWidth;
    			
    			// Comment this line if you want to label background pixels
    			if (imagePixels[idx3] == 0) 
    				continue;
    			
    			labels[idx] = idx+1;
    			
    		}
    	}	
    }	


    public void doSegmentation(){

        int pix[] = imagePixels;
        int maxN = Math.max(width, height);

        int phases = (int) Math.ceil(Math.log(maxN) / Math.log(2)) + 1;
        System.out.println("Ok, " + (phases+1) + " phases scheduled...");

        for (int pp = 0; pp <= phases; pp++) {

            // pass one. Find neighbors with better labels.
            for (int i = height - 1; i >= 0; i--) {
                for (int j = width - 1; j >= 0; j--) {
                    int idx = i*width + j;
                    int idx3 = idx*pixelWidth;

                    if (labels[idx] == 0) 
                    	continue;

                    int ll = labels[idx]; // save previous label

                    // pixels are stored as 3 ints in "pix" array. we just use the first of them. 
                    // Compare with each neighbor
                    if (i != height - 1 && 
                            Math.abs(pix[((i+1)*width + j)*pixelWidth] - pix[idx3]) < threshold) 
                        labels[idx] = Math.max(labels[idx], labels[(i+1)*width + j]);

                    if (i != 0 && 
                            Math.abs(pix[((i-1)*width + j)*pixelWidth] - pix[idx3]) < threshold) 
                        labels[idx] = Math.max(labels[idx], labels[(i-1)*width + j]);

                    if (i != height - 1 && j != width - 1 && 
                            Math.abs(pix[((i+1)*width + j + 1)*pixelWidth] - pix[idx3]) < threshold) 
                        labels[idx] = Math.max(labels[idx], labels[(i+1) * width + j + 1]);

                    if (i != 0 && j != width - 1 && 
                            Math.abs(pix[((i-1) * width + j + 1)*pixelWidth] - pix[idx3]) < threshold) 
                        labels[idx] = Math.max(labels[idx], labels[(i-1) * width + j + 1]);

                    if (i != height - 1 && j != 0 && 
                            Math.abs(pix[((i+1) * width + j - 1)*pixelWidth] - pix[idx3]) < threshold) 
                        labels[idx] = Math.max(labels[idx], labels[(i+1) * width + j - 1]);

                    if (i != 0 && j != 0 && 
                            Math.abs(pix[((i-1) * width + j - 1)*pixelWidth] - pix[idx3]) < threshold) 
                        labels[idx] = Math.max(labels[idx], labels[(i-1) * width + j - 1]);

                    if (j != 0 && 
                            Math.abs(pix[(i*width + j - 1)*pixelWidth] - pix[idx3]) < threshold)
                        labels[idx] = Math.max(labels[idx], labels[i*width + j - 1]);

                    if (j != width - 1 && 
                            Math.abs(pix[(i*width + j + 1)*pixelWidth] - pix[idx3]) < threshold)
                        labels[idx] = Math.max(labels[idx], labels[i*width + j + 1]);

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
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int idx = i*width + j;
                    if (labels[idx] != 0) {
                        labels[idx] = Math.max(labels[idx], labels[labels[idx] - 1]); 
                        // subtract 1 from pixel's label to convert it to array index
                    }
                }
            }

            System.out.println( "Phase " + (pp) + " done.");

            // (Updates the GUI after each phase. Slow, useful for visual debugging)
            // comment it out for parallel version
            gui.freezeTimer();
            gui.updateTimer(pp);
            // comment it out for parallel version
            gui.updateSegmentedImage(getSegmentedImage(), false);
            gui.unFreezeTimer();
        }
        
     // comment it out for parallel version
        gui.updateDoneLabel();
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
}
