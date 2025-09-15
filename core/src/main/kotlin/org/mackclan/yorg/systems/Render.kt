package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
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
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.UnitInfo

class Render : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>
    private lateinit var movables: ImmutableArray<Entity>
    private val obstacles by lazy { HashSet<Pair<Int, Int>>() }
    private lateinit var state: GameState

    private val spriteMap = ComponentMapper.getFor(Sprite::class.java)
    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val unitInfoMap = ComponentMapper.getFor(UnitInfo::class.java)
    private val batch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val screenViewport by lazy { ScreenViewport() }

    private val font by lazy { BitmapFont() }

    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(Family.all(Sprite::class.java).get())
        movables = engine.getEntitiesFor(Family.all(Controlled::class.java, Sprite::class.java).get())
        engine.getEntitiesFor(Family.all(Sprite::class.java).exclude(Controlled::class.java).get())
                .forEach { obstacle ->
                    val sprite = spriteMap.get(obstacle).sprite
                    obstacles.add(Pair(sprite.x.toInt(), sprite.y.toInt()))
                }
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
            val sprite = spriteMap.get(entity).sprite
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
            val sprite = spriteMap.get(entity).sprite
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
            val sprite = spriteMap.get(selected).sprite
            drawHighlight(sprite)
        }
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (selected in selectedSprites) {
            val sprite = spriteMap.get(selected).sprite
            val range = controlledMap.get(selected).walkRange
            drawRange(sprite, range)
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

    private fun drawRange(sprite: com.badlogic.gdx.graphics.g2d.Sprite, range: Int) {
        class bfsTile(val x: Int, val y: Int, val distance: Int)
        shapeRenderer.color = Color(0f, 0f, 0.3f, 0.2f)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        val listed = HashSet<Int>()
        val tileQueue = ArrayDeque<bfsTile>()
        tileQueue.add(bfsTile(sprite.x.toInt(), sprite.y.toInt(), 0))
        listed.add((sprite.x + sprite.y * state.viewport.worldWidth).toInt())

        while (tileQueue.isNotEmpty()) {
            val current = tileQueue.removeFirst()
            shapeRenderer.rect(current.x.toFloat(), current.y.toFloat(), 1f, 1f)
            if (current.distance < range) {
                val candidates =
                        listOf(
                                Pair(current.x + 1, current.y),
                                Pair(current.x - 1, current.y),
                                Pair(current.x, current.y + 1),
                                Pair(current.x, current.y - 1)
                        )
                for (tile in candidates){
                    if (tile.first < 0 || tile.second < 0) break
                    val id = (tile.first + tile.second * state.viewport.worldWidth.toInt())
                    if (!listed.contains(id) && !obstacles.contains(tile)) {
                        tileQueue.add(bfsTile(tile.first, tile.second, current.distance + 1))
                        listed.add(id)
                    }
                }
            }
        }
    }
}
