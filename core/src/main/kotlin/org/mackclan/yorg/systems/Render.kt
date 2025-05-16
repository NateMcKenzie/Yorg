package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.GameState

class Render : EntitySystem() {
    private lateinit var entities : ImmutableArray<Entity>
    private lateinit var movables : ImmutableArray<Entity>
    private lateinit var state: GameState

    private val spriteMap = ComponentMapper.getFor(Sprite::class.java)
    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val batch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer() }


    override fun addedToEngine(engine: Engine){
        entities = engine.getEntitiesFor(Family.all(Sprite::class.java).get())
        movables = engine.getEntitiesFor(Family.all(Controlled::class.java).get())
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime : Float){
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        val selectedSprites = Array<Entity>()
        batch.begin()
        for (entity in entities) {
            val sprite: Sprite = spriteMap.get(entity)
            sprite.sprite.draw(batch)
            if (controlledMap.has(entity) && controlledMap.get(entity).selected){
                selectedSprites.add(entity)
            }
        }
        batch.end()

        drawShapes(selectedSprites)

    }

    fun resize(width: Int, height: Int) {
        state.viewport.update(width, height, true)
    }

    private fun drawShapes(selectedSprites: Array<Entity>) {
        val cam = state.viewport.camera
        val minX = cam.position.x - state.viewport.worldWidth / 2
        val maxX = cam.position.x + state.viewport.worldWidth / 2
        val minY = cam.position.y - state.viewport.worldHeight / 2
        val maxY = cam.position.y + state.viewport.worldHeight / 2

        shapeRenderer.projectionMatrix = cam.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.DARK_GRAY

        for (x in minX.toInt()..maxX.toInt()){
            shapeRenderer.line(x.toFloat(), minY, x.toFloat(), maxY)
        }
        for (y in minY.toInt()..maxY.toInt()){
            shapeRenderer.line(minX, y.toFloat(), maxX, y.toFloat())
        }

        for (selected in selectedSprites){
            val sprite = spriteMap.get(selected).sprite
            drawHighlight(sprite)
            val range = controlledMap.get(selected).range
            drawRange(sprite, range)

        }

        shapeRenderer.end()
    }

    private fun drawHighlight(sprite: com.badlogic.gdx.graphics.g2d.Sprite) {
        var rightX = sprite.x + sprite.width
        var topY = sprite.y + sprite.height
        shapeRenderer.color = Color.YELLOW
        shapeRenderer.line(rightX, sprite.y, rightX, topY)       // right
        shapeRenderer.line(sprite.x, sprite.y, sprite.x, topY)   // left
        shapeRenderer.line(sprite.x, topY, rightX, topY)         // top
        shapeRenderer.line(sprite.x, sprite.y, rightX, sprite.y) // bottom

    }

    private fun drawRange(sprite : com.badlogic.gdx.graphics.g2d.Sprite, range : Int){
        shapeRenderer.color = Color.BLUE

        // Lines from up -> right
        var leftX = sprite.x
        var bottomY = sprite.y + range
        var rightX = sprite.x + sprite.width
        var topY = sprite.y + sprite.height + range
        shapeRenderer.line(leftX, bottomY, leftX, topY)
        shapeRenderer.line(leftX, topY, rightX, topY)
        shapeRenderer.line(rightX, bottomY, rightX, topY)
        for (i in 1 until range){
            topY -= sprite.height
            bottomY -= sprite.height
            rightX += sprite.width
            leftX += sprite.width
            shapeRenderer.line(leftX, topY, rightX, topY)
            shapeRenderer.line(rightX, topY, rightX, bottomY)
        }

        // Lines from down -> left
        leftX = sprite.x
        bottomY = sprite.y - range
        rightX = sprite.x + sprite.width
        topY = sprite.y - range + sprite.height
        shapeRenderer.line(leftX, bottomY, leftX, topY)
        shapeRenderer.line(leftX, bottomY, rightX, bottomY)
        shapeRenderer.line(rightX, bottomY, rightX, topY)
        for (i in 1 until range){
            topY += sprite.height
            bottomY += sprite.height
            rightX -= sprite.width
            leftX -= sprite.width
            shapeRenderer.line(leftX, bottomY, rightX, bottomY)
            shapeRenderer.line(leftX, topY, leftX, bottomY)
        }

        // Lines from left -> up
        leftX = sprite.x - range
        bottomY = sprite.y
        rightX = sprite.x - range + sprite.width
        topY = sprite.y + sprite.height
        shapeRenderer.line(leftX, bottomY, leftX, topY)
        shapeRenderer.line(leftX, bottomY, rightX, bottomY)
        shapeRenderer.line(leftX, topY, rightX, topY)
        for (i in 1 until range){
            topY += sprite.height
            bottomY += sprite.height
            rightX += sprite.width
            leftX += sprite.width
            shapeRenderer.line(leftX, topY, rightX, topY)
            shapeRenderer.line(leftX, topY, leftX, bottomY)
        }

        // Lines from right -> down
        leftX = sprite.x + range
        bottomY = sprite.y
        rightX = sprite.x + range + sprite.width
        topY = sprite.y + sprite.height
        shapeRenderer.line(rightX, bottomY, rightX, topY)
        shapeRenderer.line(leftX, bottomY, rightX, bottomY)
        shapeRenderer.line(leftX, topY, rightX, topY)
        for (i in 1 until range){
            topY -= sprite.height
            bottomY -= sprite.height
            rightX -= sprite.width
            leftX -= sprite.width
            shapeRenderer.line(leftX, bottomY, rightX, bottomY)
            shapeRenderer.line(rightX, topY, rightX, bottomY)
        }

    }
}
