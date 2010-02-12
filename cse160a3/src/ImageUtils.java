import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Students, do not touch or bother to read this, it is just image resizing for display in the GUI.
 */
public class ImageUtils {
	 /**
	  *@author http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
    
    public static Dimension determineSize(int origWidth, int origHeight, int maxWidth, int maxHeight){
    	if(origWidth < maxWidth && origHeight < maxHeight ){
    		return new Dimension(origWidth, origHeight);
    	}
    	
    	double widthRatio =  (double) maxWidth / origWidth;
    	double heightRatio = (double) maxHeight / origHeight;
    	
    	if (widthRatio < heightRatio){
    		return new Dimension((int)(origWidth * widthRatio), (int) (origHeight * widthRatio));
    	}else{
    		return new Dimension((int)(origWidth * heightRatio), (int) (origHeight * heightRatio));
    	}
    	
    }
    
    public static BufferedImage makeBinaryImage(BufferedImage inputImage) {

    	// Create a binary image for the results of processing

    	int w = inputImage.getWidth();
    	int h = inputImage.getHeight();
    	BufferedImage outputImage = new BufferedImage(w, h,
    	BufferedImage.TYPE_BYTE_BINARY);

    	// Work on a copy of input image because it is modified by diffusion

    	WritableRaster input = inputImage.copyData(null);
    	WritableRaster output = outputImage.getRaster();

    	final int threshold = 128;
    	float value, error;

    	for (int y = 0; y < h; ++y)
    	for (int x = 0; x < w; ++x) {

    	value = input.getSample(x, y, 0);

    	// Threshold value and compute error

    	if (value < threshold) {
    	output.setSample(x, y, 0, 0);
    	error = value;
    	}
    	else {
    	output.setSample(x, y, 0, 1);
    	error = value - 255;
    	}

    	// Spread error amongst neighbouring pixels

    	if((x > 0) && (y > 0) && (x < (w-1)) && (y < (h-1)))
    	{
    	value = input.getSample(x+1, y, 0);
    	input.setSample(x+1, y, 0, clamp(value + 0.4375f * error));
    	value = input.getSample(x-1, y+1, 0);
    	input.setSample(x-1, y+1, 0, clamp(value + 0.1875f * error));
    	value = input.getSample(x, y+1, 0);
    	input.setSample(x, y+1, 0, clamp(value + 0.3125f * error));
    	value = input.getSample(x+1, y+1, 0);
    	input.setSample(x+1, y+1, 0, clamp(value + 0.0625f * error));
    	}

    	}
    	return outputImage;

    	}

    	// Forces a value to a 0-255 integer range

    	public static int clamp(float value) {
    	return Math.min(Math.max(Math.round(value), 0), 255);
    	}

}

