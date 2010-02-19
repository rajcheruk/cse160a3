import javax.imageio.ImageIO;

public class SupportedFormats
   {
   /**
    * Display file formats supported by JAI on your platform.
    * e.g BMP, bmp, GIF, gif, jpeg, JPEG, jpg, JPG, png, PNG, wbmp, WBMP
    * @param args not used
    */
   public static void main ( String[] args )
      {
      String[] names = ImageIO.getWriterFormatNames();
      for ( int i = 0; i < names.length; i++ )
         {
         System.out.println( names[i] );
         }
      }
   }
