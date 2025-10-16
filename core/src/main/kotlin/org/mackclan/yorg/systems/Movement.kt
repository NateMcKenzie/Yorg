package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.*

class Movement : EntitySystem() {
    private lateinit var movables: ImmutableArray<Entity>
    private val obstacles by lazy { HashSet<Pair<Int, Int>>() }
    private lateinit var state: GameState

    private val spriteMap = ComponentMapper.getFor(SpriteComponent::class.java)
    private val animatablePositionMap = ComponentMapper.getFor(AnimatablePosition::class.java)
    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val screenViewport by lazy { ScreenViewport() }

    override fun addedToEngine(engine: Engine) {
        movables =
                engine.getEntitiesFor(
                        Family.all(Controlled::class.java, AnimatablePosition::class.java).get()
                )
        engine.getEntitiesFor(Family.all(Cover::class.java).get()).forEach { obstacle ->
            val sprite = spriteMap.get(obstacle).sprite
            obstacles.add(Pair(sprite.x.toInt(), sprite.y.toInt()))
        }
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        // Render Ranges
        state.viewport.apply()
        for (entity in movables) {
            // Move selected unit if needed
            if (entity == state.selected) {
                drawRange(entity)
                val controlled = controlledMap.get(entity)
                controlled.desiredMove?.let { moveLocation ->
                    val animatablePosition = animatablePositionMap.get(entity)
                    val distance =
                            findWalkDistance(
                                    animatablePosition.position.x.toInt(),
                                    animatablePosition.position.y.toInt(),
                                    moveLocation.x.toInt(),
                                    moveLocation.y.toInt(),
                            )
                    if (distance <= controlled.walkRange && controlled.actionPoints > 0) {
                        animatablePosition.target = moveLocation.cpy()
                        animatablePosition.velocity =
                                moveLocation
                                        .cpy()
                                        .sub(animatablePosition.position)
                                        .nor()
                                        .scl(animatablePosition.speed)

                        controlled.desiredMove = null
                        controlled.actionPoints -= 1
                        if (controlled.actionPoints <= 0) spendUnit(controlled, state)
                    }
                    // TODO: Implement two action point moves
                }
            }

            // Animate movement of any moving unit
            val animatablePosition = animatablePositionMap.get(entity)
            if (animatablePosition.target.dst(animatablePosition.position) > 1f) {
                animatablePosition.position.add(animatablePosition.velocity.cpy().scl(deltaTime))
            } else if (animatablePosition.target.x != animatablePosition.position.x &&
                            animatablePosition.target.y != animatablePosition.position.y
            ) {
                animatablePosition.position = animatablePosition.target.cpy()
            }
        }
    }

    private class bfsTile(val x: Int, val y: Int, val distance: Int)

    private fun drawRange(entity : Entity) {
        // Renderer setup
        shapeRenderer.projectionMatrix = state.viewport.camera.combined
        shapeRenderer.color = Color(0f, 0f, 0.3f, 0.2f)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // BFS setup
        val position = animatablePositionMap.get(entity).position
        val listed = HashSet<Int>()
        val tileQueue = ArrayDeque<bfsTile>()
        tileQueue.add(bfsTile(position.x.toInt(), position.y.toInt(), 0))
        listed.add((position.x + position.y * state.viewport.worldWidth).toInt())

        // BFS main
        val range = controlledMap.get(entity).walkRange
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

        shapeRenderer.end()
    }

    private fun findWalkDistance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
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
