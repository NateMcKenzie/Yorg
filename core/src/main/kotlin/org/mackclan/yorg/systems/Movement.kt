package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.*

class Movement : EntitySystem() {
    private lateinit var movables: ImmutableArray<Entity>
    private lateinit var obstacles: MutableList<MutableList<Boolean>>
    private lateinit var state: GameState

    private val spriteMap = ComponentMapper.getFor(SpriteComponent::class.java)
    private val animatablePositionMap = ComponentMapper.getFor(AnimatablePosition::class.java)
    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val screenViewport by lazy { ScreenViewport() }

    override fun addedToEngine(engine: Engine) {
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState

        obstacles =
                MutableList<MutableList<Boolean>>(state.viewport.worldHeight.toInt()) {
                    MutableList<Boolean>(state.viewport.worldWidth.toInt()) { false }
                }

        movables =
                engine.getEntitiesFor(
                        Family.all(Controlled::class.java, AnimatablePosition::class.java).get()
                )

        engine.getEntitiesFor(Family.all(Cover::class.java).get()).forEach { obstacle ->
            val sprite = spriteMap.get(obstacle).sprite
            obstacles[sprite.y.toInt()][sprite.x.toInt()] = true
        }
    }

    override fun update(deltaTime: Float) {
        // Render Ranges
        state.viewport.apply()
        for (entity in movables) {
            // Move selected unit if needed
            if (entity == state.selected) {
                val animatablePosition = animatablePositionMap.get(entity)
                val controlled = controlledMap.get(entity)
                val tiles = genBfsGraph(animatablePosition.position, controlled.walkRange)
                drawRange(tiles.values)
                controlled.desiredMove?.let { moveLocation ->
                    val desiredTile =
                            tiles.get(moveLocation.x.toInt() + moveLocation.y.toInt() * state.viewport.worldWidth.toInt())
                    if (desiredTile != null && controlled.actionPoints > 0) {
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
                }
                // TODO: Implement two action point moves
            }

            // Animate movement of any moving unit
            val animatablePosition = animatablePositionMap.get(entity)
            val scaledMove = animatablePosition.velocity.cpy().scl(deltaTime)
            val nextPos = animatablePosition.position.cpy().add(scaledMove)
            if (nextPos.dst(animatablePosition.target) <=
                            animatablePosition.position.dst(animatablePosition.target)
            ) {
                animatablePosition.position = nextPos.cpy()
            } else {
                animatablePosition.position = animatablePosition.target.cpy()
            }
        }
    }

    private class bfsTile(val x: Int, val y: Int, val distance: Int, val predecessor: bfsTile?)

    private fun genBfsGraph(position: Vector2, range: Int): Map<Int, bfsTile> {
        // BFS setup
        val listed = HashMap<Int, bfsTile>()
        val tileQueue = ArrayDeque<bfsTile>()
        val startTile = bfsTile(position.x.toInt(), position.y.toInt(), 0, null)
        tileQueue.add(startTile)
        listed.put((position.x + position.y * state.viewport.worldWidth).toInt(), startTile)

        // BFS main
        while (tileQueue.isNotEmpty()) {
            val current = tileQueue.removeFirst()
            if (current.distance < range) {
                val candidates =
                        listOf(
                                Pair(current.x + 1, current.y),
                                Pair(current.x - 1, current.y),
                                Pair(current.x, current.y + 1),
                                Pair(current.x, current.y - 1)
                        )
                for (tile in candidates) {
                    val worldWidth = state.viewport.worldWidth.toInt()
                    val worldHeight = state.viewport.worldHeight.toInt()
                    if (tile.first < 0 || tile.second < 0 || tile.first >= worldWidth || tile.second >= worldHeight)
                        continue
                    val id = tile.first + tile.second * worldWidth
                    if (!listed.contains(id) && !obstacles[tile.second][tile.first]) {
                        val nextTile =
                                bfsTile(tile.first, tile.second, current.distance + 1, current)
                        tileQueue.add(nextTile)
                        listed.put(id, nextTile)
                    }
                }
            }
        }
        return listed
    }

    private fun drawRange(tiles: Collection<bfsTile>) {
        shapeRenderer.projectionMatrix = state.viewport.camera.combined
        shapeRenderer.color = Color(0f, 0f, 0.3f, 0.2f)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (tile in tiles) {
            shapeRenderer.rect(tile.x.toFloat(), tile.y.toFloat(), 1f, 1f)
        }
        shapeRenderer.end()
    }

    fun resize(width: Int, height: Int) {
        state.viewport.update(width, height, true)
        screenViewport.update(width, height, true)
    }
}
