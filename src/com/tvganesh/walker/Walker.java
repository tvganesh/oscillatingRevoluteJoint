package com.tvganesh.walker;

/*
 * Designed and developed by Tinniam V Ganesh 1 Aug 2013
 * Uses Box2D physics & AndEngine
 * 
 */


import java.util.Timer;
import java.util.TimerTask;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import android.hardware.SensorManager;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;



public class Walker extends SimpleBaseGameActivity implements  IAccelerationListener, IOnSceneTouchListener {
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	public static final float PIXEL_TO_METER_RATIO_DEFAULT = 32.0f;
	
    
    private Scene mScene;
    
    private PhysicsWorld mPhysicsWorld;
    
	private BitmapTextureAtlas mBitmapTextureAtlas;   
	private TextureRegion mWallTextureRegion;
	private TextureRegion mLegTextureRegion;
	private TextureRegion mBallTextureRegion;
	
	Rectangle r;
	Rectangle ground,roof,left,right;
	static Sprite lWall,rWall;
	Body lWallBody,rWallBody;
	Sprite wheel1,wheel2;
	Body wheelBody1,wheelBody2;
	Sprite car;
	Body  carBody;
	Sprite leg;
	Body legBody;
	Sprite circle;
	Body circleBody;
	
	final FixtureDef gameFixtureDef = PhysicsFactory.createFixtureDef(10f, 0.0f, 0.0f);

    private static FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(50f, 0.0f, 0.5f);
	
	static RevoluteJoint rJoint;
	public EngineOptions onCreateEngineOptions() {
		
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}
	
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 205, 615, TextureOptions.BILINEAR);		
		
		BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "wheel.png", 0, 0);		
		this.mWallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "wall.png", 60, 60);		
		BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "car.png", 65, 545);
		this.mLegTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "leg.png", 185, 555);
		this.mBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ball.png", 195, 605);
		this.mBitmapTextureAtlas.load();
		
	
	}
	
	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		this.mScene.setOnSceneTouchListener(this);
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_MOON), false);
			
		this.initWheels(mScene);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
	
		return mScene;		
		
	}
	

	public void initWheels(Scene mScene){
		//Create the floor		
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		// Set a small friction for the wheels to roll
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.0f, 0.2f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
       
        
		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		// Create the left wall - Collisions happen between bodies
		lWall = new Sprite(0, 0, this.mWallTextureRegion, this.getVertexBufferObjectManager());
		lWallBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, lWall, BodyType.StaticBody, wallFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(lWall, lWallBody, true, true));
		this.mScene.attachChild(lWall);
		
		// Create right wall - Collisions happen between bodies
		rWall = new Sprite(715, 0, this.mWallTextureRegion, this.getVertexBufferObjectManager());
		rWallBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, rWall, BodyType.StaticBody, wallFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(rWall, rWallBody, true, true));
		this.mScene.attachChild(rWall);
							
		circle = new Sprite(100, 320, this.mBallTextureRegion, this.getVertexBufferObjectManager());
		circleBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, circle, BodyType.StaticBody, FIXTURE_DEF);
		this.mScene.attachChild(circle);
				
		// Create leg
		leg = new Sprite(110, 325, this.mLegTextureRegion, this.getVertexBufferObjectManager());
		legBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, leg, BodyType.DynamicBody, gameFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(leg, legBody, true, true));
		this.mScene.attachChild(leg);
		
		   
        final RevoluteJointDef rJointDef = new RevoluteJointDef();	    
        rJointDef.initialize(legBody, circleBody, circleBody.getWorldCenter());
        rJointDef.enableMotor = true;
        rJointDef.enableLimit = true;
		rJoint = (RevoluteJoint) this.mPhysicsWorld.createJoint(rJointDef);		
		rJoint.setMotorSpeed(2);
		rJoint.setMaxMotorTorque(100);
		rJoint.setLimits((float)(30 * (Math.PI)/180), (float)(270 * (Math.PI)/180));
		Log.d("x","x1-----"+rJoint.getUpperLimit() );
		new IntervalTimer(5,rJoint);		
		Log.d("x","x1-----"+rJoint.getUpperLimit() );
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
	}
	

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}	

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
		
	}


	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);

	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}
	
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {
				//this.addBall(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				return true;
			}
		}
		return false;
	}

}	

class IntervalTimer {
    Timer timer;
   

    public IntervalTimer(int seconds, RevoluteJoint rj) {
    	Log.d("Inside","in");
        timer = new Timer();  //At this line a new Thread will be created      
        timer.scheduleAtFixedRate(new RemindTask(rj), seconds*1000, 1000);
        
    }

    class RemindTask extends TimerTask {
    	RevoluteJoint rj1;;
    	RemindTask(RevoluteJoint rj){
    		rj1 = rj;
    	}
        @Override
        public void run() {
        	Log.d("x","x" +"Reversing motor");
        	reverseMotor();
            
        }
        
        public void reverseMotor(){
        	rj1.setMotorSpeed(-(rj1.getMotorSpeed()));
        	rj1.setMaxMotorTorque(100);
        	Log.d("aa","speed:"+rj1.getMotorSpeed() + "torque:" + rj1.getMotorTorque());
        	
        }
    }

   
}