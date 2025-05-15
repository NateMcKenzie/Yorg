package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.Viewport

class Render : EntitySystem() {
    private lateinit var entities : ImmutableArray<Entity>
    private lateinit var viewport: FitViewport

    private val spriteMap = ComponentMapper.getFor(Sprite::class.java)
    private val batch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer() }


    override fun addedToEngine(engine: Engine){
        entities = engine.getEntitiesFor(Family.all(Sprite::class.java).get())
        val camera = engine.getEntitiesFor(Family.all(Viewport::class.java).get()).first()
        viewport = (camera.components.first() as Viewport).viewport
    }

    override fun update(deltaTime : Float){
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        viewport.apply()
        batch.projectionMatrix = viewport.camera.combined
        batch.begin()
        for (entity in entities) {
            val sprite: Sprite = spriteMap.get(entity)
            sprite.sprite.draw(batch)
        }
        batch.end()

        drawGrid()

    }

    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    private fun drawGrid(){
        // TODO: This is maybe not really necessary for grid lines probably, but GPT gave it to
        // me and it may useful for camera stuff later
        val cam = viewport.camera
        val minX = cam.position.x - viewport.worldWidth / 2
        val maxX = cam.position.x + viewport.worldWidth / 2
        val minY = cam.position.y - viewport.worldHeight / 2
        val maxY = cam.position.y + viewport.worldHeight / 2

        shapeRenderer.projectionMatrix = cam.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.DARK_GRAY

        for (x in minX.toInt()..maxX.toInt()){
            shapeRenderer.line(x.toFloat(), minY, x.toFloat(), maxY)
        }
        for (y in minY.toInt()..maxY.toInt()){
            shapeRenderer.line(minX, y.toFloat(), maxX, y.toFloat())
        }

        shapeRenderer.end()
    }
}
