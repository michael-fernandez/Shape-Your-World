/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.LinkedList;

/**
 *
 * @author David Osinski
 */
public class HailGenerator {

    private static final int HAIL_MAX = 1000;
    public static final int TIME_TO_LIVE = 10000;//milliseconds
    public static int numOfHail = 0;
    private Main main;
    private Vector2f area;//area in which the hail will fall in
    private Vector3f position;//central position where the hail will be generated
    private Vector3f offset;
    private LinkedList<Ball> hailList;

    public HailGenerator(Main main, Vector2f area, Vector3f position) {
        this.main = main;
        this.area = area;
        this.position = position;
        hailList = new LinkedList<Ball>();

    }

    private void generateHail() {

        offset = new Vector3f((float) (2.0 * Math.random() * area.x - area.x + position.x),
                position.z,
                (float) (2.0 * Math.random() * area.y - area.y + position.z));

        Ball hail = new Ball(main, offset, System.currentTimeMillis());
        hailList.add(hail);
        
        numOfHail++;
    }

    public void update() {
        
        int limit = (int)(Math.random()*100+1);
        
        for(int i=0; i<limit && numOfHail<HAIL_MAX; i++){
            generateHail();
        }
        
        //remove hail
        long time = System.currentTimeMillis();
        boolean flag = true;
        while (flag) {
            if ( (time - hailList.getFirst().timestamp) > TIME_TO_LIVE) {
                Ball b = hailList.getFirst();
                b.remove();
                hailList.removeFirst();
                b = null;
                numOfHail--;
            }else{
                flag=false;
            }
        }
    }
}
