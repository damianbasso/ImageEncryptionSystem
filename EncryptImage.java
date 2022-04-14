import java.io.*;
import java.nio.charset.Charset;
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
        
        System.out.println(" dimensions = " + this.x + "  " + this.y);
        this.colors = new Color[this.x*this.y];
        for (int y = 0; y< this.y; y++) {
            for (int x = 0; x <this.x; x++) {
                int clr = image.getRGB(x, y);
                colors[y*this.x + x] = new Color(clr);
            }
        }
    }

    //Dont know accuracy need to test
    private static long getLength(File file) throws IOException {
        FileReader fr= new FileReader(file);   //Creation of File Reader object
        BufferedReader br=new BufferedReader(fr);  //Creation of BufferedReader object
        long count = 0;             
        while(br.read() != -1)         //Read char by Char
        {
            count++;
        }
        return count;
    }

    private void encrypt(File file) throws IllegalArgumentException, IOException {
        // Calculates the span, which is how many pixels each character
        // is represented over
        int span = (int)((this.colors.length -this.x) / (getLength(file))); 
       
        System.out.println("span = " + span);
        if(span == 0) {
            throw new IllegalArgumentException("Text file is too large to encrypt in image");
        }

        // Gets the initial value represented in the first row of the image
        int size = 0;
        for (int i =0; i< this.x; i++) {
            Color curr = colors[i];
            size += curr.getRed()*i + curr.getGreen()*i + curr.getBlue()*i;
            
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
        targ = this.colors[Math.abs(diff)];

        // Updates rgb values to move the represented value to our target
        while(diff != 0) {
            int newShade;

            if (targ.getRed() + iterator < 255 && targ.getRed() + iterator > 0) {
                newShade = targ.getRed() + iterator;
                this.colors[Math.abs(diff)] = new Color(newShade,targ.getGreen(),targ.getBlue());
                diff += iterator * Math.abs(diff);
            }
            else if (targ.getGreen() + iterator < 255 && targ.getGreen() + iterator > 0) {
                newShade = targ.getGreen() + iterator;
                this.colors[Math.abs(diff)] = new Color(targ.getRed(), newShade,targ.getBlue());
                diff += iterator * Math.abs(diff);
            }
            else if (targ.getBlue() + iterator < 255 && targ.getBlue() + iterator > 0){
                newShade = targ.getBlue() + iterator;
                this.colors[Math.abs(diff)] = new Color(targ.getRed(),targ.getGreen(),newShade);
                diff += iterator * Math.abs(diff);
            }
            else {

                // flip the iterator and diff if the current diff can't be inserted due to RGB boundaries
                iterator*=-1;
                if (diff <0) {
                    diff += this.x;
                }
                else {
                    diff -= this.x;
                }
            }
        }


        int index = 0;
        rand = new Random(444478);
        
        try (InputStream in = new FileInputStream(file);
            
            Reader reader = new InputStreamReader(in, Charset.defaultCharset());
            Reader buffer = new BufferedReader(reader)) {
        
            int c = 0;
            while ((c = reader.read()) != -1) {

                size = 0;
                for (int i = this.x + index*span; i< this.x + (index+1)*span;i++) {
                    Color curr = colors[i];
                    size += 25*curr.getRed() + 5*curr.getGreen() + curr.getBlue();
                }
                
                // mod 256 as we are encoding on 256 to use ASCII representation of nums
                size %= 256;
                // converts read character to its int value to get diff
                // diff is how much we need to change the target pixels to represent our desired value
                diff = size - (int)c;
                iterator = -1;
                if (diff < 0) {
                    iterator = 1;
                }

                while(diff != 0) {
                
                    int select = rand.nextInt(span);
                    targ = this.colors[this.x + index*span + select];
                    int newShade;
    
                    // Picks which channel to update depending on value of diff
                    // red (worth 5^2)
                    if (Math.abs(diff) >= 25) {
                        int loop = 0;
                        while(true) {
                            targ=this.colors[this.x + index*span + select];
                            
                            newShade = targ.getRed() + iterator;
                            if (newShade>=0 && newShade<256) {
                                this.colors[this.x + index*span + select] = new Color(newShade, targ.getGreen(), targ.getBlue());
                                diff += iterator*25;
                                break;
                            }
                            // If the shade can't be increased due to RGB boundary, a new pixel is found
                            else {
                                select++;
                                loop++;

                                // If we have looped through all the pixels and none are suitable,
                                // diff is flipped i.e if we were trying to subtract, we add to get to
                                // our desired value mod 256
                                if (loop >= span - 1) {
                                    // Diff is >= 25, but the pixels can not support increasing red channel due to them being >256
                                    // hence, we need to reduce pixels, easiest way to do this is going to be to reverse the iterator and have
                                    // the algorithm solve the other way
                                
                                    diff = flipMod256(diff);
                                    iterator *= -1;
                                    break;
                                }
                                else {
                                    select%=span;
                                }
                            }
                        } 
                    }
                    // green (worth 5^1)
                    else if (Math.abs(diff) >= 5) {
                        int loop = 0;
                        while(true) {                            
                            newShade = targ.getGreen() + iterator;
                            if (newShade>=0 && newShade<256) {
                                this.colors[this.x + index*span + select] = new Color(targ.getRed(), newShade, targ.getBlue());
                                diff += iterator*5;
                                break;
                            }
                            // If the shade can't be increased due to RGB boundary, a new pixel is found
                            else {
                                select++;
                                select%=span;
                                loop++;
                                targ=this.colors[this.x + index*span + select];

                                // If we have looped through all the pixels and none are suitable,
                                // the RGB value is instead subtracted.
                                if (loop >= span-1) {
                                    newShade = targ.getGreen() - iterator*4;
                                    this.colors[this.x + index*span + select] = new Color(targ.getRed(), newShade, targ.getBlue());
                                    diff -= iterator*4*5;
                                    if (diff > 128) {
                                        diff -= 256;
                                    }
                                    iterator = -1;
                                    if (diff < 0) {
                                        iterator = 1;
                                    }
                                    break;
                                }
                                else {
                                    targ=this.colors[this.x + index*span + select];    
                                }
                            }
                        } 
                    }
                    // blue (worth 5^0)
                    else {
                        int loop = 0;
                        while(true) {
                            newShade = targ.getBlue() + iterator;
                            if (newShade>=0 && newShade<256) {
                                this.colors[this.x + index*span + select] = new Color(targ.getRed(), targ.getGreen(), newShade);
                                diff += iterator*1;
                                break;
                            }
                            else {
                                select++;
                                select%=span;
                                loop++;
                                targ=this.colors[this.x + index*span + select];
                                // If we have looped through all the pixels and none are suitable,
                                // the RGB value is instead subtracted.
                                if (loop >= span -1) {
                                    newShade = targ.getBlue() - iterator*4;
                                    this.colors[this.x + index*span + select] = new Color(targ.getRed(), targ.getGreen(), newShade);
                                    diff -= iterator*4;
                                    if (diff > 128) {
                                        diff -= 256;
                                    }
                                    iterator = -1;
                                    if (diff < 0) {
                                        iterator = 1;
                                    }
                                    break;
                                }
                                else {
                                    targ=this.colors[this.x + index*span + select];    
                                }
                            }
                        } 
                    }
                }
               
                size = 0;
                for (int i = this.x + index*span; i< this.x + (index+1)*span;i++) {
                    Color curr = colors[i];
                    size += 25*curr.getRed() + 5*curr.getGreen() + curr.getBlue();
                }
                // mod 256 as we are encoding on 256 to use ASCII representation of nums
                size %= 256;
                index++;
            }
        }
        System.out.println("fin");
    }

    private int flipMod256(int val) {
        if (val < 0) {
            
            return val + 256;
        }
        else {
            return val - 256;
        }
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
        File outputfile = new File("outputImages/Encrypted"+ filename + ".png");
        ImageIO.write(bufferedImage, "png", outputfile);
    }

    public static void main(String args[]) throws IOException {
        File file = new File("sampleInput/HighResDragon.png");
        File text = new File("sampleInput/1598400ASCII.txt");
        EncryptImage cs = new EncryptImage(file, text, "whiteA");
    }
}
