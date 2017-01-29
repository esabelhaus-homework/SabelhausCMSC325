package mygame;

import appstate.InputAppState;
import physics.PhysicsTestHelper;
import characters.MyGameCharacterControl;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    protected BulletAppState bulletAppState;
    private Vector3f normalGravity = new Vector3f(0, -9.81f, 0);

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial scene = assetManager.loadModel("Scenes/HwTwoScene.j3o");

        rootNode.attachChild(scene);

        bulletAppState = new BulletAppState(); //Allows for the use of Physics simulation
        stateManager.attach(bulletAppState);
        //stateManager.detach(stateManager.getState(FlyCamAppState.class));

        //Create the Physics World based on the Helper class
        PhysicsTestHelper.createPhysicsTestWorldSoccer(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        
        //Add a custom font and text to the scene
        BitmapFont myFont = assetManager.loadFont("Interface/Fonts/Monospaced.fnt");
        
        BitmapText hitsText = new BitmapText(myFont, true);
        hitsText.setText("HITS : 0");
        hitsText.setName("Hits");
        hitsText.setColor(ColorRGBA.Magenta);
        hitsText.setSize(guiFont.getCharSet().getRenderedSize());
        hitsText.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() - hitsText.getLineHeight(), 0f);
        guiNode.attachChild(hitsText);
        
        InputAppState appState = new InputAppState(hitsText);
        
        //Add the Player to the world and use the customer character and input control classes
        Node playerOneNode = (Node) assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");

        MyGameCharacterControl charOneControl = new MyGameCharacterControl(0.5f, 2.5f, 5f, 0.0f , 1.8f, 3.0f, "Sinbad");
        charOneControl.setCamera(cam);
        playerOneNode.addControl(charOneControl);
        charOneControl.setGravity(normalGravity);
        bulletAppState.getPhysicsSpace().add(charOneControl);
        appState.setCharacterOne(charOneControl);
        rootNode.attachChild(playerOneNode);
        
        Node playerTwoNode = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        MyGameCharacterControl charTwoControl = new MyGameCharacterControl(0.5f, 2.5f, 5f, 0.0f , 1.8f, 3.0f, "Otto");
        charTwoControl.setCamera(cam);
        playerTwoNode.addControl(charTwoControl);
        charTwoControl.setGravity(normalGravity);
        bulletAppState.getPhysicsSpace().add(charTwoControl);
        appState.setCharacterTwo(charTwoControl);
        rootNode.attachChild(playerTwoNode);
        
        stateManager.attach(appState);

        //Add the "bullets" to the scene to allow the player to shoot the balls
        PhysicsTestHelper.createBallShooter(this, rootNode, bulletAppState.getPhysicsSpace());
        
        BitmapText hudText = new BitmapText(myFont, true);
        hudText.setText("CMSC325 Week3 Physics Intro !\n\n\t\t+");
        hudText.setColor(ColorRGBA.Red);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());

        //Set the text in the middle of the screen
        hudText.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() / 2 + hudText.getLineHeight(), 0f); //Positions text to middle of screen
        guiNode.attachChild(hudText);
        
        List<Spatial> targets = new ArrayList<Spatial>();
        
        // create list of targets for app state
        targets.add(rootNode.getChild("Box"));
        targets.add(rootNode.getChild("Otto"));
        targets.add(rootNode.getChild("Sinbad"));
        targets.add(rootNode.getChild("Soccer ball"));
        targets.add(rootNode.getChild("Dome"));
        targets.add(rootNode.getChild("Ring"));
        targets.add(rootNode.getChild("Sphere"));
        
        // set targets on app state
        appState.setTargets(targets);
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
