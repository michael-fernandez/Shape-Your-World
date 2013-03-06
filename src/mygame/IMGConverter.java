package mygame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class IMGConverter {

    int[][][] colorImage;
    int[][] grayImage;
    int height, width;

    public IMGConverter(String filename) throws IOException {
        BufferedImage bi = javax.imageio.ImageIO.read(new File(filename));
        height = bi.getHeight();
        width = bi.getWidth();
        colorImage = new int[width][height][3];
        grayImage = new int[width][height];
        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                Color col = new Color(bi.getRGB(xx, yy));
                int r1 = col.getRed();
                int g1 = col.getGreen();
                int b1 = col.getBlue();
                colorImage[xx][yy][0] = r1;
                colorImage[xx][yy][1] = g1;
                colorImage[xx][yy][2] = b1;
                grayImage[xx][yy] = (r1+g1+b1)/3;
            }
        }
    }

    // -------------------------------------------------------------------------
    // invert color and gray image
    public void invert(){
                for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                colorImage[xx][yy][0] = 255 - colorImage[xx][yy][0];
                colorImage[xx][yy][1] = 255 - colorImage[xx][yy][1];
                colorImage[xx][yy][2] = 255 - colorImage[xx][yy][2];
                grayImage[xx][yy] = 255 - grayImage[xx][yy];
            }
        }
    }

    // -------------------------------------------------------------------------
    public int [][]getGrayValues(){
        return(grayImage);
    }

    // -------------------------------------------------------------------------
    public void show(){
        if (colorImage == null){
            return;
        }
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MyPanel mp = new MyPanel();
        f.add(mp);
        f.pack();
        f.setVisible(true);
    }

    class MyPanel extends JPanel{
        public MyPanel(){
            setPreferredSize(new Dimension(width*2, height));
        }
        public void paintComponent(Graphics g){
            for (int r = 0; r<height; r++){
                for (int c=0;c<width; c++){
                    g.setColor(new Color(colorImage[c][r][0],colorImage[c][r][1],colorImage[c][r][2]));
                    g.drawLine(c,r,c,r);
                    g.setColor(new Color(grayImage[c][r],grayImage[c][r],grayImage[c][r]));
                    g.drawLine(c+width,r,c+width,r);
                }
            }
        }
    }
}
