package mygame;

import appstate.InputAppState;
import characters.AICharacterControl;
import physics.PhysicsTestHelper;
import characters.MyGameCharacterControl;
import characters.NavMeshNavigationControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    protected BulletAppState bulletAppState;
    private CollisionResults collisionResults = new CollisionResults();
    private Vector3f normalGravity = new Vector3f(0, -9.81f, 0);
    public static java.io.File file;
    private static PrintWriter positionFile;
    private HitStateText sceneHitText;
    private PlayerRecords records;
    private BallStateText[] ballsText;
    private Spatial[] pathFinderTargets;
    private Node targets;
    private int numBalls = 4;
    private NavMeshNavigationControl navMesh;
    float shortest;

    // intiate the variable for storing time
    private long startTime;
    private long estimatedTime;
    
    public static void main(String[] args) {
        file = new java.io.File("positions.txt");
        try {
            positionFile = new PrintWriter(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().addCollisionListener(this);
        //uncomment to enable debugging
        //will show all collision mesh targets
        //bulletAppState.setDebugEnabled(true);
        getFlyByCamera().setMoveSpeed(45f);
        cam.setLocation(new Vector3f(20, 60, 20));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
        
        targets = new Node("shootable targets");
        
        rootNode.attachChild(targets);
        
        pathFinderTargets = new Spatial[numBalls];
        ballsText = new BallStateText[numBalls];

        Node scene = setupWorld();

        // create hit counter
        sceneHitText = new HitStateText();

        //Add a custom font and text to the scene
        BitmapFont myFont = assetManager.loadFont("Interface/Fonts/Monospaced.fnt");

        // create appstate for basic characters
        InputAppState appState = new InputAppState();

        //Add the Player to the world and use the customer character and input control classes

        // create sinbad character
        Node playerOneNode = (Node) assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        playerOneNode.setLocalTranslation(45, 30, -15);
        MyGameCharacterControl charOneControl = new MyGameCharacterControl(3, 10, 30, "Sinbad");
        charOneControl.setCamera(cam);
        playerOneNode.addControl(charOneControl);
        charOneControl.setGravity(normalGravity);
        bulletAppState.getPhysicsSpace().add(charOneControl);
        bulletAppState.getPhysicsSpace().addAll(playerOneNode);
        appState.setCharacterOne(charOneControl);
        rootNode.attachChild(playerOneNode);

        // create Otto character        
        Node playerTwoNode = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        playerTwoNode.setLocalTranslation(-35, 30, 30);
        MyGameCharacterControl charTwoControl = new MyGameCharacterControl(3, 10, 30, "Otto");
        charTwoControl.setCamera(cam);
        playerTwoNode.addControl(charTwoControl);
        charTwoControl.setGravity(normalGravity);
        bulletAppState.getPhysicsSpace().add(charTwoControl);
        bulletAppState.getPhysicsSpace().addAll(playerTwoNode);
        appState.setCharacterTwo(charTwoControl);
        rootNode.attachChild(playerTwoNode);

        stateManager.attach(appState);

        BitmapText crosshair = new BitmapText(myFont, true);
        crosshair.setText("+");
        crosshair.setColor(ColorRGBA.Red);
        crosshair.setSize(guiFont.getCharSet().getRenderedSize());
        crosshair.setLocalTranslation(settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2, settings.getHeight() / 2 + crosshair.getLineHeight() / 2, 0);
        guiNode.attachChild(crosshair);
        
//        BitmapText hudText = new BitmapText(myFont, true);
//        hudText.setText("CMSC325 Week3 Physics Intro !\n\n\t\t+");
//        hudText.setColor(ColorRGBA.Red);
//        hudText.setSize(guiFont.getCharSet().getRenderedSize());
//        hudText.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() / 2 + hudText.getLineHeight(), 0f); //Positions text to middle of screen
//        guiNode.attachChild(hudText);

        initGameTargets();

        DirectionalLight l = new DirectionalLight();
        rootNode.addLight(l);
        setupCharacter(scene);
    }
    
    // generate physics world 
    // get targets created in that world
    // add them to target list
    // return scene complete with navmesh
    private Node setupWorld() {
        Node scene = (Node) assetManager.loadModel("Scenes/HwTwoScene.j3o");

        rootNode.attachChild(scene);
        createBallShooter();
        PhysicsTestHelper.createPhysicsWalls(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        /*ArrayList<Spatial> newTargets = */PhysicsTestHelper.createPhysicsWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());

        // add objects to target list
//        for (Spatial t: newTargets) {
//            targets.add(t);
//        }
        
        Geometry navGeom = new Geometry("NavMesh");
        navGeom.setMesh(((Geometry) scene.getChild("NavMesh")).getMesh());
        Material green = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        green.setColor("Color", ColorRGBA.Green);
        green.getAdditionalRenderState().setWireframe(true);
        navGeom.setMaterial(green);

        rootNode.attachChild(navGeom);

        Spatial terrain = scene.getChild("terrain-HwTwoScene");
        terrain.addControl(new RigidBodyControl(0));
        bulletAppState.getPhysicsSpace().addAll(terrain);
        return scene;
    }

    private void setupCharacter(Node scene) {
        // Load model, attach to character node
        Node aiCharacter = (Node) assetManager.loadModel("Models/Jaime/Jaime.j3o");
        aiCharacter.setName("Jaime");
        aiCharacter.setLocalTranslation(new Vector3f(45, 20, -25));
        aiCharacter.setLocalScale(2f);
        
        createBalls();

        AICharacterControl physicsCharacter = new AICharacterControl(0.3f, 2.5f, 8f);
        aiCharacter.addControl(physicsCharacter);
        bulletAppState.getPhysicsSpace().add(physicsCharacter);
        scene.attachChild(aiCharacter);
        navMesh = new NavMeshNavigationControl((Node) scene);

        targets.attachChild(aiCharacter);
        aiCharacter.addControl(navMesh);
    }
    
    private void initGameTargets() {
        targets.attachChild(PhysicsTestHelper.createTargetOne(rootNode, assetManager, bulletAppState.getPhysicsSpace()));
        targets.attachChild(PhysicsTestHelper.createTargetTwo(rootNode, assetManager, bulletAppState.getPhysicsSpace()));
        targets.attachChild(PhysicsTestHelper.createTargetThree(rootNode, assetManager, bulletAppState.getPhysicsSpace()));
        targets.attachChild(PhysicsTestHelper.createTargetFour(rootNode, assetManager, bulletAppState.getPhysicsSpace()));
        
    }
    
    private void createBalls() {
        for (int x = 0; x < numBalls; x++) {
            Spatial thisBall = PhysicsTestHelper.createBall("Ball"+x, rootNode, assetManager, bulletAppState.getPhysicsSpace());
            ballsText[x] = new BallStateText(x, settings.getWidth() - 400, settings.getHeight() - x*25, 0);
            pathFinderTargets[x] = thisBall;
        }
    }
    
    public void createBallShooter() {
        ActionListener actionListener = new ActionListener() {
            public void onAction(String name, boolean keyPressed, float tpf) {                
                PhysicsSpace space = bulletAppState.getPhysicsSpace();
                Sphere bullet = new Sphere(20, 20, 0.6f, true, false);
                bullet.setTextureMode(Sphere.TextureMode.Projected);
                Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                TextureKey key2 = new TextureKey("Materials/turquoise_scale_material.jpg");
                key2.setGenerateMips(true);
                Texture tex2 = assetManager.loadTexture(key2);
                mat2.setTexture("ColorMap", tex2);
                if (name.equals("shoot") && !keyPressed) {
                    Geometry bulletg = new Geometry("bullet", bullet);
                    bulletg.setMaterial(mat2);
                    bulletg.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                    bulletg.setLocalTranslation(getCamera().getLocation());
                    RigidBodyControl bulletControl = new RigidBodyControl(10);
                    bulletg.addControl(bulletControl);
                    bulletControl.setLinearVelocity(getCamera().getDirection().mult(50));
                    bulletg.addControl(bulletControl);
                    rootNode.attachChild(bulletg);
                    space.add(bulletControl);
                    sceneHitText.shot();
                }
                
                // Reset results list.
                CollisionResults results = new CollisionResults();

                // Aim the ray from cam loc to cam direction.
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                
                targets.collideWith(ray, results);
                if (results.size() > 0) {
                    for (int i = 0; i < results.size(); i++) {
                        String whatWasHit = results.getCollision(i).getGeometry().getName();
                        sceneHitText.hit();
                        break;
                    }
                } else {
                    System.out.println("Nothing Hit");
                }

            }
        };
        getInputManager().addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        getInputManager().addListener(actionListener, "shoot");
    }
    
    public class HitStateText {
        private BitmapText hitText;
        int hitCount = 0;
        int shotsFired = 0;
        
        HitStateText() {
            this.hitText = new BitmapText(guiFont, false);
            this.hitText.setText("Shots : 0\nHits : 0");
            this.hitText.setColor(ColorRGBA.Magenta);
            this.hitText.setSize(guiFont.getCharSet().getRenderedSize());
            this.hitText.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() - hitText.getLineHeight(), 0f);
            guiNode.attachChild(hitText);
        }
        
        public void hit() {
            this.hitCount++;
            this.hitText.setText("Shots : " + shotsFired + "\nHits : " + hitCount);
        }
        
        public void shot() {
            this.shotsFired++;
            this.hitText.setText("Shots : " + shotsFired + "\nHits : " + hitCount);
        }
    }

    public class PlayerRecords {
        private BitmapText pastPlayers;
        
        PlayerRecords(String myPlayers) {
            pastPlayers = new BitmapText(guiFont, false);
            pastPlayers.setText(myPlayers);
            pastPlayers.setColor(ColorRGBA.Blue);
            pastPlayers.setSize(speed);
            pastPlayers.setLocalTranslation(speed, speed, speed);
            guiNode.attachChild(pastPlayers);
        }
    }
    
    public class BallStateText {

        private BitmapText ballText;
        int ballNumber;

        // This displays the positions of this ball on the GUI
        BallStateText(int whichBall, float xPos, float yPos, float zPos) {
            ballText = new BitmapText(guiFont, false);
            ballText.setSize(guiFont.getCharSet().getRenderedSize());
            ballText.setText("B" + whichBall + ": ");
            ballNumber = whichBall;
            ballText.setLocalTranslation(xPos, yPos, zPos);
            guiNode.attachChild(ballText);
        }

        // This sets the text of the ballText for each this ball
        public void setText(Spatial ball) {
            String pos = ball.getLocalTranslation().toString();
            ballText.setText("Ball " + ballNumber + ": " + pos);
        }
    }
    
    public String getPosStr(Spatial ball_spatial) {
        return ball_spatial.getLocalTranslation().toString();
    }

    @Override
    public void simpleUpdate(float tpf) {
        estimatedTime = System.currentTimeMillis() - startTime;
        int elapsedTime = 0;
        
        // if one second has elapsed in game
        if(estimatedTime > 1000) {
            // to the text file
            updatePositionDisplay();
            // get jamie from the root node
            Spatial jaime = targets.getChild("Jaime");
            
            positionFile.println("Find The Closest Ball");
            // we dont exactly know the shortest yet so start with ball 0
            shortest = jaime.getLocalTranslation().distance(pathFinderTargets[0].getLocalTranslation());
            navMesh.moveTo(pathFinderTargets[0].getLocalTranslation());
            // print jaime's position every second 
            positionFile.println("Jamie Position: " + jaime.getLocalTranslation().toString());
            for (int i = 0; i < numBalls; i++) {
                // print "this" balls position every second
                positionFile.println("Ball " + i + ": " + getPosStr(pathFinderTargets[i]));
                float distToBall = jaime.getLocalTranslation().distance(pathFinderTargets[i].getLocalTranslation());
                if (distToBall < shortest) {
                    shortest = distToBall;
                    navMesh.moveTo(pathFinderTargets[i].getLocalTranslation());
                }
            }            
            startTime = System.currentTimeMillis();
        }
    }
    
    // set the text on the gui display
    public void updatePositionDisplay() {        
        for (int i = 0; i < numBalls; i++) {
            ballsText[i].setText(pathFinderTargets[i]);
        }
    }
//
//    @Override
//    public void simpleRender(RenderManager rm) {
//        //TODO: add render code
//    }
}
