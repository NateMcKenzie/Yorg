package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.*

class Render : EntitySystem() {
    private lateinit var sprites: ImmutableArray<Entity>
    private lateinit var movables: ImmutableArray<Entity>
    private lateinit var state: GameState

    private val spriteComponentMap = ComponentMapper.getFor(SpriteComponent::class.java)
    private val animatablePositionMap = ComponentMapper.getFor(AnimatablePosition::class.java)
    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val unitInfoMap = ComponentMapper.getFor(UnitInfo::class.java)
    private val batch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val screenViewport by lazy { ScreenViewport() }

    private val font by lazy { BitmapFont() }

    override fun addedToEngine(engine: Engine) {
        sprites = engine.getEntitiesFor(Family.all(SpriteComponent::class.java).get())
        movables =
                engine.getEntitiesFor(
                        Family.all(Controlled::class.java, AnimatablePosition::class.java).get()
                )
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        batch.begin()
        for (entity in sprites) {
            val sprite = spriteComponentMap.get(entity).sprite
            sprite.draw(batch)
        }
        batch.end()

        drawShapes(state.selected)

        screenViewport.apply()
        batch.projectionMatrix = screenViewport.camera.combined
        batch.begin()
        for (entity in movables) {
            val info = unitInfoMap.get(entity)
            val position = animatablePositionMap.get(entity).position
            val health = info.health.toString()

            // Need to convert between coordinate systems
            val gameWorldPos =
                    Vector2(
                            position.x + 0.5f,
                            position.y + 1
                    ) // Put text on top and "centered" to be modified in screen coords later
            val screenPos = Vector2()
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

    private fun drawShapes(selected: Entity?) {
        // Camera setup
        val cam = state.viewport.camera
        val minX = cam.position.x - state.viewport.worldWidth / 2
        val maxX = cam.position.x + state.viewport.worldWidth / 2
        val minY = cam.position.y - state.viewport.worldHeight / 2
        val maxY = cam.position.y + state.viewport.worldHeight / 2

        // Renderer setup
        shapeRenderer.projectionMatrix = cam.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.DARK_GRAY

        // Draw gridlines
        for (x in minX.toInt()..maxX.toInt()) {
            shapeRenderer.line(x.toFloat(), minY, x.toFloat(), maxY)
        }
        for (y in minY.toInt()..maxY.toInt()) {
            shapeRenderer.line(minX, y.toFloat(), maxX, y.toFloat())
        }

        // Draw highlight around selected unit
        selected?.let { unit ->
            val position = animatablePositionMap.get(unit).position
            shapeRenderer.color = Color.YELLOW
            shapeRenderer.rect(
                    position.x,
                    position.y,
                    1f,
                    1f
            ) // Hardcoding size of unit at 1x1. Change if adding tanks or something later
        }

        shapeRenderer.end()
    }
}
