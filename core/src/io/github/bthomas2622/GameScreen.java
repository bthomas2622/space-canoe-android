package io.github.bthomas2622;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by bthom on 12/7/2016.
 */

public class GameScreen implements Screen {
    final SpaceCanoe game;
    Texture canoeImage;
    Texture spaceDebrisImage;
    Texture spaceDebrisImageLarge;
    Texture spaceDebrisImageLargest;
    Texture spaceDebrisImageLargerThanLargest;
    Texture backgroundSpaceImage;
    Texture purplePlanetImage;
    Texture orangePlanetImage;
    Texture paddleImage;
    Sound paddleSound;
    Box2DDebugRenderer debugRenderer;
    Matrix4 debugMatrix;
    Music backgroundMusic;
    OrthographicCamera camera;
    Sprite canoe;
    Sprite paddle;
    Sprite purplePlanet;
    Sprite orangePlanet;
    Array<Sprite> spaceDebris;
    long lastDebrisTime;
    long nextDebrisTime = 500000000;
    int debrisDodged = 0;
    World world;
    Body canoeBody;
    Body debrisBody;
    Array<Body> bodies;
    Array<Fixture> debrisBodyFixture;
    float torque = 0.0f;
    float currentDegrees;
    Boolean gameOver = false;
    double getCanoeAngleDouble;
    int i;
    //font variables
    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    BitmapFont gameFont;
    GlyphLayout glyphLayout;
    float countWidth;
    String debrisDodgedString;
    float[] canoePolygon;
    float debrisDiceRoller;
    Boolean drawPaddle = false;
    double doubleCanoeAngleInRadians;
    int rows = 0;
    double degreesDouble;
    float debrisVelocity = 1f;
    float impulseForce = .6f;
    final float PIXELS_TO_METERS = 100f;
    Vector3 touchPos = new Vector3();
    AssetManager assetManager;
    boolean loaded = false;

