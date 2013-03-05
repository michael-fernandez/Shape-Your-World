package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.texture.Texture;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import readimg.IMGData;

public class Main extends SimpleApplication{

    IMGData imgData;
    BulletAppState bulletAppState;
    
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
        //set up the physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        
        
        try {
            //get image data
            imgData = new IMGData(); //Koala image is 1024 x 768
            /*
            int HeightMatrixSize = 17; //what size you want to represent your photo 
            //create matrices to reduce grayscale data to size picked above
            int [][] FirstMatrix = new int[HeightMatrixSize][imgData.gv.length];
            for(int i=0;i<FirstMatrix.length;i++){
                for(int j=0;j<FirstMatrix[0].length;j++){
                    if(i == j){
                        FirstMatrix[i][j] = 1;
                    } else {
                        FirstMatrix[i][j] = 0;
                    }
                }
            }
            int [][] SecondMatrix = new int[imgData.gv[0].length][HeightMatrixSize];
            for(int i=0;i<SecondMatrix.length;i++){
                for(int j=0;j<SecondMatrix[0].length;j++){
                    if(i == j){
                        SecondMatrix[i][j] = 1;
                    } else {
                        SecondMatrix[i][j] = 0;
                    }
                }
            }
            //Figure out how to multiply together the same way matrices would
            
            //a 17x1024 identity matrix times gv times 768x17 to get hightValues;
            int [][] heightValues = new int[HeightMatrixSize][HeightMatrixSize]; //first set of height values for fractle
            */
            //test: WORKS :D
            int[][] heightValues = new int[17][17];
            heightValues[8][8] = 200;
            //create fractal height field
            int size = imgData.gv.length+1;
            float range = 0;//1.0f;
            float roughness = 0.5f;
            float normalizer = 500.0f;
            float waterLevel = normalizer / 3f;
            //only want recursion to occur after (17-1)^2 times for a 17x17 and we want to control the heightmap with the grey scale [preset size]
            AbstractHeightMap heightmap = initFractalHeightMap(size, range, roughness, normalizer, waterLevel, heightValues);

            int patchSize = 65;
            TerrainQuad terrain = new TerrainQuad("my terrain", patchSize, size, heightmap.getHeightMap());
            TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
            terrain.addControl(control);

            Material mat = initMaterial(size, waterLevel, normalizer);
            terrain.setMaterial(mat);
            terrain.setLocalTranslation(0, -normalizer, 0);
            terrain.setLocalScale(2f, 1f, 2f);

            rootNode.attachChild(terrain);
            flyCam.setMoveSpeed(100);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    public AbstractHeightMap initFractalHeightMap(int size, float range, float roughness, float normalizer, float waterLevel, int[][] heightValues) {
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ModifiedMidpointDisplacementHeightMap(size, range, roughness, heightValues);
        } catch (Exception ex) {
        }
        // normalize
        heightmap.normalizeTerrain(normalizer);

        // flood
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (heightmap.getTrueHeightAtPoint(x, z) < waterLevel) {
                    heightmap.setHeightAtPoint(waterLevel, x, z);
                }
            }
        }
        heightmap.erodeTerrain();
        return (heightmap);
    }

    private Material initMaterial(int terrainSize, float waterLevel, float normalizer) {
        // the material and its definitions can be found in:
        // jme3-libraries - jme3-terrain.jar
        // look at the j3md file to find the parameters
        Material mat = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        Texture rock = assetManager.loadTexture("Textures/DirtWater.jpg");
        rock.setWrap(Texture.WrapMode.Repeat);
        Texture dirtWater = assetManager.loadTexture("Textures/Test.jpg");
        dirtWater.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("region1ColorMap", dirtWater);
        mat.setTexture("region2ColorMap", grass);
        mat.setTexture("region3ColorMap", dirt);
        mat.setTexture("region4ColorMap", rock);
        mat.setTexture("slopeColorMap", dirt);
        //
        float step = (normalizer - waterLevel) / 3f;
        mat.setVector3("region1", new Vector3f(0, waterLevel, 512f)); //startheight, endheight, scale
        mat.setVector3("region2", new Vector3f(waterLevel, waterLevel + step, 32f)); //startheight, endheight, scale
        mat.setVector3("region3", new Vector3f(waterLevel + step, waterLevel + 2 * step, 64f)); //startheight, endheight, scale
        mat.setVector3("region4", new Vector3f(waterLevel + 2 * step, normalizer, 32f)); //startheight, endheight, scale
        //
        mat.setFloat("terrainSize", terrainSize);
        mat.setFloat("slopeTileFactor", 32f);
        return (mat);
    }
}
