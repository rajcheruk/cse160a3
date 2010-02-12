import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Box4 {
    static String outputImageStr = "box4.png";
    static public int N, B;

    
    public void Box4(int N, int B){
    }



    static void parseInputArguments(String args[])
    {
    	
    	int i = 0;
      	Box.N=16; Box.B=4;
  
	 // Parse input parameters
	    while (i < args.length && args[i].startsWith("-")) {
	    	
	        String arg = args[i++];
	
	        if (arg.equals("-i")) {
	            // input file
	            outputImageStr = args[i++];                 
	            
	        }
	        // Parallel mode
	        else if (arg.equals("-n")) {
	            try{ N = ( Integer.parseInt(args[i++]));
	            } catch (NumberFormatException Exc){
	                System.err.println("-n needs an int");
	            }
	            Box.N = N;
	        }
	        else if (arg.equals("-b")) {
	            try{ B = ( Integer.parseInt(args[i++]));
	            } catch (NumberFormatException Exc){
	                System.err.println("-b needs an int");
	            }
	            Box.B = B;
	        }
	
	        else{
	            System.err.println("Unknown option " + arg);
	        }
	    }
	    if (i < args.length) {
	        System.err.println("Unknown option " + args[i]);
	    }
    }

    public static void main(String[] args) {

       parseInputArguments(args);
        
       System.out.println("Box.N " + Box.N);
       int imagePixels[] = new int[N*N];
//       image.getRaster().getPixels(0, 0, width, height, (int[])null)

       for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int idx = (i*N + j);
                imagePixels[idx] = 0;
            }
        }
       int N2 = N/2;
       for (int i = 0; i < B; i++) {
            int I = i + N/2 - B/2;
            for (int j = 0; j < B; j++) {
                int J = j + N/2 - B/2;
                int idx = (I*N + J - N/4);
                int idx1 = (I*N + J + N/4);
                imagePixels[idx] = 255;
                imagePixels[idx1] = 255;
                int idx2 = ((I+N/4)*N + J - N/4);
                int idx3 = ((I-N/4)*N + J + N/4);
                imagePixels[idx2] = 255;
                imagePixels[idx3] = 255;
            }
        }

       BufferedImage img = new BufferedImage (Box.N, Box.N, BufferedImage.TYPE_BYTE_GRAY);
       WritableRaster iRaster = img.getRaster();
       iRaster.setPixels(0,0,N,N,imagePixels);

        // Write output
  
        try {
            ImageIO.write(img, "png", new File(outputImageStr));
        } catch (IOException e) {
            System.out.println (e);
//            e.printStackTrace();
        }

    }
}
