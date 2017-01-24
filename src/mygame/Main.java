package mygame;

import appstate.InputAppState;
import physics.PhysicsTestHelper;
import characters.MyGameCharacterControl;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

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
//        Box b = new Box(1, 1, 1);
//        Geometry geom = new Geometry("Box", b);
//
//        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setColor("Color", ColorRGBA.Blue);
//        geom.setMaterial(mat);
        Spatial scene = assetManager.loadModel("Scenes/HwTwoScene.j3o");

        rootNode.attachChild(scene);

        bulletAppState = new BulletAppState(); //Allows for the use of Physics simulation
        stateManager.attach(bulletAppState);
        //stateManager.detach(stateManager.getState(FlyCamAppState.class));

        //Create the Physics World based on the Helper class
        PhysicsTestHelper.createPhysicsTestWorldSoccer(rootNode, assetManager, bulletAppState.getPhysicsSpace());

        //Add the Player to the world and use the customer character and input control classes
        Node playerNode = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        MyGameCharacterControl charControl = new MyGameCharacterControl(0.5f, 2.5f, 8f);
        charControl.setCamera(cam);
        playerNode.addControl(charControl);
        charControl.setGravity(normalGravity);
        bulletAppState.getPhysicsSpace().add(charControl);

        InputAppState appState = new InputAppState();
        appState.setCharacter(charControl);
        stateManager.attach(appState);
        rootNode.attachChild(playerNode);

        //Add the "bullets" to the scene to allow the player to shoot the balls
        PhysicsTestHelper.createBallShooter(this, rootNode, bulletAppState.getPhysicsSpace());

        //Add a custom font and text to the scene
        BitmapFont myFont = assetManager.loadFont("Interface/Fonts/Monospaced.fnt");

        BitmapText hudText = new BitmapText(myFont, true);
        hudText.setText("CMSC325 Week3 Physics Intro!\n\n\t\t+");
        hudText.setColor(ColorRGBA.Red);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());

        //Set the text in the middle of the screen
        hudText.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() / 2 + hudText.getLineHeight(), 0f); //Positions text to middle of screen
        guiNode.attachChild(hudText);

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
