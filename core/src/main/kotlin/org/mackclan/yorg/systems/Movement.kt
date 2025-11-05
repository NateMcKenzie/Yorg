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
import org.mackclan.yorg.components.Animations
import org.mackclan.yorg.utils.bfsTile
import kotlin.math.abs

class Movement : EntitySystem() {
    private lateinit var movables: ImmutableArray<Entity>
    private lateinit var obstacles: MutableList<MutableList<Boolean>>
    private lateinit var state: GameState

    private val spriteMap = ComponentMapper.getFor(SpriteComponent::class.java)
    private val animatablePositionMap = ComponentMapper.getFor(AnimatablePosition::class.java)
    private val animationMap = ComponentMapper.getFor(AnimationComponent::class.java)
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
            val animatablePosition = animatablePositionMap.get(entity)
            val animation = animationMap.get(entity)
            // Move selected unit if needed
            if (entity == state.selected) {
                val controlled = controlledMap.get(entity)
                val tiles = genBfsGraph(animatablePosition.position, controlled.walkRange)
                drawRange(tiles.values)
                controlled.desiredMove?.let { moveLocation ->
                    val desiredTile =
                            tiles.get(moveLocation.x.toInt() + moveLocation.y.toInt() * state.viewport.worldWidth.toInt())
                    if (desiredTile != null && controlled.actionPoints > 0) {
                        animation.activeAnimation = Animations.run
                        animatablePosition.path.clear()
                        animatablePosition.path.addAll(smoothPath(getPath(tiles, moveLocation)))
                        val target = Vector2(animatablePosition.path.get(0).x.toFloat(), animatablePosition.path.get(0).y.toFloat())
                        animatablePosition.velocity = target
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
            if (animatablePosition.path.isNotEmpty()){
                val target = Vector2(animatablePosition.path.get(0).x.toFloat(), animatablePosition.path.get(0).y.toFloat())
                val scaledMove = animatablePosition.velocity.cpy().scl(deltaTime)
                val nextPos = animatablePosition.position.cpy().add(scaledMove)

                if (nextPos.dst(target) <= animatablePosition.position.dst(target)) {
                    animatablePosition.position = nextPos.cpy()
                } else {
                    animatablePosition.path.removeAt(0)
                    animatablePosition.position = target.cpy()
                    if(animatablePosition.path.isEmpty()){
                        animation.activeAnimation = Animations.idle
                    } else {
                        val newTarget = Vector2(animatablePosition.path.get(0).x.toFloat(), animatablePosition.path.get(0).y.toFloat())
                        animatablePosition.velocity = newTarget
                                    .cpy()
                                    .sub(animatablePosition.position)
                                    .nor()
                                    .scl(animatablePosition.speed)
                    }
                }
            }
        }
    }


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

    private fun getPath(tiles: Map<Int, bfsTile>, pos : Vector2) : List<bfsTile>{
        val id = pos.x.toInt() + pos.y.toInt() * state.viewport.worldWidth.toInt()
        val path = mutableListOf<bfsTile>()
        var next = tiles.get(id)
        while (next != null){
            path.add(next)
            next = next.predecessor
        }

        return path.reversed()
    }

private fun smoothPath(path: List<bfsTile>): List<bfsTile> {
    if (path.size < 3) return path.subList(1,path.size)

    val smoothed = mutableListOf(path[0])
    var current = 0

    while (current < path.size - 1) {
        // Look ahead as far as possible
        var farthest = current + 1
        for (i in current + 2 ..< path.size) {
            if (hasLineOfSight(path[current], path[i])) {
                farthest = i
            } else {
                break
            }
        }

        smoothed.add(path[farthest])
        current = farthest
    }

    smoothed.removeAt(0)
    return smoothed
}

private fun hasLineOfSight(start: bfsTile, end: bfsTile): Boolean {
    // Bresenham's Line Algorithm
    val dx = abs(end.x - start.x)
    val dy = abs(end.y - start.y)
    var x = start.x
    var y = start.y
    val stepX = if (end.x > start.x) 1 else -1
    val stepY = if (end.y > start.y) 1 else -1

    if (dx > dy) {
        var err = dx / 2.0f
        while (x != end.x) {
            if (obstacles[y][x]) return false
            err -= dy
            if (err < 0) {
                y += stepY
                err += dx.toFloat()
            }
            x += stepX
        }
    } else {
        var err = dy / 2.0f
        while (y != end.y) {
            if (obstacles[y][x]) return false
            err -= dx
            if (err < 0) {
                x += stepX
                err += dy.toFloat()
            }
            y += stepY
        }
    }

    return true
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
