package physics;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Torus;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author normenhansen
 */
public class PhysicsTestHelper {

    /**
     * creates a simple physics test world with a floor, an obstacle and some
     * test boxes
     *
     * @param rootNode
     * @param assetManager
     * @param space
     */
    public static void createPhysicsTestWorld(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));

        Box floorBox = new Box(140, 0.25f, 140);
        Geometry floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setMaterial(material);
        floorGeometry.setLocalTranslation(0, -0.25f, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        rootNode.attachChild(floorGeometry);
        space.add(floorGeometry);

        //movable boxes
        for (int i = 0; i < 12; i++) {
            Box box = new Box(0.25f, 0.25f, 0.25f);
            Geometry boxGeometry = new Geometry("Box", box);
            boxGeometry.setMaterial(material);
            boxGeometry.setLocalTranslation(i, 5, -3);
            //RigidBodyControl automatically uses box collision shapes when attached to single geometry with box mesh
            boxGeometry.addControl(new RigidBodyControl(2));
            rootNode.attachChild(boxGeometry);
            space.add(boxGeometry);
        }

        //immovable sphere with mesh collision shape
        Sphere sphere = new Sphere(8, 8, 1);
        Geometry sphereGeometry = new Geometry("Sphere", sphere);
        sphereGeometry.setMaterial(material);
        sphereGeometry.setLocalTranslation(4, -4, 2);
        sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
        rootNode.attachChild(sphereGeometry);
        space.add(sphereGeometry);

    }

    public static void createPhysicsWalls(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        Material wallMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        wallMat.setColor("Color", ColorRGBA.LightGray);

        //wall1
        {
            //immovable Box with mesh collision shape
            Box box = new Box(100f, 20f, .1f);
            Geometry boxGeometry = new Geometry("Box", box);
            boxGeometry.setMaterial(wallMat);
            boxGeometry.setQueueBucket(RenderQueue.Bucket.Translucent);
            boxGeometry.setLocalTranslation(-100f, 20, 0);
            boxGeometry.rotate(0, 1.57f, 0);
            boxGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(box), 0));
            rootNode.attachChild(boxGeometry);
            space.add(boxGeometry);
        }
        //wall2
        {
            //immovable Box with mesh collision shape
            Box box = new Box(100f, 20f, .1f);
            Geometry boxGeometry = new Geometry("Box", box);
            boxGeometry.setMaterial(wallMat);
            boxGeometry.setQueueBucket(RenderQueue.Bucket.Translucent);
            boxGeometry.setLocalTranslation(0, 20, -100f);
            boxGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(box), 0));
            rootNode.attachChild(boxGeometry);
            space.add(boxGeometry);
        }
        //wall3
        {
            //immovable Box with mesh collision shape
            Box box = new Box(100f, 20f, .1f);
            Geometry boxGeometry = new Geometry("Box", box);
            boxGeometry.setMaterial(wallMat);
            boxGeometry.setQueueBucket(RenderQueue.Bucket.Translucent);
            boxGeometry.setLocalTranslation(100f, 20, 0);
            boxGeometry.rotate(0, 1.57f, 0);
            boxGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(box), 0));
            rootNode.attachChild(boxGeometry);
            space.add(boxGeometry);
        }
        //wall4
        {
            //immovable Box with mesh collision shape
            Box box = new Box(100f, 20f, .1f);
            Geometry boxGeometry = new Geometry("Box", box);
            boxGeometry.setMaterial(wallMat);
            boxGeometry.setQueueBucket(RenderQueue.Bucket.Translucent);
            boxGeometry.setLocalTranslation(0, 20, 100f);
            boxGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(box), 0));
            rootNode.attachChild(boxGeometry);
            space.add(boxGeometry);
        }
    }

    public static int generateRandomPos() {
        Random positionGenerator = new Random();
        int pos = positionGenerator.nextInt(20);
        return pos;
    }

    public static Vector3f generateRandomXYZ() {
        Random positionGenerator = new Random();
        int xpos = positionGenerator.nextInt(20);
        // ensure the objects are placed in the scene
        int ypos = 24 + positionGenerator.nextInt(20);
        int zpos = positionGenerator.nextInt(20);
        return new Vector3f(xpos, ypos, zpos);
    }

    public static Spatial createTargetOne(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        Material mustachio = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mustachio.setTexture("ColorMap", assetManager.loadTexture("Materials/mustacio.jpg"));
        //immovable sphere with mesh collision shape
        Sphere sphere = new Sphere(30, 30, 1);
        Geometry sphereGeometry = new Geometry("Target One", sphere);
        sphereGeometry.setMaterial(mustachio);
        sphereGeometry.setLocalTranslation(generateRandomXYZ());
        sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
        rootNode.attachChild(sphereGeometry);
        space.add(sphereGeometry);
        return sphereGeometry;
    }

    public static Spatial createTargetTwo(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        Material evil = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        evil.setTexture("ColorMap", assetManager.loadTexture("Materials/evil.jpg"));
        //immovable sphere with mesh collision shape
        Sphere sphere = new Sphere(30, 30, 1);
        Geometry sphereGeometry = new Geometry("Target Two", sphere);
        sphereGeometry.setMaterial(evil);
        sphereGeometry.setLocalTranslation(generateRandomXYZ());
        sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
        rootNode.attachChild(sphereGeometry);
        space.add(sphereGeometry);
        return sphereGeometry;
    }

    public static Spatial createTargetThree(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        Material batman = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        batman.setTexture("ColorMap", assetManager.loadTexture("Materials/batman.jpg"));
        //immovable sphere with mesh collision shape
        Sphere sphere = new Sphere(30, 30, 1);
        Geometry sphereGeometry = new Geometry("Target Three", sphere);
        sphereGeometry.setMaterial(batman);
        sphereGeometry.setLocalTranslation(generateRandomXYZ());
        sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
        rootNode.attachChild(sphereGeometry);
        space.add(sphereGeometry);
        return sphereGeometry;
    }

    public static Spatial createTargetFour(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        Material sunglasses = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunglasses.setTexture("ColorMap", assetManager.loadTexture("Materials/sunglasses.jpg"));
        //immovable sphere with mesh collision shape
        Sphere sphere = new Sphere(30, 30, 1);
        Geometry sphereGeometry = new Geometry("Target Four", sphere);
        sphereGeometry.setMaterial(sunglasses);
        sphereGeometry.setLocalTranslation(generateRandomXYZ());
        sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
        rootNode.attachChild(sphereGeometry);
        space.add(sphereGeometry);
        return sphereGeometry;
    }

    public static Spatial createBall(String name, Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        Material materialSteel = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialSteel.setTexture("ColorMap", assetManager.loadTexture("Materials/steel_material.jpg"));

        Sphere sphere = new Sphere(40, 40, 1);
        Geometry ballGeometry = new Geometry(name, sphere);
        ballGeometry.setMaterial(materialSteel);
        int xpos = 50 + generateRandomPos();
        int ypos = 24 + generateRandomPos();
        int zpos = generateRandomPos();

        ballGeometry.setLocalTranslation(xpos, ypos, zpos);
        //RigidBodyControl automatically uses Sphere collision shapes when attached to single geometry with sphere mesh
        ballGeometry.addControl(new RigidBodyControl(.001f));
        ballGeometry.getControl(RigidBodyControl.class).setRestitution(1);
        rootNode.attachChild(ballGeometry);
        space.add(ballGeometry);
        return ballGeometry;
    }

    public static ArrayList<Spatial> createPhysicsWorld(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        Material materialSteel = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialSteel.setTexture("ColorMap", assetManager.loadTexture("Materials/steel_material.jpg"));

        Material materialCarbonFiber = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialCarbonFiber.setTexture("ColorMap", assetManager.loadTexture("Materials/carbon_fiber_material.jpg"));

        Material materialPatinaCopper = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialPatinaCopper.setTexture("ColorMap", assetManager.loadTexture("Materials/patina_copper_img.jpg"));

        Material materialOnyx = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialOnyx.setTexture("ColorMap", assetManager.loadTexture("Materials/onyx_material.jpg"));

        Material materialTurcoiseScale = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialTurcoiseScale.setTexture("ColorMap", assetManager.loadTexture("Materials/turquoise_scale_material.jpg"));

        ArrayList<Spatial> targets = new ArrayList<Spatial>();


        //movable spheres
        for (int i = 0; i < 5; i++) {
            Sphere sphere = new Sphere(16, 16, .5f);
            Geometry ballGeometry = new Geometry("Soccer ball", sphere);
            ballGeometry.setMaterial(materialOnyx);
            ballGeometry.setLocalTranslation(i, 24, -3);
            //RigidBodyControl automatically uses Sphere collision shapes when attached to single geometry with sphere mesh
            ballGeometry.addControl(new RigidBodyControl(.001f));
            ballGeometry.getControl(RigidBodyControl.class).setRestitution(1);
            rootNode.attachChild(ballGeometry);
            space.add(ballGeometry);
            targets.add(ballGeometry);
        }
        {
            //immovable Box with mesh collision shape
            Box box = new Box(1, 1, 1);
            Geometry boxGeometry = new Geometry("Box", box);
            boxGeometry.setMaterial(materialCarbonFiber);
            boxGeometry.setLocalTranslation(4, 23, 2);
            boxGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(box), 0));
            rootNode.attachChild(boxGeometry);
            space.add(boxGeometry);
            targets.add(boxGeometry);
        }
        {
            //immovable Dome with mesh collision shape
            Dome dome = new Dome(Vector3f.ZERO, 32, 32, 1f, false);
            Geometry domeGeometry = new Geometry("Dome", dome);
            domeGeometry.setMaterial(materialSteel);
            domeGeometry.setLocalTranslation(5, 23, 10);
            domeGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(dome), 0));
            rootNode.attachChild(domeGeometry);
            space.add(domeGeometry);
            targets.add(domeGeometry);
        }
        {
            //immovable Cone with mesh collision shape
            Dome cone = new Dome(Vector3f.ZERO, 2, 32, 1f, false);
            Geometry coneGeometry = new Geometry("Dome", cone);
            coneGeometry.setMaterial(materialPatinaCopper);
            coneGeometry.setLocalTranslation(-15, 23, 5);
            coneGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(cone), 0));
            rootNode.attachChild(coneGeometry);
            space.add(coneGeometry);
            targets.add(coneGeometry);
        }
        {
            //immovable Torus with mesh collision shape
            Torus torus = new Torus(64, 48, 0.5f, 1.0f);
            Geometry ring = new Geometry("Ring", torus);
            ring.setMaterial(materialTurcoiseScale);
            ring.setLocalTranslation(-5, 26, 5);
            ring.addControl(new RigidBodyControl(new MeshCollisionShape(torus), 0));
            rootNode.attachChild(ring);
            space.add(ring);
            targets.add(ring);
        }
        {
            //immovable sphere with mesh collision shape
            Sphere sphere = new Sphere(8, 8, 1);
            Geometry sphereGeometry = new Geometry("Sphere", sphere);
            sphereGeometry.setMaterial(materialOnyx);
            sphereGeometry.setLocalTranslation(4, 28, 2);
            sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
            rootNode.attachChild(sphereGeometry);
            space.add(sphereGeometry);
            targets.add(sphereGeometry);
        }

        return targets;
    }

    /**
     * creates the necessary inputlistener and action to shoot balls from the
     * camera
     *
     * @param app
     * @param rootNode
     * @param space
     */
    public static void createBallShooter(final Application app, final Node rootNode, final PhysicsSpace space) {
        ActionListener actionListener = new ActionListener() {
            public void onAction(String name, boolean keyPressed, float tpf) {
                Sphere bullet = new Sphere(40, 40, 0.6f, true, false);
                bullet.setTextureMode(TextureMode.Projected);
                Material mat2 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                TextureKey key2 = new TextureKey("Materials/turquoise_scale_material.jpg");
                key2.setGenerateMips(true);
                Texture tex2 = app.getAssetManager().loadTexture(key2);
                mat2.setTexture("ColorMap", tex2);
                if (name.equals("shoot") && !keyPressed) {
                    Geometry bulletg = new Geometry("bullet", bullet);
                    bulletg.setMaterial(mat2);
                    bulletg.setShadowMode(ShadowMode.CastAndReceive);
                    bulletg.setLocalTranslation(app.getCamera().getLocation());
                    RigidBodyControl bulletControl = new RigidBodyControl(10);
                    bulletg.addControl(bulletControl);
                    bulletControl.setLinearVelocity(app.getCamera().getDirection().mult(50));
                    bulletg.addControl(bulletControl);
                    rootNode.attachChild(bulletg);
                    space.add(bulletControl);
                }
            }
        };
        app.getInputManager().addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(actionListener, "shoot");
    }
}
