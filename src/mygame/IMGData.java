/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.io.IOException;

public class IMGData {

    public int[][] gv;

    public IMGData() throws IOException {
        //load your image.
        //this creates a color and grayvalue version.
        IMGConverter img = new IMGConverter("Koala.jpg");
        
        //looks fancy [creates better grayscale values]
        img.invert();
        
        //show the image
        //img.show();
        
        //to get the grayvalues, call getGrayValues().
        //this returns a Column x Rows (!!!observe the order!!!) array
        //of values 0..255
        gv = img.getGrayValues();
    }
}
