package mygame;

import com.jme3.math.FastMath;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.JMException;

public class ModifiedMidpointDisplacementHeightMap extends AbstractHeightMap {

    private static final Logger logger = Logger.getLogger(ModifiedMidpointDisplacementHeightMap.class.getName());
    private float range; // The offset in which randomness will be added [hillness]
    private float persistence; // How the random offset evolves with increasing passes [roughness/spikeness]
    private long seed; // seed for random number generator
    private float [][] heightValues;

    /**
     * The constructor generates the heightmap. After the first 4 corners are
     * randomly given an height, the center will be heighted to the average of
     * the 4 corners to which a random value is added. Then other passes fill
     * the heightmap by the same principle. The random value is generated
     * between the values
     * <code>-range</code> and
     * <code>range</code>. The
     * <code>range</code> parameter is multiplied by the
     * <code>persistence</code> parameter each pass to smoothen close cell
     * heights. Extends this class and override the getOffset function for more
     * control of the randomness (you can use the coordinates and/or the
     * computed average height to influence the random amount added.
     *
     * @param size The size of the heightmap, must be 2^N+1
     * @param range The range in which randomness will be added. A value of 1
     * will allow -1 to 1 value changes.
     * @param persistence The factor by which the range will evolve at each
     * iteration. A value of 0.5f will halve the range at each iteration and is
     * typically a good choice
     * @param seed A seed to feed the random number generator.
     * @throw JMException if size is not a power of two plus one.
     */
    public ModifiedMidpointDisplacementHeightMap(int size, float range, float persistence, long seed, float[][] heightValues) throws Exception {
        if (size < 0 || !FastMath.isPowerOfTwo(size - 1)) {
            throw new JMException("The size is negative or not of the form 2^N +1"
                    + " (a power of two plus one)");
        }
        this.heightValues = heightValues;
        this.size = size;
        this.range = range; //hillness
        this.persistence = persistence; //roughness
        this.seed = seed;
        load();
    }

    //if you don't specify the seed
    public ModifiedMidpointDisplacementHeightMap(int size, float range, float persistence, float[][] heightValues) throws Exception {
        this(size, range, persistence, new Random().nextLong(), heightValues);
    }

    //Generate the heightmap
    @Override
    public boolean load() {
        // clean up data if needed.
        if (null != heightData) {
            unloadHeightMap();
        }
        heightData = new float[size * size];
        float[][] tempBuffer = new float[size][size];
        Random random = new Random(seed);

//        tempBuffer[0][0] = heightValues[0][0];
//        tempBuffer[0][size - 1] = heightValues[0][heightValues.length-1];
//        tempBuffer[size - 1][0] = heightValues[heightValues.length-1][0];
//        tempBuffer[size - 1][size - 1] = heightValues[heightValues.length-1][heightValues.length-1];
        
       for (int i = 0; i < heightValues.length; i++) {
            for (int j = 0; j < heightValues.length; j++) {
                tempBuffer[i*(size/(heightValues.length-1))][j*(size/(heightValues.length-1))] = heightValues[i][j];
            }
        }

        float offsetRange = range;
        int stepSize = (heightValues.length-1);
        //initialize the key points you want for height field
//        for(int i=0;i<heightValues.length;i++){
//            for(int j=0;j<heightValues.length;j++){
//                setHeightAtPoint((float)heightValues[i][j],i*(size/(heightValues.length-1)),j*(size/(heightValues.length-1)));
//            }
//        }
        //starts after the iteration that would initialize the heightValue points we specify
        while (stepSize > 1) {
            int[] nextCoords = {0, 0};
            while (nextCoords != null) {
                nextCoords = doSquareStep(tempBuffer, nextCoords, stepSize, offsetRange, random);
            }
            nextCoords = new int[]{0, 0};
            while (nextCoords != null) {
                nextCoords = doDiamondStep(tempBuffer, nextCoords, stepSize, offsetRange, random);
            }
            stepSize /= 2;
            offsetRange *= persistence;
        }

        //sets the rest of the points [have to account for the fact that we initialize points but not sure how to do yet.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                setHeightAtPoint((float) tempBuffer[i][j], i, j);
            }
        }

        normalizeTerrain(NORMALIZE_RANGE);

        logger.log(Level.INFO, "Midpoint displacement heightmap generated");
        return true;
    }

    /**
     * Will fill the value at (coords[0]+stepSize/2, coords[1]+stepSize/2) with
     * the average from the corners of the square with topleft corner at
     * (coords[0],coords[1]) and width of stepSize.
     *
     * @param tempBuffer the temprary heightmap
     * @param coords an int array of lenght 2 with the x coord in position 0
     * @param stepSize the size of the square
     * @param offsetRange the offset range within a random value is picked and
     * added to the average
     * @param random the random generator
     * @return
     */
    protected int[] doSquareStep(float[][] tempBuffer, int[] coords, int stepSize, float offsetRange, Random random) {
        float cornerAverage = 0;
        int x = coords[0];
        int y = coords[1];
        cornerAverage += tempBuffer[x][y];
        cornerAverage += tempBuffer[x + stepSize][y];
        cornerAverage += tempBuffer[x + stepSize][y + stepSize];
        cornerAverage += tempBuffer[x][y + stepSize];
        cornerAverage /= 4;
        float offset = getOffset(random, offsetRange, coords, cornerAverage);
        tempBuffer[x + stepSize / 2][y + stepSize / 2] = cornerAverage + offset;

        // Only get to next square if the center is still in map
        if (x + stepSize * 3 / 2 < size) {
            return new int[]{x + stepSize, y};
        }
        if (y + stepSize * 3 / 2 < size) {
            return new int[]{0, y + stepSize};
        }
        return null;
    }

    //Will fill the cell at (x+stepSize/2, y) with the average of the 4 corners
    //of the diamond centered on that point with width and height of stepSize.
    protected int[] doDiamondStep(float[][] tempBuffer, int[] coords, int stepSize, float offsetRange, Random random) {
        int cornerNbr = 0;
        float cornerAverage = 0;
        int x = coords[0];
        int y = coords[1];
        int[] dxs = new int[]{0, stepSize / 2, stepSize, stepSize / 2};
        int[] dys = new int[]{0, -stepSize / 2, 0, stepSize / 2};

        for (int d = 0; d < 4; d++) {
            int i = x + dxs[d];
            if (i < 0 || i > size - 1) {
                continue;
            }
            int j = y + dys[d];
            if (j < 0 || j > size - 1) {
                continue;
            }
            cornerAverage += tempBuffer[i][j];
            cornerNbr++;
        }
        cornerAverage /= cornerNbr;
        float offset = getOffset(random, offsetRange, coords, cornerAverage);
        tempBuffer[x + stepSize / 2][y] = cornerAverage + offset;

        if (x + stepSize * 3 / 2 < size) {
            return new int[]{x + stepSize, y};
        }
        if (y + stepSize / 2 < size) {
            if (x + stepSize == size - 1) {
                return new int[]{-stepSize / 2, y + stepSize / 2};
            } else {
                return new int[]{0, y + stepSize / 2};
            }
        }
        return null;
    }

    //Generate a random value to add to the computed average
    protected float getOffset(Random random, float offsetRange, int[] coords, float average) {
        return 2 * (random.nextFloat() - 0.5F) * offsetRange;
    }

    public float getPersistence() {
        return persistence;
    }

    public void setPersistence(float persistence) {
        this.persistence = persistence;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}