    /**
     * contstructor that takes in game object and creates game instance, loads in assets, creates debug renderer, world contact listener, etc.
     * @param gam SpaceCanoe game object that is rendered, batched, etc.
     */
    public GameScreen(final SpaceCanoe gam) {
        game = gam;
        // load the images for the canoe and the space debris
        canoeImage = new Texture(Gdx.files.internal("canoeSprite.png"));
        spaceDebrisImage = new Texture(Gdx.files.internal("spaceDebris.png"));
        spaceDebrisImageLarge = new Texture(Gdx.files.internal("spaceDebris100.png"));
        spaceDebrisImageLargest = new Texture(Gdx.files.internal("spaceDebris150.png"));
        spaceDebrisImageLargerThanLargest = new Texture(Gdx.files.internal("spaceDebris250.png"));
        backgroundSpaceImage = new Texture(Gdx.files.internal("spaceBackground1920.png"));
        purplePlanetImage = new Texture(Gdx.files.internal("purplePlanet.png"));
        orangePlanetImage = new Texture(Gdx.files.internal("orangePlanet.png"));
        paddleImage = new Texture(Gdx.files.internal("paddle25.png"));

        assetManager = new AssetManager();
        assetManager.load("paddle.mp3", Sound.class);
        assetManager.load("gameBackground.mp3", Music.class);
        assetManager.finishLoading();
//        paddleSound = Gdx.audio.newSound(Gdx.files.internal("paddle.mp3"));
//        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("gameBackground.mp3"));
//        backgroundMusic.setLooping(true);
//        backgroundMusic.setVolume(0.75f);
//        backgroundMusic.play();

        //debug renderer allows us to see physics simulation controlling the scen
        //debugRenderer = new Box2DDebugRenderer();
        // create the camera
        camera = new OrthographicCamera();
        //camera.setToOrtho(false, 1280, 720);
        camera.setToOrtho(false, 1920, 1080);


        // creating canoe sprite
        canoe = new Sprite(canoeImage);
        canoe.setPosition(Gdx.graphics.getWidth()/2 - canoe.getWidth() / 2, Gdx.graphics.getHeight() / 2 - canoe.getHeight() / 2);
        //need to set origin center of sprite so that future rotations around center of sprite
        canoe.setOriginCenter();
        canoe.setRotation(0f);

        // creating planet sprites
        purplePlanet = new Sprite(purplePlanetImage);
        purplePlanet.setPosition(Gdx.graphics.getWidth()* (float) Math.random(), Gdx.graphics.getHeight()* (float) Math.random());
        purplePlanet.setOriginCenter();
        orangePlanet = new Sprite(orangePlanetImage);
        orangePlanet.setPosition(Gdx.graphics.getWidth()* (float) Math.random(), Gdx.graphics.getHeight()* (float) Math.random());
        orangePlanet.setOriginCenter();

        paddle = new Sprite(paddleImage);
        //placeholder position for paddle
        paddle.setPosition(Gdx.graphics.getWidth()/2 - paddle.getWidth() / 2, Gdx.graphics.getHeight() / 2 - paddle.getHeight() / 2);

        //phyiscs world and bodytypes
        world = new World(new Vector2(0f, 0f), true);
        BodyDef canoeBodyDef = new BodyDef();
        BodyDef debrisBodyDef = new BodyDef();
        canoeBodyDef.type = BodyDef.BodyType.DynamicBody;
        debrisBodyDef.type = BodyDef.BodyType.DynamicBody;
        canoeBodyDef.position.set((canoe.getX() + canoe.getWidth() / 2)/PIXELS_TO_METERS, (canoe.getY() + canoe.getHeight() / 2)/PIXELS_TO_METERS);
        //create body in world using our definition
        canoeBody = world.createBody(canoeBodyDef);
        //define dimensions of the canoe physics shape
        PolygonShape canoeShape = new PolygonShape();
        //canoeShape.setAsBox(canoe.getWidth()/2, canoe.getHeight()/2);
        //float array of indices that make up shape of canoe for hit detection, it is a diamond, coordinates or with origin at center of canoe
        canoePolygon = new float[8];
        canoePolygon[0] = -112f / PIXELS_TO_METERS;
        canoePolygon[1] = 0f;
        canoePolygon[2] = 0f;
        canoePolygon[3] = 26f / PIXELS_TO_METERS;
        canoePolygon[4] = 112f / PIXELS_TO_METERS;
        canoePolygon[5] = 0f / PIXELS_TO_METERS;
        canoePolygon[6] = 0f / PIXELS_TO_METERS;
        canoePolygon[7] = -26f / PIXELS_TO_METERS;
        canoeShape.set(canoePolygon);
        //FixtureDef defines shape of body and properties like density
        FixtureDef canoeFixtureDef = new FixtureDef();
        canoeFixtureDef.shape = canoeShape;
        canoeFixtureDef.density = 1f;
        canoeFixtureDef.restitution = 1f;
        //Fixture canoeFixture = canoeBody.createFixture(canoeFixtureDef);
        canoeBody.setUserData("canoe");
        canoeBody.createFixture(canoeFixtureDef);

        //create contact listener for when debris collides with canoe
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                if (contact.isTouching()) {
                    if ((contact.getFixtureA().getBody().getUserData() == "debris" && contact.getFixtureB().getBody().getUserData() == "canoe") || (contact.getFixtureA().getBody().getUserData() == "canoe" && contact.getFixtureB().getBody().getUserData() == "debris")) {
                        System.out.println("COLLISION");
                        gameOver = true;
                    }
                }
            }
            @Override
            public void endContact(Contact contact) {
            }
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });

        //create font for dodge counter
        generator = new FreeTypeFontGenerator(Gdx.files.internal("SpaceMono-Bold.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 30;
        parameter.color = Color.RED;
        gameFont = generator.generateFont(parameter);
        //generating a glyph layout to get the length of the string so i can center it
        glyphLayout = new GlyphLayout();
        debrisDodgedString = String.valueOf(debrisDodged);
        glyphLayout.setText(gameFont,debrisDodgedString);
        countWidth = glyphLayout.width;

        // create the space debris array and spawn the first piece of debris
        spaceDebris = new Array<Sprite>();
        bodies = new Array<Body>();
        spawnDebris();

        canoeShape.dispose();
    }

    /**
     * start the music once the assets have been loaded
     * @return true or false has the music started
     */
    public boolean startMusic() {
        if(assetManager.isLoaded("gameBackground.mp3") & assetManager.isLoaded("paddle.mp3")) {
            backgroundMusic = assetManager.get("gameBackground.mp3", Music.class);
            paddleSound = assetManager.get("paddle.mp3", Sound.class);
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(1f);
            backgroundMusic.play();
            return true;
        }else {
            //System.out.println("not loaded yet");
            return false;
        }
    }

    /**
     * method to spawn new space debris into game world, places based on canoe angle and gives initial velocity
     */
    private void spawnDebris() {
        debrisDiceRoller = MathUtils.random(10f);
        //System.out.println(String.valueOf(debrisDiceRoller));
        Sprite debris;
        if (debrisDodged > 50){
            if (debrisDiceRoller <= 7f){
                debris = new Sprite(spaceDebrisImageLarge);
            }
            else if (debrisDiceRoller <= 9.5f){
                debris = new Sprite(spaceDebrisImageLargest);
            }
            else {
                debris = new Sprite(spaceDebrisImageLargerThanLargest);
            }
        }
        else {
            if (debrisDiceRoller <= 8f){
                debris = new Sprite(spaceDebrisImageLarge);
            }
            else {
                debris = new Sprite(spaceDebrisImageLargest);
            }
        }

        //place the canoe just outside the screen wherever the canoe is facing
        if (getCanoeAngle() <= 45f || getCanoeAngle() >= 315f){
            debris.setPosition(Gdx.graphics.getWidth() + debris.getWidth() / 2, MathUtils.random()*Gdx.graphics.getHeight());
        } else if (getCanoeAngle() > 45f && getCanoeAngle() <= 135f){
            debris.setPosition(Gdx.graphics.getWidth()*MathUtils.random(), Gdx.graphics.getHeight() + debris.getHeight()/2);
        } else if (getCanoeAngle() > 135f && getCanoeAngle() <= 225f){
            debris.setPosition(0 - debris.getWidth()/2,Gdx.graphics.getHeight()*MathUtils.random());
        } else {
            debris.setPosition(Gdx.graphics.getWidth()*MathUtils.random(), 0 - debris.getHeight()/2);
        }

        //System.out.println(debris.getX());
        //System.out.println(debris.getY());

        //origin center allows debris to be rotated based off its origin
        debris.setOriginCenter();
        BodyDef debrisBodyDef = new BodyDef();
        debrisBodyDef.type = BodyDef.BodyType.DynamicBody;
        debrisBodyDef.position.set((debris.getX() + debris.getWidth()/2)/PIXELS_TO_METERS,(debris.getY()+debris.getHeight()/2)/PIXELS_TO_METERS);
        //create body in world using our definition
        debrisBody = world.createBody(debrisBodyDef);
        //define dimensions of the canoe physics shape
        //PolygonShape debrisShape = new PolygonShape();
        CircleShape debrisShape = new CircleShape();
        debrisShape.setRadius(debris.getWidth()/2 / PIXELS_TO_METERS);
        //debrisShape.setAsBox(debris.getWidth()/2, debris.getHeight()/2);
        //FixtureDef defines shape of body and properties like density
        FixtureDef debrisFixtureDef = new FixtureDef();
        debrisFixtureDef.shape = debrisShape;
        debrisFixtureDef.density = 1.0f;
        debrisFixtureDef.restitution = 1.0f;
        debrisFixtureDef.friction = 0.0f;
        debrisBody.createFixture(debrisFixtureDef);
        //Fixture debrisFixture = canoeBody.createFixture(debrisFixtureDef);

        if (getCanoeAngle() <= 45f || getCanoeAngle() >= 315f){
            debrisBody.applyLinearImpulse(-debrisVelocity, 0, debris.getX(), debris.getY(), true);
        } else if (getCanoeAngle() > 45f && getCanoeAngle() <= 135f){
            debrisBody.applyLinearImpulse(0, -debrisVelocity, debris.getX(), debris.getY(), true);
        } else if (getCanoeAngle() > 135f && getCanoeAngle() <= 225f){
            debrisBody.applyLinearImpulse(debrisVelocity, 0, debris.getX(), debris.getY(), true);
        } else {
            debrisBody.applyLinearImpulse(0, debrisVelocity, debris.getX(), debris.getY(), true);
        }

        debrisBody.setUserData("debris");
        spaceDebris.add(debris);
        bodies.add(debrisBody);

        debrisShape.dispose();

        lastDebrisTime = TimeUtils.nanoTime();
    }

    /**
     * getter method to obtain the current orientation of the canoe
     * @return float of canoe angle in degrees
     */
    public float getCanoeAngle(){
        getCanoeAngleDouble = (double) canoeBody.getAngle();
        currentDegrees = (float) Math.toDegrees(getCanoeAngleDouble);
        return currentDegrees;
    };

    /**
     * setter method to rotate the game sprite and physics body towards new canoe angle
     * @param degrees
     */
    public void setCanoeAngle(float degrees){
        //canoe.setRotation((float)Math.toRadians(degrees));
        canoe.setOriginCenter();
        canoe.setRotation(degrees);
        degreesDouble = (double) degrees;
        //System.out.println(degreesDouble);
        //System.out.println((float) Math.toRadians(degreesDouble));
        canoeBody.setTransform(canoeBody.getPosition(), (float) Math.toRadians(degreesDouble));
        //System.out.println(canoeBody.getAngle());
    };

    @Override
    public void render(float delta) {
        if (loaded == false){
            loaded = startMusic();
        }
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(1, 1, 1, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();
        //step physics forward at refresh rate of 60hz
        world.step(1f/60f, 6, 2);
        //canoe.setPosition(canoeBody.getPosition().x, canoeBody.getPosition().y);
        drawPaddle = false;
        //the impulseForce represents the instant force on the space debris objects as a result of a canoe paddle
        if (Gdx.input.justTouched()){
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            if (touchPos.x > Gdx.graphics.getWidth()/2f) {
                paddleSound.play(0.02f);
                drawPaddle = true;
                paddle.setPosition(Gdx.graphics.getWidth()/2 - paddle.getWidth()/2 + (canoe.getWidth()/3)*(float)Math.sin(doubleCanoeAngleInRadians), Gdx.graphics.getHeight()/2 - paddle.getHeight()/2 - (canoe.getWidth()/3)*(float)Math.cos(doubleCanoeAngleInRadians));
                //canoe.setRotation((float)Math.toDegrees(30));
                if (getCanoeAngle() >= 330f)
                    setCanoeAngle(0f);
                else
                    setCanoeAngle(getCanoeAngle() + 30f);
                i = 0;
                doubleCanoeAngleInRadians = Math.toRadians((double) getCanoeAngle());
                for (Sprite debris : spaceDebris) {
                    bodies.get(i).applyLinearImpulse(-impulseForce*(float)Math.cos(doubleCanoeAngleInRadians), -impulseForce*(float)Math.sin(doubleCanoeAngleInRadians), canoe.getOriginX(), canoe.getOriginY(), true);
                    //debris.setPosition(bodies.get(i).getPosition().x + debris.getWidth(), bodies.get(i).getPosition().y);
                    debris.setPosition((bodies.get(i).getPosition().x*PIXELS_TO_METERS - debris.getWidth()/2),  (bodies.get(i).getPosition().y*PIXELS_TO_METERS - debris.getHeight()/2));
                    debris.setRotation((float) Math.toDegrees((double) bodies.get(i).getAngle()));
                    //System.out.println((float) Math.toDegrees((double) bodies.get(i).getAngle()));
                    i++;
                }
                rows++;
            } else if (touchPos.x <= Gdx.graphics.getWidth()/2f) {
                paddleSound.play(0.02f);
                drawPaddle = true;
                paddle.setPosition(Gdx.graphics.getWidth()/2 - paddle.getWidth()/2 - (canoe.getWidth()/3)*(float)Math.sin(doubleCanoeAngleInRadians), Gdx.graphics.getHeight()/2 - paddle.getHeight()/2 + (canoe.getWidth()/3)*(float)Math.cos(doubleCanoeAngleInRadians));
                if (getCanoeAngle() <= 0f)
                    setCanoeAngle(330f);
                else
                    setCanoeAngle((getCanoeAngle() - 30f));
                i = 0;
                doubleCanoeAngleInRadians = Math.toRadians((double) getCanoeAngle());
                for (Sprite debris : spaceDebris) {
                    bodies.get(i).applyLinearImpulse(-impulseForce*(float)Math.cos(doubleCanoeAngleInRadians), -impulseForce*(float)Math.sin(doubleCanoeAngleInRadians), canoe.getOriginX(), canoe.getOriginY(), true);
                    //debris.setPosition(bodies.get(i).getPosition().x + debris.getWidth(), bodies.get(i).getPosition().y);
                    debris.setPosition((bodies.get(i).getPosition().x*PIXELS_TO_METERS - debris.getWidth()/2),  (bodies.get(i).getPosition().y*PIXELS_TO_METERS - debris.getHeight()/2));
                    debris.setRotation((float) Math.toDegrees(bodies.get(i).getAngle()));
                    i++;
                }
                rows++;
            }
        } else {
            i = 0;
            for (Sprite debris : spaceDebris) {
                //bodies.get(i).applyForceToCenter((bodies.get(i).getLinearVelocity()).x * 25f, bodies.get(i).getLinearVelocity().y * 25f, true);
                //debris.setPosition(bodies.get(i).getPosition().x + debris.getWidth(), bodies.get(i).getPosition().y);
                debris.setPosition((bodies.get(i).getPosition().x*PIXELS_TO_METERS - debris.getWidth()/2),  (bodies.get(i).getPosition().y*PIXELS_TO_METERS - debris.getHeight()/2));
                debris.setRotation((float) Math.toDegrees(bodies.get(i).getAngle()));
                //remove avoided space debris
                //System.out.println(bodies.get(i).getPosition().x);
                if (bodies.get(i).getPosition().x*PIXELS_TO_METERS < - debris.getWidth() || bodies.get(i).getPosition().x*PIXELS_TO_METERS > Gdx.graphics.getWidth() + debris.getWidth() || bodies.get(i).getPosition().y*PIXELS_TO_METERS < -debris.getHeight() || bodies.get(i).getPosition().y*PIXELS_TO_METERS > Gdx.graphics.getHeight() + debris.getHeight()){
                    spaceDebris.removeIndex(i); //destroys sprite associated with dodged debris
                    debrisBodyFixture = bodies.get(i).getFixtureList();
                    bodies.get(i).destroyFixture(debrisBodyFixture.first()); //destroys body fixture associated with dodged debris
                    bodies.removeIndex(i); //destroys body associated with dodged debris
                    debrisDodged++;
                    debrisDodgedString = String.valueOf(debrisDodged);
                }
                i++;
            }
        }
        doubleCanoeAngleInRadians = Math.toRadians((double) getCanoeAngle());
//        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//            drawPaddle = true;
//            paddle.setPosition(Gdx.graphics.getWidth()/2 - paddle.getWidth()/2 + (canoe.getWidth()/3)*(float)Math.sin(doubleCanoeAngleInRadians), Gdx.graphics.getHeight()/2 - paddle.getHeight()/2 - (canoe.getWidth()/3)*(float)Math.cos(doubleCanoeAngleInRadians));
//        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
//            drawPaddle = true;
//            paddle.setPosition(Gdx.graphics.getWidth()/2 - paddle.getWidth()/2 - (canoe.getWidth()/3)*(float)Math.sin(doubleCanoeAngleInRadians), Gdx.graphics.getHeight()/2 - paddle.getHeight()/2 + (canoe.getWidth()/3)*(float)Math.cos(doubleCanoeAngleInRadians));
//        }
        //System.out.println(getCanoeAngle());
        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);
        //debugMatrix = game.batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS,PIXELS_TO_METERS, 0);

        // begin a new batch and draw the canoe and all debris
        game.batch.begin();
        game.batch.draw(backgroundSpaceImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //game.font.draw(game.batch, "Debris Dodged: " + debrisDodged, 0, 480);
        game.batch.draw(purplePlanet, purplePlanet.getX(), purplePlanet.getY(), purplePlanet.getOriginX(), purplePlanet.getOriginY(), purplePlanet.getWidth(), purplePlanet.getHeight(), purplePlanet.getScaleX(), purplePlanet.getScaleY(), purplePlanet.getRotation());
        game.batch.draw(orangePlanet, orangePlanet.getX(), orangePlanet.getY(), orangePlanet.getOriginX(), orangePlanet.getOriginY(), orangePlanet.getWidth(), orangePlanet.getHeight(), orangePlanet.getScaleX(), orangePlanet.getScaleY(), orangePlanet.getRotation());
        gameFont.draw(game.batch, debrisDodgedString, Gdx.graphics.getWidth()/2 - countWidth/2, Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/6);
        game.batch.draw(canoe, canoe.getX(), canoe.getY(), canoe.getOriginX(), canoe.getOriginY(), canoe.getWidth(), canoe.getHeight(), canoe.getScaleX(), canoe.getScaleY(), canoe.getRotation());
        if (drawPaddle){
            game.batch.draw(paddle, paddle.getX(), paddle.getY(), paddle.getOriginX(), paddle.getOriginY(), paddle.getWidth(), paddle.getHeight(), paddle.getScaleX(), paddle.getScaleY(), paddle.getRotation());
        }
        for (Sprite debris : spaceDebris) {
            game.batch.draw(debris, debris.getX(), debris.getY(), debris.getOriginX(), debris.getOriginY(), debris.getWidth(), debris.getHeight(), debris.getScaleX(), debris.getScaleY(), debris.getRotation());
        }
        game.batch.end();

        //render the debug matrix
        //debugRenderer.render(world, debugMatrix);

        //increase difficulty
        if (nextDebrisTime > 100000000)
            nextDebrisTime -= 10000;

        //check if we need to create a new space debris object based on time in nanoseconds
        if (TimeUtils.nanoTime() - lastDebrisTime > nextDebrisTime){
            spawnDebris();
        }

        //check to see if a collision with the canoe has been detected to generate the game over screen
        if (gameOver){
            game.setScreen(new GameOverScreen(game, debrisDodged, rows));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        //re-eastablish canoe parameters with new screen size
        canoe.setPosition(Gdx.graphics.getWidth()/2 - canoe.getWidth() / 2, Gdx.graphics.getHeight() / 2 - canoe.getHeight() / 2);
        canoeBody.setTransform((canoe.getX() + canoe.getWidth() / 2) / PIXELS_TO_METERS, (canoe.getY() + canoe.getHeight() / 2) / PIXELS_TO_METERS, (float) Math.toRadians(degreesDouble));
        camera.update();
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        //backgroundMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        canoeImage.dispose();
        spaceDebrisImage.dispose();
        spaceDebrisImageLarge.dispose();
        spaceDebrisImageLargest.dispose();
        backgroundSpaceImage.dispose();
        purplePlanetImage.dispose();
        orangePlanetImage.dispose();
        paddleImage.dispose();
        world.dispose();
        paddleSound.dispose();
        backgroundMusic.dispose();
        //debugRenderer.dispose();
        generator.dispose();
    }
}
