import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.Arrays;


@SuppressWarnings("serial")
public class SegmentImg extends JPanel {
    public static int THUMB_MAX_WIDTH = 400;
    public static int THUMB_MAX_HEIGHT = 300;
    public static int SEGMENT_IMAGE_MAX_WIDTH = 400;
    public static int SEGMENT_MAGE_MAX_HEIGHT = 300;

    //default values for program arguments 
    static int numThreads=1; 
    static boolean parallelMode = false;
    static boolean displayOff = false;
    static boolean hold = false; 
    static String title = "Serial Segmentation";

    // Default image file names
    static String inputImageStr = "input.png";
    static String outputImageStr = "output";

    static String file_extension = "_sm."; //serial segmentation
    static String format = "png";
    
    private BufferedImage origThumb;
 
    private JLabel timerLabel;
    private JLabel timerLabelSeconds;
    private JLabel segmentLabel;
    private ImageIcon segmentedIcon;
    private Dimension segmentImageSize;

    private static long time0;
    private static long timeSub0;
    private static long timeSub;

    private static long elapsedTime;

    //BENFRASER RULE
    private static volatile long codyCoriva;
    
    public SegmentImg(BufferedImage image) {

        if (!displayOff) {
            // create thumbnails
            segmentImageSize =  ImageUtils.determineSize(image.getWidth(), image.getHeight(), SEGMENT_IMAGE_MAX_WIDTH, SEGMENT_MAGE_MAX_HEIGHT);
            Dimension thumbSize = ImageUtils.determineSize(image.getWidth(), image.getHeight(), THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT);
            this.origThumb = ImageUtils.getScaledInstance(image, thumbSize.width, thumbSize.height, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
 
            this.createLayout();
        }
    }



    private void createLayout() {
        this.setLayout(new BorderLayout());

        //Display a thumbnail of the original image
        Container topContainer = new JPanel();
        topContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        Container btmContainer = new JPanel();
        btmContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        ImageIcon fromIcon = new ImageIcon(origThumb);
        JLabel fromLabel = new JLabel("");
        fromLabel.setIcon(fromIcon);
        fromLabel.setBorder(BorderFactory.createTitledBorder("Original image"));
        topContainer.add(fromLabel);

        this.add(topContainer, BorderLayout.WEST);

        //Display the image being changed

        segmentLabel = new JLabel();
        segmentLabel.setBorder(BorderFactory.createTitledBorder("Segmentation progress"));
        btmContainer.add(segmentLabel);
        this.add(btmContainer, BorderLayout.EAST);

        Container timerContainer = new JPanel();
        timerLabel = new JLabel("Time taken: ");
        timerContainer.add(timerLabel);
        timerLabelSeconds = new JLabel("0 ms");
        timerContainer.add(timerLabelSeconds);
        this.add(timerContainer, BorderLayout.SOUTH);
    }

    public void startTimer() {
        time0 = System.currentTimeMillis();
        timeSub = 0;
        timeSub0 = 0;
    }

    public void stopTimer() {
        elapsedTime = System.currentTimeMillis() - time0 - timeSub;
    }

    public void freezeTimer() {
        timeSub0 = System.currentTimeMillis();
    }

    public void unFreezeTimer() {
        timeSub += System.currentTimeMillis() - timeSub0;
    }

    public void updateTimer(int phase) {
        if (!displayOff) {
            long t = System.currentTimeMillis() - time0;
            t -= timeSub;
            if(phase !=-1)
            	timerLabelSeconds.setText(Long.toString(t) + " ms" + "  (Phase: " + phase + ")" );
            else 
            	timerLabelSeconds.setText(Long.toString(t) + " ms");	
        }
    }

    public void updateDoneLabel() {
        if (!displayOff)
            timerLabel.setText("Image segmentation finished in: ");
    }

    public void updateSegmentedImage(BufferedImage image, boolean hiQuality) {
        if (!displayOff) {
            BufferedImage displayImage = ImageUtils.getScaledInstance(image, segmentImageSize.width, segmentImageSize.height, RenderingHints.VALUE_INTERPOLATION_BILINEAR, hiQuality);
            segmentedIcon = new ImageIcon(displayImage);
            segmentLabel.setIcon(segmentedIcon);
        }
    }




    static void Usage() {
        System.err.println("Usage: SegmentImg \n" +
                "[-f <format> ] use format as output format. i.e. png. (default png)\n" + 
                "[-p <n> ] switch to parallel mode with n number of threads\n" + 
                "[-s <n> ] threshold value (default 4)\n" +
                "[-R <n> ] use <n> as seed to the random number generator which in turn chooses color on labels.\n" +
                "[-t ] turn off display\n" + 
                "[-h ] hold the display\n" + 
                "[-i inputimage ]\n");

        System.exit(0);
    }
    
    static void parseInputArguments(String args[])
    {
    	
    	int i = 0;
      	int threshold = 4;
  
	 // Parse input parameters
	    while (i < args.length && args[i].startsWith("-")) 
	    {
	    	
	        String arg = args[i++];
	
	        if (arg.equals("-i")) {
	            // input file
	            inputImageStr = args[i++];                 
	            outputImageStr = inputImageStr.substring(0, inputImageStr.lastIndexOf('.')) + "_out";
	            
	        }
	        // Parallel mode
	        else if (arg.equals("-p")) {
	            parallelMode = true;
	            try{ numThreads = ( Integer.parseInt(args[i++]));
	            } catch (NumberFormatException Exc){
	                System.err.println("-p needs an int");
	                Usage();
	            }
	            ParallelSegController.numThreads = numThreads;
	            title = "Parallel Segmentation";
	            // The output file will have the extension "_pm" added
	            file_extension = "_pm."; // parallel segmentation
	
	        }
	        else if (arg.equals("-s")) {
	            try{ threshold = ( Integer.parseInt(args[i++]));
	            } catch (NumberFormatException Exc){
	                System.err.println("-s needs an int");
	                Usage();
	            }
	            Segmentation.threshold = threshold;
	        }
	        else if (arg.equals("-h")) {
	           hold = true;
	        }
	        else if (arg.equals("-f")) {
	            String outputformat = args[i++];
	            String[] supportedformatnames = ImageIO.getWriterFormatNames();
	
	            if (!Arrays.asList(supportedformatnames).contains(outputformat)) {
	                System.err.println("format " + outputformat + " not supported. supported formats are:");
	                System.err.println(Arrays.asList(supportedformatnames));
	                Usage();
	            }
	            format = outputformat;
	        }
	        else if (arg.equals("-R")) {
	            int randomseed = -1;
	            try{
	                randomseed = ( Integer.parseInt(args[i++]));
	            } catch (NumberFormatException Exc){
	                System.err.println("-R needs an int");
	                Usage();
	            }
	            Segmentation.randomSeed = randomseed;
	        }
	
	        else if (arg.equals("-t")) {
	            displayOff = true;
	            System.out.println("Display turned off!");
	            SegmentImg.displayOff = true;
	        }
	        else{
	            System.err.println("Unknown option " + arg);
	            Usage();
	        }
	    }
	    if (i < args.length) {
	        System.err.println("Unknown option " + args[i]);
	        Usage();
	    }
}

    public static void main(String[] args) {

        JFrame frame;

        parseInputArguments(args);
        
        File imgFile = new File(inputImageStr);
        assert(imgFile.exists() && imgFile.isFile());

        //Read images
        BufferedImage image = null;
        try {
            image = ImageIO.read(imgFile);
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Initiate gui 
        SegmentImg sa = new SegmentImg(image);
        if (!displayOff) {
            frame = new JFrame(title);
            frame.setContentPane(sa);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            frame.setSize(800, 400);
        }

        // Do work
        
        BufferedImage segmentedImage;
        
        if(parallelMode){
	        ParallelSegController segmentation = new ParallelSegController();
	        segmentation.parallelSegInit(image, sa);
	        sa.startTimer();
	    
	        segmentation.doSegmentation();
	       
	        sa.stopTimer();
	        
	        sa.updateTimer(-1);
	        segmentedImage = segmentation.getSegmentedImage();
	        sa.updateSegmentedImage(segmentedImage, true);
        }
        else{
	        Segmentation segmentation = new Segmentation();
	        segmentation.SegmentationInit(image, sa);
	        sa.startTimer();
	    
	        segmentation.doSegmentation();
	       
	        sa.stopTimer();
	        
	        sa.updateTimer(-1);
	        segmentedImage = segmentation.getSegmentedImage();
	        sa.updateSegmentedImage(segmentedImage, true);
        }
        // Write output
  
        File output = new File( outputImageStr + file_extension  + format);
        try {
            ImageIO.write(segmentedImage, format, output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Elapsed Time (ms): " +  Long.toString(elapsedTime));
        if(parallelMode)
        	System.out.println("Phase 1 Time (ms): " + codyCoriva);

        if(!displayOff){
            try{
            	if(!hold){
            		// last 4 sec before self destruction
            		Thread.sleep(4000); 
            		System.exit(0);
            	}
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
    }



	public static void setCodyCoriva(long codyCoriva) {
		SegmentImg.codyCoriva = codyCoriva - time0 - timeSub;
	}

	public static long getCodyCoriva() {
		return codyCoriva;
	}
}

