package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.SpriteComponent
import org.mackclan.yorg.components.UnitInfo

class Render : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>
    private lateinit var movables: ImmutableArray<Entity>
    private lateinit var state: GameState

    private val spriteComponentMap = ComponentMapper.getFor(SpriteComponent::class.java)
    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val unitInfoMap = ComponentMapper.getFor(UnitInfo::class.java)
    private val batch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val screenViewport by lazy { ScreenViewport() }

    private val font by lazy { BitmapFont() }

    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(Family.all(SpriteComponent::class.java).get())
        movables = engine.getEntitiesFor(Family.all(Controlled::class.java, SpriteComponent::class.java).get())
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        batch.begin()
        val selectedSprites = Array<Entity>()
        for (entity in entities) {
            val sprite = spriteComponentMap.get(entity).sprite
            sprite.draw(batch)
            if (controlledMap.has(entity) && controlledMap.get(entity).selected) {
                selectedSprites.add(entity)
            }
        }
        batch.end()

        drawShapes(selectedSprites)

        screenViewport.apply()
        batch.projectionMatrix = screenViewport.camera.combined
        batch.begin()
        for (entity in movables) {
            val info = unitInfoMap.get(entity)
            val sprite = spriteComponentMap.get(entity).sprite
            val health = info.health.toString()

            // Need to convert between coordinate systems
            val gameWorldPos =
                Vector3(
                    sprite.x + 0.5f,
                    sprite.y + 1,
                    0f
                ) // Put text on top and "centered" to be modified in screen coords later
            val screenPos = Vector3()
            state.viewport.project(screenPos.set(gameWorldPos))

            // Bump up and center
            val bounds = GlyphLayout()
            bounds.setText(font, health)
            screenPos.y += bounds.height * 1.15f // Need some fudge not sure why
            screenPos.x -= bounds.width / 2

            font.draw(batch, health, screenPos.x, screenPos.y)
        }
        batch.end()
    }

    fun resize(width: Int, height: Int) {
        state.viewport.update(width, height, true)
        screenViewport.update(width, height, true)
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

        for (x in minX.toInt()..maxX.toInt()) {
            shapeRenderer.line(x.toFloat(), minY, x.toFloat(), maxY)
        }
        for (y in minY.toInt()..maxY.toInt()) {
            shapeRenderer.line(minX, y.toFloat(), maxX, y.toFloat())
        }

        // TODO: There's only one though right? Might be better off using gamestate selected for
        // this?
        for (selected in selectedSprites) {
            val sprite = spriteComponentMap.get(selected).sprite
            drawHighlight(sprite)
        }
        shapeRenderer.end()
    }

    private fun drawHighlight(sprite: com.badlogic.gdx.graphics.g2d.Sprite) {
        var rightX = sprite.x + sprite.width
        var topY = sprite.y + sprite.height
        shapeRenderer.color = Color.YELLOW
        shapeRenderer.line(rightX, sprite.y, rightX, topY) // right
        shapeRenderer.line(sprite.x, sprite.y, sprite.x, topY) // left
        shapeRenderer.line(sprite.x, topY, rightX, topY) // top
        shapeRenderer.line(sprite.x, sprite.y, rightX, sprite.y) // bottom
    }
}
