package mygame;

import appstate.InputAppState;
import characters.AICharacterControl;
import physics.PhysicsTestHelper;
import characters.MyGameCharacterControl;
import characters.NavMeshNavigationControl;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
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
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    protected BulletAppState bulletAppState;
    private Vector3f normalGravity = new Vector3f(0, -9.81f, 0);
    private InputAppState appState;
    private Node scene;
    
    private String playerInitials;
    private static java.io.File file;
    private static PrintWriter positionFile;
    private HudText gameHud;
    private ArrayList<Score> scores = new ArrayList<Score>();
    private java.io.File recordFile = new java.io.File("records.txt");
    private static PrintWriter recordFileWriter;
    private BallStateText[] ballsText;
    private Spatial[] pathFinderTargets;
    private Node targets;
    private int numBalls = 4;
    private NavMeshNavigationControl navMesh;
    float shortest;
    private int gameTimeCount;

    // intiate the variable for storing time
    private long startTime;
    private long estimatedTime;
    
    public static void main(String[] args) {
        file = new java.io.File("positions.txt");
        try {
            positionFile = new PrintWriter(file);
            recordFileWriter = new PrintWriter(new BufferedWriter(new FileWriter("records.txt", true)));
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        getFlyByCamera().setMoveSpeed(45f);
        cam.setLocation(new Vector3f(50, 40, -10));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
        
        targets = new Node("shootable targets");
        
        rootNode.attachChild(targets);
        
        pathFinderTargets = new Spatial[numBalls];
        ballsText = new BallStateText[numBalls];
        
        scene = setupWorld();

        // create hit counter
        gameHud = new HudText();

        //Add a custom font and text to the scene
        BitmapFont myFont = assetManager.loadFont("Interface/Fonts/Monospaced.fnt");

        // create appstate for basic characters
        appState = new InputAppState();
        createOtto();
        createSinbad();
        stateManager.attach(appState);
        
        BitmapText crosshair = new BitmapText(myFont, true);
        crosshair.setText("+");
        crosshair.setColor(ColorRGBA.Red);
        crosshair.setSize(guiFont.getCharSet().getRenderedSize());
        crosshair.setLocalTranslation(settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2, settings.getHeight() / 2 + crosshair.getLineHeight() / 2, 0);
        guiNode.attachChild(crosshair);
        
        playerInitials = JOptionPane.showInputDialog(null, "What are your initials?", "Enter player initials", JOptionPane.QUESTION_MESSAGE);
        
        // display instructuons
        JOptionPane.showMessageDialog(null,
                    "Welcome " + playerInitials + "\n"
                    + "You've got 60 seconds to shoot as many targets as you can!\n"
                    + "Targets are smiley faced balls, and Jaime the monkey\n"
                    + "Good Luck!",
                    "Shooting Targets", JOptionPane.INFORMATION_MESSAGE);
        
        String recordsLine = "Top 5 Players :";
        
        if (recordFile.canRead()) {
            FileReader rec;
            BufferedReader br;
            String line;
            try {
               rec = new FileReader(recordFile.toString());
               br = new BufferedReader(rec);
               while ((line = br.readLine()) != null) {
                    String lineParts[] = line.split(":");
                    scores.add(new Score(lineParts[0], Integer.parseInt(lineParts[1])));
                }
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // sort scores
        Collections.sort(scores, new Comparator<Score>() {
            @Override
            public int compare(Score o1, Score o2) {
                return o1.getScore().compareTo(o2.getScore());
            }
        });
        
        // reverse to descending
        Collections.reverse(scores);
        
        // get top 5 scores
        int inc = 0;
        for (Score s: scores) {
            if (inc == 5) {
                break;
            } else {
                recordsLine += "\n" + s.getScoreString();
            }
            inc++;
        }

        PlayerRecords displayRecords = new PlayerRecords(recordsLine, settings.getMinWidth(), settings.getHeight(), 0);
        
        initGameTargets();

        DirectionalLight l = new DirectionalLight();
        rootNode.addLight(l);
        createBalls();
        setupCharacter(scene);
    }
    
    private void createOtto() {
        // create Otto character        
        Node playerTwoNode = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        playerTwoNode.setName("Otto");
        playerTwoNode.setLocalTranslation(PhysicsTestHelper.generateRandomXYZ());
        MyGameCharacterControl charTwoControl = new MyGameCharacterControl(3, 10, 30, "Otto");
        charTwoControl.setCamera(cam);
        playerTwoNode.addControl(charTwoControl);
        charTwoControl.setGravity(normalGravity);
        bulletAppState.getPhysicsSpace().add(charTwoControl);
        bulletAppState.getPhysicsSpace().addAll(playerTwoNode);
        appState.setCharacterTwo(charTwoControl);
    }
    
    private void createSinbad() {
        // create sinbad character
        Node playerOneNode = (Node) assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        playerOneNode.setLocalTranslation(PhysicsTestHelper.generateRandomXYZ());
        playerOneNode.setName("Sinbad");
        MyGameCharacterControl charOneControl = new MyGameCharacterControl(3, 10, 30, "Sinbad");
        charOneControl.setCamera(cam);
        playerOneNode.addControl(charOneControl);
        charOneControl.setGravity(normalGravity);
        bulletAppState.getPhysicsSpace().add(charOneControl);
        bulletAppState.getPhysicsSpace().addAll(playerOneNode);
        appState.setCharacterOne(charOneControl);
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
        PhysicsTestHelper.createPhysicsWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());

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
        aiCharacter.setLocalTranslation(PhysicsTestHelper.generateRandomXYZ());
        aiCharacter.setLocalScale(2f);

        AICharacterControl physicsCharacter = new AICharacterControl(0.6f, 3.5f, 8f);
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
            ballsText[x] = new BallStateText(x, settings.getWidth() - 325, settings.getHeight() - x*25, 0);
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
                    bulletControl.setLinearVelocity(getCamera().getDirection().mult(400));
                    bulletg.addControl(bulletControl);
                    rootNode.attachChild(bulletg);
                    space.add(bulletControl);
                    gameHud.shot();
                }
                
                // Reset results list.
                CollisionResults results = new CollisionResults();

                // Aim the ray from cam loc to cam direction.
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                
                targets.collideWith(ray, results);
                if (results.size() > 0) {
                    for (int i = 0; i < results.size(); i++) {
                        String whatWasHit = results.getCollision(i).getGeometry().getName();
                        hitAndMoveTarget(whatWasHit);
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
    
    private void hitAndMoveTarget(String whatWasHit) {
        if ("Target One".equals(whatWasHit)) {
            shuffleBall(whatWasHit);
            targets.attachChild(PhysicsTestHelper.createTargetOne(rootNode, assetManager, bulletAppState.getPhysicsSpace()));
            gameHud.points(10, "one");
        } else if ("Target Two".equals(whatWasHit)) {
            shuffleBall(whatWasHit);
            targets.attachChild(PhysicsTestHelper.createTargetTwo(rootNode, assetManager, bulletAppState.getPhysicsSpace()));
            gameHud.points(15, "two");            
        } else if ("Target Three".equals(whatWasHit)) {
            shuffleBall(whatWasHit);
            targets.attachChild(PhysicsTestHelper.createTargetThree(rootNode, assetManager, bulletAppState.getPhysicsSpace()));
            gameHud.points(20, "three");
        } else if ("Target Four".equals(whatWasHit)) {
            shuffleBall(whatWasHit);
            targets.attachChild(PhysicsTestHelper.createTargetFour(rootNode, assetManager, bulletAppState.getPhysicsSpace()));
            gameHud.points(25, "four");
        } else if (whatWasHit.toLowerCase().contains("jaimegeom")) {
            System.out.println("!!!!!!!!!!!!!!!!!!!Hit Jaime!!!!!!!!!!!!!!");
            bulletAppState.getPhysicsSpace().remove(targets.getChild("Jaime").getControl(AICharacterControl.class));
            targets.detachChild(targets.getChild("Jaime"));
            setupCharacter(scene);
            gameHud.points(50, "five");
        }
        
        gameHud.hit();
    }
    
    private void shuffleBall(String whatWasHit) {
        bulletAppState.getPhysicsSpace().remove(targets.getChild(whatWasHit).getControl(RigidBodyControl.class));
        targets.detachChild(targets.getChild(whatWasHit));
    }
    
    // storage object for a single score
    public class Score {
        private String initials;
        private int score;
        
        Score(String theseInitials, int thisScore) {
            this.initials = theseInitials;
            this.score = thisScore;
        }
        
        public Integer getScore() {
            return score;
        }
        
        public String getScoreString() {
            return initials + ":" + score;
        }
    }
    
    // heads up display for this game
    public class HudText {
        private BitmapText hitText;
        int hitCount = 0;
        int shotsFired = 0;
        int pointsEarned = 0;
        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;
        int five = 0;
        
        HudText() {
            this.hitText = new BitmapText(guiFont, false);
            this.hitText.setText(
                    "Time Remaining : " + timeRemaining() + 
                    "\nShots : 0\nHits : 0\nPoints Earned : 0");
            this.hitText.setColor(ColorRGBA.Magenta);
            this.hitText.setSize(guiFont.getCharSet().getRenderedSize());
            this.hitText.setLocalTranslation(settings.getWidth() / 2 - 100, settings.getHeight() - hitText.getLineHeight(), 0f);
            guiNode.attachChild(hitText);
        }
        
        private void setText() {
            this.hitText.setText(
                    "Time Remaining : " + timeRemaining() + 
                    "\nShots : " + shotsFired + 
                    "\nHits : " + hitCount + 
                    "\nMustachio Smiley: " + one +
                    "\nEvil Smiley: " + two +
                    "\nBatman Smiley: " + three +
                    "\nCool Smiley: " + four +
                    "\nJaime: " + five +
                    "\nTotal Points Earned : " + pointsEarned);
        }
        
        // update the time
        public void tick() {
            setText();
        }
        
        // add a hit to the tracker
        public void hit() {
            this.hitCount++;
            setText();
        }
        
        // add a shot to the tracker
        public void shot() {
            this.shotsFired++;
            setText();
        }
        
        // update the points gained
        public void points(int howMany, String target) {
            pointsEarned += howMany;
            if ("one".equals(target)) {
                one += howMany;
            } else if ("two".equals(target)) {
                two += howMany;
            } else if ("three".equals(target)) {
                three += howMany;
            } else if ("four".equals(target)) {
                four += howMany;
            } else if ("five".equals(target)) {
                five += howMany;
            }
            setText();
        }
        
        public int getShots() {
            return shotsFired;
        }
        
        public int getHits() {
            return hitCount;
        }
        
        public double getAccuracy() {
            return shotsFired / hitCount * 100;
        }
        
        // get how many secods are remaining
        private int timeRemaining() {
            return 60 - gameTimeCount;
        }
        
        // get the number of points the user earned
        public int getPoints() {
            return pointsEarned;
        }
    }

    // player records display
    public class PlayerRecords {
        private BitmapText pastPlayers;
        
        PlayerRecords(String myPlayers, float xPos, float yPos, float zPos) {
            pastPlayers = new BitmapText(guiFont, false);
            pastPlayers.setText(myPlayers);
            pastPlayers.setColor(ColorRGBA.Magenta);
            pastPlayers.setSize(guiFont.getCharSet().getRenderedSize());
            pastPlayers.setLocalTranslation(xPos, yPos, zPos);
            guiNode.attachChild(pastPlayers);
        }
    }
    
    // where is this ball
    public class BallStateText {

        private BitmapText ballText;
        int ballNumber;

        // This displays the positions of this ball on the GUI
        BallStateText(int whichBall, float xPos, float yPos, float zPos) {
            ballText = new BitmapText(guiFont, false);
            ballText.setSize(guiFont.getCharSet().getRenderedSize());
            ballText.setColor(ColorRGBA.Magenta);
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
        if (getTimer().getTimeInSeconds() >= 1) {
            getTimer().reset();
            gameTimeCount++;
            gameHud.tick();
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
        
        
        if (gameTimeCount >= 60) {
            recordFileWriter.println(new Score(playerInitials, gameHud.getPoints()).getScoreString());
            recordFileWriter.flush();
            recordFileWriter.close();
            // stop the game
            JOptionPane.showMessageDialog(null, "Time Is Up!");
            // Shows the final results
            JOptionPane.showMessageDialog(null,
                    "Shots Fired: " + gameHud.getShots() + "\n"
                    + "Bullets Hit: " + gameHud.getHits() + "\n"
                    + "Points: " + gameHud.getPoints(),
                    "Results:", JOptionPane.INFORMATION_MESSAGE);
            stop();
        }
    }
    
    // set the text on the gui display
    public void updatePositionDisplay() {        
        for (int i = 0; i < numBalls; i++) {
            ballsText[i].setText(pathFinderTargets[i]);
        }
    }
    
    @Override
    public void stop() {
        recordFileWriter.flush();
        recordFileWriter.close();
        System.exit(0);
    }
}
