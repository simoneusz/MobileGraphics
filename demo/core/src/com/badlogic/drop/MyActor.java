package com.badlogic.drop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MyActor extends Actor {
    private TextureRegion region;
    private ShapeRenderer renderer = new ShapeRenderer();

    public MyActor (TextureRegion region) {
        this.region = region;
        setBounds(region.getRegionX(), region.getRegionY(),
                region.getRegionWidth(), region.getRegionHeight());
    }
    public void draw (Batch batch, float parentAlpha) {
        batch.end();

        renderer.setProjectionMatrix(batch.getProjectionMatrix());
        renderer.setTransformMatrix(batch.getTransformMatrix());
        renderer.translate(getX(), getY(), 0);

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.BLUE);
        renderer.rect(0, 0, getWidth(), getHeight());
        renderer.end();

        batch.begin();
    }
}
