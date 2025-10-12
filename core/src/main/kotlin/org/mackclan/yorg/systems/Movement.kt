package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.Cover
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.SpriteComponent
import org.mackclan.yorg.systems.Turn

class Movement : EntitySystem() {
    private lateinit var movables: ImmutableArray<Entity>
    private val obstacles by lazy { HashSet<Pair<Int, Int>>() }
    private lateinit var state: GameState

    private val spriteMap = ComponentMapper.getFor(SpriteComponent::class.java)
    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val batch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val screenViewport by lazy { ScreenViewport() }

    override fun addedToEngine(engine: Engine) {
        movables =
            engine.getEntitiesFor(Family.all(Controlled::class.java, SpriteComponent::class.java).get())
        engine.getEntitiesFor(Family.all(Cover::class.java).get())
            .forEach { obstacle ->
                val sprite = spriteMap.get(obstacle).sprite
                obstacles.add(Pair(sprite.x.toInt(), sprite.y.toInt()))
            }
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        // Render Ranges
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        val selectedSprites = Array<Entity>()
        for (entity in movables) {
            if (controlledMap.get(entity).selected) {
                selectedSprites.add(entity)
            }
        }
        drawShapes(selectedSprites)

        // Move Logic
        state.selected?.let { selected ->
            val selectedControlled = controlledMap.get(selected)
            selectedControlled.desiredMove?.let { moveLocation ->
                val selectedSprite = spriteMap.get(selected).sprite
                val distance =
                    findWalkDistance(
                        selectedSprite.x.toInt(),
                        selectedSprite.y.toInt(),
                        moveLocation.x.toInt(),
                        moveLocation.y.toInt(),
                    )
                if (distance <= selectedControlled.walkRange && selectedControlled.actionPoints > 0) {
                    selectedSprite.x = moveLocation.x
                    selectedSprite.y = moveLocation.y

                    selectedControlled.desiredMove = null
                    selectedControlled.actionPoints -= 1
                    if (selectedControlled.actionPoints <= 0) spendUnit(selectedControlled, state)
                }
                //TODO: Implement two action point moves
            }
        }
    }

    private fun drawShapes(selectedSprites: Array<Entity>) {
        shapeRenderer.projectionMatrix = state.viewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (selected in selectedSprites) {
            val sprite = spriteMap.get(selected).sprite
            val range = controlledMap.get(selected).walkRange
            drawRange(sprite, range)
        }
        shapeRenderer.end()
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
                for (tile in candidates) {
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

    private fun findWalkDistance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        class bfsTile(val x: Int, val y: Int, val distance: Int)

        val listed = HashSet<Int>()
        val tileQueue = ArrayDeque<bfsTile>()
        tileQueue.add(bfsTile(x1, y1, 0))
        listed.add((x1 + y1 * state.viewport.worldWidth).toInt())

        while (tileQueue.isNotEmpty()) {
            val current = tileQueue.removeFirst()
            val candidates =
                listOf(
                    Pair(current.x + 1, current.y),
                    Pair(current.x - 1, current.y),
                    Pair(current.x, current.y + 1),
                    Pair(current.x, current.y - 1)
                )
            for (tile in candidates) {
                if (tile.first < 0 || tile.second < 0) break
                if (tile.first == x2 && tile.second == y2) return current.distance + 1

                val id = (tile.first + tile.second * state.viewport.worldWidth.toInt())
                if (!listed.contains(id) && !obstacles.contains(tile)) {
                    tileQueue.add(bfsTile(tile.first, tile.second, current.distance + 1))
                    listed.add(id)
                }
            }
        }
        return Int.MAX_VALUE
    }

    fun resize(width: Int, height: Int) {
        state.viewport.update(width, height, true)
        screenViewport.update(width, height, true)
    }
}
