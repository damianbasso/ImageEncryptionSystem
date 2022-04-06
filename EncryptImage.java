import java.io.*;
import java.nio.charset.Charset;
import java.util.Random;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.sound.midi.SysexMessage;

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

    //Dont know accuracy need to test
    private static long getLength(File file) throws IOException {
        FileReader fr= new FileReader(file);   //Creation of File Reader object
        BufferedReader br=new BufferedReader(fr);  //Creation of BufferedReader object
        long count = 0;             
        while(br.read() != -1)         //Read char by Char
        {
            count++;
        }
        System.out.println("MMMMMMMM = " + count);
        return count;
    }

    private void encrypt(File file) throws IllegalArgumentException, IOException {
        // FileReader fr= new FileReader(file);   //Creation of File Reader object
        // BufferedReader br=new BufferedReader(fr);  //Creation of BufferedReader object

        // Calculates the span, which is how many characters

        // Why does it break when I do /(getLength(file) - this.x)
        // I think that shit should be correct
        int span = (int)((this.colors.length -this.x) / (getLength(file))); 
       
        System.out.println("span = " + span);
        if(span == 0) {
            throw new IllegalArgumentException("Text file is too large to encrypt in image");
        }

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
        System.out.println("G IS " + g%this.x);

        int index = 0;
        rand = new Random(444478);
        
        try (InputStream in = new FileInputStream(file);
            
            Reader reader = new InputStreamReader(in, Charset.defaultCharset());
            // buffer for efficiency
            Reader buffer = new BufferedReader(reader)) {
        
            int c = 0;
            int ind = 0;
            while ((c = reader.read()) != -1) {
                // System.out.println("   " + c);

                // if (ind > 750) {
                //     return;
                // }
                ind++;
                size = 0;
                for (int i = this.x + index*span; i< this.x + (index+1)*span;i++) {
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

                // if (ind >2000 && c == 50) {
                //     System.out.println("CHUCk " + (char)c);
                // }
    
                // System.out.println((char)c);
                while(diff != 0) {
                    if (c == 115) {
                        // System.out.println("ohno" + diff);
                        size = 0;
                        for (int i = this.x + index*span; i< this.x + (index+1)*span;i++) {
                            Color curr = colors[i];
                            size += 25*curr.getRed() + 5*curr.getGreen() + curr.getBlue();
                        }
                        
                        // mod 256 as we are encoding on 256 to use ASCII representation of nums
                        size %= 256;
                        // System.out.println("SIZE IS " + size);
                    
                    }
                    int select = rand.nextInt(span);
                    targ = this.colors[this.x + index*span + select];
                    int newShade;
    
                    // Picks which channel to update depending on value of diff
                    if (Math.abs(diff) >= 25) {
                        int loop = 0;
                        while(true) {
                            // THIS LINE HAS TO BE HERE
                            targ=this.colors[this.x + index*span + select];

                            // System.out.println("gop");
                            newShade = targ.getRed() + iterator;
                            if (newShade>=0 && newShade<256) {
                                this.colors[this.x + index*span + select] = new Color(newShade, targ.getGreen(), targ.getBlue());
                                diff += iterator*25;
                                break;
                            }
                            // If the shade can't be increased due to RGB boundary, a new pixel is found
                            else {
                                // if (c== 67) {
                                //     System.out.print("Looking for red + " + loop + "   " + span);
                                // }
                                select++;
                                loop++;
                                // If we have looped through all the pixels and none are suitable,
                                // the RGB value is instead subtracted.
                                // System.out.println("ggg " + loop + "   " + span);

                                if (loop == span - 1) {
                                    // System.out.println("CHIPSANDSALAD");

                                    // System.out.print("loop=span");
                                    // Diff is >= 25, but the pixels can not support increasing red channel due to them being >256
                                    // hence, we need to reduce pixels, easiest way to do this is going to be to reverse the iterator and have
                                    // algo solve -diff
                                    
                                    // newShade = targ.getRed() - iterator*4;
                                    // this.colors[this.x + index*span + select] = new Color(newShade, targ.getGreen(), targ.getBlue());
                                    
                                    // System.out.println("initial diff " + diff);
                                    diff = flipMod256(diff);
                                    // System.out.println("flipped diff " + diff);
                                    iterator *= -1;
                                    // System.out.println("GIRAFFE");
                                    break;
            
                                    // diff -= iterator*4*25;
                                    // while (Math.abs(diff) < 128
                                    // // diff %= 256; 
                                    // if (diff > 128) {
                                    //     diff -= 256;
                                    // }
                                    // iterator = -1;
                                    // if (diff < 0) {
                                    //     iterator = 1;
                                    // }
                                    // System.out.println("dddeeerr1");
                                    // break;
                                }
                                else {
                                    select%=span;
                                    // targ=this.colors[this.x + index*span + select];
                                }
                            }
                        } 
                    }
                    else if (Math.abs(diff) >= 5) {
                        int loop = 0;
                        while(true) {
                            // System.out.println("plop");
                            
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
                                if (loop == span-1) {
                                    // System.out.println("CHIPSANDDIP");
                                    // targ=this.colors[this.x + index*span + select];
                                    newShade = targ.getGreen() - iterator*4;
                                    
                                    // if (c==107) {
                                    //     size = 0;
                                    //     for (int i = this.x + index*span; i< this.x + (index+1)*span;i++) {
                                    //         Color curr = colors[i];
                                    //         size += 25*curr.getRed() + 5*curr.getGreen() + curr.getBlue();
                                    //     }
                                    //     System.out.println("SIZE IS HERE JIASF " + size%256);
                                    // }
                                    // System.out.println("SSSSjj " + select + "   " + span);
                                    // System.out.println(this.colors[this.x + index*span + select]);

                                    this.colors[this.x + index*span + select] = new Color(targ.getRed(), newShade, targ.getBlue());
                                    // System.out.println(this.colors[this.x + index*span + select]);

                                    // if (c==107) {
                                    //     size = 0;
                                    //     for (int i = this.x + index*span; i< this.x + (index+1)*span;i++) {
                                    //         Color curr = colors[i];
                                    //         size += 25*curr.getRed() + 5*curr.getGreen() + curr.getBlue();
                                    //     }
                                    //     // System.out.println("SIZE IS HERE JIASF " + size%256);
                                    // }
                                    // System.out.println("DIFF YA " + diff);
                                    diff -= iterator*4*5;
                                    // diff %= 256; 
                                    // System.out.println("DIFF YA " + diff);
                                    if (diff > 128) {
                                        diff -= 256;
                                    }
                                    // System.out.println("DIFF YA " + diff);
                                    iterator = -1;
                                    if (diff < 0) {
                                        iterator = 1;
                                    }
                                    // System.out.println("dddeeerr2");
                                    break;
                                }
                                else {
                                    targ=this.colors[this.x + index*span + select];    
                                }
                            }
                        } 
                    }
                    else {
                        int loop = 0;
                        while(true) {
                            // System.out.println("cop");
                            newShade = targ.getBlue() + iterator;
                            if (newShade>=0 && newShade<256) {
                                this.colors[this.x + index*span + select] = new Color(targ.getRed(), targ.getGreen(), newShade);
                                diff += iterator*1;
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
                                if (loop == span -1) {
                                    newShade = targ.getBlue() - iterator*4;
                                    this.colors[this.x + index*span + select] = new Color(targ.getRed(), targ.getGreen(), newShade);
                                    diff -= iterator*4;
                                    // diff %= 256; 
                                    if (diff > 128) {
                                        diff -= 256;
                                    }
                                    iterator = -1;
                                    if (diff < 0) {
                                        iterator = 1;
                                    }
                                    // System.out.println("dddeeerr3");
                                    break;
                                }
                                else {
                                    select%=span;
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
                if (c == 115) {
                    // System.out.println("uk    " + (char)size);
                    // System.out.println("    " + size);
                    
                }
                // System.out.println("    " + (char)size);
    
                index++;
            }
            // System.out.println("NNNNNNN = " + index);
        }
        g = 0;
        for (int i =0; i<this.x; i++) {
            Color curr = colors[i];
            g += curr.getRed()*25 + curr.getGreen()*5 + curr.getBlue()*1;
        }
        System.out.println("G2 IS " + g%this.x);
        for (int p =0;p<50;p++) {
            size = 0;
            for (int i = this.x + p*span; i< this.x + (p+1)*span;i++) {
                Color curr = colors[i];
                size += 25*curr.getRed() + 5*curr.getGreen() + curr.getBlue();
            }
            // mod 256 as we are encoding on 256 to use ASCII representation of nums
            size %= 256;
            // System.out.println("ITS    " + (char)size);

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
        File text = new File("sampleInput/100thou.txt");
        EncryptImage cs = new EncryptImage(file, text, "testDrag");
    }
}
