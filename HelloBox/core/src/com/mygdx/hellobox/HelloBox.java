package com.mygdx.hellobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import static com.mygdx.hellobox.utils.Constants.PPM;
import static com.mygdx.hellobox.utils.Constants.GRAVITY;

import java.util.Iterator;

import jdk.internal.org.jline.utils.Log;

public class HelloBox extends ApplicationAdapter {
	private boolean DEBUG = true;

	private final float SCALE = 2.0f;
	private OrthographicCamera camera;
	private Box2DDebugRenderer box2DDebugRenderer;
	private World world;
	private Body player, platform;
	SpriteBatch batch;
	BitmapFont font;
	Array<Rectangle> raindrops;
	long lastDropTime;
	int dropsGathered;

	private Texture bucketImage;
	private Texture dropImage;


	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		font = new BitmapFont();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, w/SCALE, h/SCALE);

		world = new World(new Vector2(0, GRAVITY), false);
		box2DDebugRenderer = new Box2DDebugRenderer();

		player = createBox(0,10,32,32,false);
		platform = createBox(-10,0, 1024,32,true);//btm
		platform = createBox(-10,400, 1024,32,true);//top
		platform = createBox(-500,0, 32,1024,true);//left
		platform = createBox(500,0, 32,1024,true);//right
		//platform = createBox(0,-10, 32,1024,true);//top

		batch = new SpriteBatch();

		dropImage = new Texture(Gdx.files.internal("drop.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	@Override
	public void render () {
		float deltaTime = Gdx.graphics.getDeltaTime();
		update(deltaTime);
		//render
		Gdx.gl.glClearColor(0f,0f,0f,1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		font.draw(batch, "Drops Collected: " + dropsGathered, player.getPosition().x, player.getPosition().y);
		batch.draw(bucketImage,
				player.getPosition().x * PPM - bucketImage.getWidth()/2,
				player.getPosition().y * PPM - bucketImage.getHeight()/2);
		if(DEBUG){
			font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			font.draw(batch,
					"Player Position: " + player.getPosition().x*PPM + ", " + player.getPosition().y*PPM,
					player.getPosition().x * PPM,
					player.getPosition().y * PPM-20);
		}
		for (Rectangle raindrop : raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();


		box2DDebugRenderer.render(world,camera.combined.scl(PPM));

		if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) Gdx.app.exit();

		if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
			spawnRaindrop();


	}
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(-13*PPM, 13*PPM);
		raindrop.y = 10*PPM;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}
	@Override
	public void resize(int width, int height){
		camera.setToOrtho(false, width/SCALE, height/SCALE);
	}
	

	public void update(float delta){
		world.step(1/60f, 6,2);
		inputUpdate(delta);
		cameraUpdate(delta);
		batch.setProjectionMatrix(camera.combined);
		Rectangle playerRectangle = new Rectangle(
				player.getPosition().x * PPM - bucketImage.getWidth() / 2,
				player.getPosition().y * PPM - bucketImage.getHeight() / 2,
				bucketImage.getWidth(),
				bucketImage.getHeight());

		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0)
				iter.remove();
			if (raindrop.overlaps(playerRectangle)) {
				dropsGathered++;
				iter.remove();
			}
		}

	}

	public void inputUpdate(float delta) {
		int horizontalForce = 0;

		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			player.getPosition().x = touchPos.x - 64 / 2;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			horizontalForce -= 1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			horizontalForce += 1;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
			player.applyForceToCenter(0,300,false);
		}
		//force*5newtons
		player.setLinearVelocity(horizontalForce * 10, player.getLinearVelocity().y);
	}

	public  void cameraUpdate(float delta){
		Vector3 position = camera.position;
		position.x = player.getPosition().x * PPM;
		position.y = player.getPosition().y * PPM;
		camera.position.set(position);

		camera.update();
	}
	public Body createBox(int x, int y, int width, int height, boolean isStatic){
		Body pBody;
		BodyDef def = new BodyDef();
		if(isStatic) def.type = BodyDef.BodyType.StaticBody;
		else def.type = BodyDef.BodyType.DynamicBody;
		def.position.set(x / PPM,y / PPM);
		def.fixedRotation = true;
		pBody = world.createBody(def);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2 / PPM,height / 2 / PPM);
		pBody.createFixture(shape, 1.0f);
		shape.dispose();
		return pBody;
	}
	@Override
	public void dispose () {
		world.dispose();
		box2DDebugRenderer.dispose();
		batch.dispose();
		font.dispose();
	}
}
