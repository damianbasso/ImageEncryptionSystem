import java.io.*;
import java.util.Random;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class EncryptImage {
    
    Color[] colors;
    int x;
    int y;
    public EncryptImage(File image, File text, String filename) throws IOException {
        parseImage(ImageIO.read(image));
        encrypt(text);
        generateImage(filename);
    }

    private void parseImage(BufferedImage image) throws IOException {
        this.x = image.getWidth();
        this.y = image.getHeight();
        this.colors = new Color[this.x*this.y];
        for (int y = 0; y< this.y; y++) {
            for (int x = 0; x <this.x; x++) {
                int clr = image.getRGB(x, y);
                colors[y*this.x + x] = new Color(clr);
            }
        }
    }

    private static int getLength(File file) throws IOException {
        FileReader fr= new FileReader(file);   //Creation of File Reader object
        BufferedReader br=new BufferedReader(fr);  //Creation of BufferedReader object
        int count = 0;             
        while(br.read() != -1)         //Read char by Char
        {
            count++;
        }
        return count;
    }

    private void encrypt(File file) throws IOException {
        FileReader fr= new FileReader(file);   //Creation of File Reader object
        BufferedReader br=new BufferedReader(fr);  //Creation of BufferedReader object

        // Calculates the span, which is how many characters

        // Why does it break when I do /(getLength(file) - this.x)
        // I think that shit should be correct
        int span = (this.colors.length -this.x) / (getLength(file)); 
        // System.out.println("span = " + span);
        // System.out.println("colorslength = " + this.colors.length);
        // System.out.println("this.x = " + this.x);
        // System.out.println("lenth = " + getLength(file));

        // int span = (this.colors.length -this.x) / (getLength(file)-this.x); 
        
        // Sets the first row of the image to contain a key which gives
        // how many pixels each character of the message is contained over

        // Gets the initial value represented in the first row of the image
        int size = 0;
        for (int i =0; i< this.x; i++) {
            Color curr = colors[i];
            // Treats the 3 channels as base 5 values
            // i.e, rgb(17,12,8) = 17*5^25 + 12*5^1 + 8*5^0
            size += curr.getRed()*25 + curr.getGreen()*5 + curr.getBlue()*1;
        }

        // mod the size by the total width
        // hence, the maximum length that a single character can
        // be distributed over the image is one entire row of the image (x)
        size %= this.x;

        // Diff is how much distortion of the image has to be made
        // to encrypt the key (span)
        int diff = size - span;

        // Iterator is used to add/subtract diff as we move through distorting it
        // polarity of the iterator is determined by whether distortion needs to move
        // up or down i.e RGB values increase or decrease
        int iterator = -1;
        if (diff < 0) {
            iterator = 1;
        }

        Color targ;

        Random rand = new Random(12323424);
        while(diff != 0) {
            // Picks a random pixel in the first row
            int select = rand.nextInt(this.x);
            targ = this.colors[select];
            int newShade;
            // Picks which channel to update depending on value of diff
            if (Math.abs(diff) >= 25) {
                newShade = targ.getRed() + iterator;
                if (newShade>=0 && newShade<256) {
                    this.colors[select] = new Color(newShade, targ.getGreen(), targ.getBlue());
                    diff += iterator*25;
                }
            }
            else if (Math.abs(diff) >= 5) {
                newShade = targ.getGreen() + iterator;
                if (newShade>=0 && newShade<256) {
                    this.colors[select] = new Color(targ.getRed(), newShade, targ.getBlue());
                    diff += iterator*5;
                }
            }
            else {
                newShade = targ.getBlue() + iterator;
                if (newShade>=0 && newShade<256) {
                    this.colors[select] = new Color(targ.getRed(), targ.getGreen(), newShade);
                    diff += iterator*1;
                }
            }
        }

        // Check first row value is correct
        int g = 0;
        for (int i =0; i<this.x; i++) {
            Color curr = colors[i];
            g += curr.getRed()*25 + curr.getGreen()*5 + curr.getBlue()*1;
        }
        System.out.println("OP G IS " + g);
        System.out.println("OP G mod IS " + (g % this.x));
        


        int c = 0;   
        int index = 0;
        rand = new Random(444478);
        
        // Reads through every character
        while((c = br.read()) != -1)         //Read char by Char
        {   
            size = 0;
            for (int i = this.x + index*span; this.x + i<(index+1)*span;i++) {
                Color curr = colors[i];
                size += 25*curr.getRed() + 5*curr.getGreen() + curr.getBlue();
            }
            // mod 256 as we are encoding on 256 to use ASCII representation of nums
            size %= 256;
            // converts read character to its int value to get diff
            diff = size - (int)c;
            iterator = -1;
            if (diff < 0) {
                iterator = 1;
            }

            while(diff != 0) {
                int select = this.x + index*span+rand.nextInt(span);
                targ = this.colors[select];
                int newShade;

                // Picks which channel to update depending on value of diff
                if (Math.abs(diff) >= 25) {
                    newShade = targ.getRed() + iterator;
                    if (newShade>=0 && newShade<256) {
                        this.colors[select] = new Color(newShade, targ.getGreen(), targ.getBlue());
                        diff += iterator*25;
                    }
                }
                else if (Math.abs(diff) >= 5) {
                    newShade = targ.getGreen() + iterator;
                    if (newShade>=0 && newShade<256) {
                        this.colors[select] = new Color(targ.getRed(), newShade, targ.getBlue());
                        diff += iterator*5;
                    }
                }
                else {
                    newShade = targ.getBlue() + iterator;
                    if (newShade>=0 && newShade<256) {
                        this.colors[select] = new Color(targ.getRed(), targ.getGreen(), newShade);
                        diff += iterator*1;
                    }
                }
                }
                index++;
        }
        br.close();
        System.out.println("fin");
    }

    public void generateImage(String filename) throws IOException {
        
        // Initialize BufferedImage, assuming Color[][] is already properly populated.
        BufferedImage bufferedImage = new BufferedImage(this.x, this.y,
                BufferedImage.TYPE_INT_RGB);

        // Set each pixel of the BufferedImage to the color from the Color[][].
        for (int y = 0; y < this.y; y++) {
            for (int x = 0; x < this.x; x++) {
                bufferedImage.setRGB(x, y, colors[this.x*y + x].getRGB());
            }
        }
        File outputfile = new File("Encrypted"+ filename + ".png");
        ImageIO.write(bufferedImage, "png", outputfile);
    }

    public static void main(String args[]) throws IOException {
        File file = new File("lime.png");
        File text = new File("Communism.txt");

        EncryptImage cs = new EncryptImage(file, text, "limeCommunism");
    
        File cipherImg = new File("EncryptedlimeCommunism.png");
        DecryptImage dddd = new DecryptImage(cipherImg);
    }
}
