/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author Owner
 */
public class Ball {
    private Main main;
    private Sphere sphere;
    private Geometry ball_geo;
    private Material ball_mat;
    private Node ball_node;
    
    private CollisionShape shape;
    private RigidBodyControl rigidBody;
    private GhostControl ghost;
    
    public long timestamp;
    
    
    public Ball(Main main, Vector3f position, long timestamp) {
        this.main = main;
        this.timestamp = timestamp;
        sphere = new Sphere(10, 10, 0.1f);
        ball_geo = new Geometry("hail precursor", sphere);
        ball_mat = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        ball_mat.setColor("Color", ColorRGBA.White);
        ball_geo.setMaterial(ball_mat);
        
        ball_node = new Node();
        ball_node.attachChild(ball_geo);
        
        shape = new SphereCollisionShape(sphere.radius);
        rigidBody = new RigidBodyControl(shape, 0.05f);
        ghost = new GhostControl(shape);
        
        ball_geo.addControl(rigidBody);
        ball_geo.addControl(ghost);
        
        rigidBody.setKinematic(true);//moves the ball into position
        ball_node.move(position);
        rigidBody.setKinematic(false);
        
        main.getRootNode().attachChild(ball_node);
        main.bulletAppState.getPhysicsSpace().add(rigidBody);
        main.bulletAppState.getPhysicsSpace().add(ghost);
    }
    
    public void remove(){
        main.bulletAppState.getPhysicsSpace().remove(rigidBody);
        main.bulletAppState.getPhysicsSpace().remove(ghost);
        main.getRootNode().removeFromParent();
    }
}

