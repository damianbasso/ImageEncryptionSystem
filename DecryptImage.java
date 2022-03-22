import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import javax.imageio.ImageIO;

public class DecryptImage {

    Color[] colors;
    int x;
    int y;

    public DecryptImage(BufferedImage cipherImage) throws IOException {
        parseImage(cipherImage);
        decode();
    }

    public DecryptImage(File file) throws IOException {
        this(ImageIO.read(file));
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

    private String decode() {
        int sum = 0;
        for (int i = 0; i< this.x; i++) {
            Color curr = colors[i];
            sum += curr.getRed()*25 + curr.getGreen()*5 + curr.getBlue()*1;
        }

        int span = sum % this.x; 
        System.out.println("Span is " + (sum%this.x));

        int index = this.x;
        int currSum = 0;
        String message = "";
        while(index < this.colors.length) {
            if((index-this.x) %span == 0) {
                int val = currSum%256;
                System.out.println(val);
                System.out.println(message);
                message += (char)val;
            }
            Color curColor = colors[index];
            currSum += 25*curColor.getRed() + 5*curColor.getGreen() + 1*curColor.getBlue();
            index++;
        }


        // System.out.println("message is = " + message);
        return message;
    }

}
