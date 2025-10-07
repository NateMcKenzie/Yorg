package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.floor
import org.mackclan.yorg.components.*
import org.mackclan.yorg.entities.createPopup

class Clicks : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>

    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val spriteMap = ComponentMapper.getFor(Sprite::class.java)
    private val unitInfoMap = ComponentMapper.getFor(UnitInfo::class.java)

    // TODO: There's a fair bit of overlap between work here and in Render, might make sense to
    // combine movement into a system
    private val obstacles by lazy { HashSet<Pair<Int, Int>>() }

    private lateinit var state: GameState

    override fun addedToEngine(engine: Engine) {
        entities =
                engine.getEntitiesFor(Family.all(Sprite::class.java, Controlled::class.java).get())

        engine.getEntitiesFor(Family.all(Sprite::class.java).exclude(Controlled::class.java).get())
                .forEach { obstacle ->
                    val sprite = spriteMap.get(obstacle).sprite
                    obstacles.add(Pair(sprite.x.toInt(), sprite.y.toInt()))
                }

        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        if (Gdx.input.justTouched()) {
            val touchPos = Vector2()
            touchPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            state.viewport.unproject(touchPos)
            touchPos.set(floor(touchPos.x), floor(touchPos.y))

            var clickedEntity: Entity? = null
            for (entity in entities) {
                val sprite = spriteMap.get(entity).sprite
                if (sprite.x == touchPos.x && sprite.y == touchPos.y) {
                    clickedEntity = entity
                }
            }
            if (clickedEntity != null) {
                val controlled = controlledMap.get(clickedEntity)
                val sprite = spriteMap.get(clickedEntity).sprite
                if (state.playerTurn == controlled.playerControlled) {
                    // Deselect
                    controlled.selected = !controlled.selected
                    state.selected = clickedEntity
                } else {
                    // Open shoot popup
                    state.selected?.let { selected ->
                        val selectedSprite = spriteMap.get(selected).sprite
                        val distance =
                                findSquaredDistance(
                                        sprite.x,
                                        sprite.y,
                                        selectedSprite.x,
                                        selectedSprite.y
                                ) - 1
                        val selectedInfo = unitInfoMap.get(selected)
                        val damage = (selectedInfo.damage * (selectedInfo.range - distance)).toInt()
                        val chance = (selectedInfo.range - distance) / selectedInfo.range
                        val popup =
                                createPopup(damage, chance) { hit ->
                                    if (hit) {
                                        val info = unitInfoMap.get(clickedEntity)
                                        info.health -= damage
                                        if (info.health <= 0) engine.removeEntity(clickedEntity)
                                    }
                                    changeTurns()
                                }
                        engine.addEntity(popup)
                    }
                }
            } else {
                // Move
                state.selected?.let { selected ->
                    val selectedSprite = spriteMap.get(selected).sprite
                    val distance =
                            findWalkDistance(
                                    selectedSprite.x.toInt(),
                                    selectedSprite.y.toInt(),
                                    touchPos.x.toInt(),
                                    touchPos.y.toInt()
                            )
                    if (distance <= 5) {
                        selectedSprite.x = touchPos.x
                        selectedSprite.y = touchPos.y

                        // Recompute cover, TODO: should probably have a more robust way of handling this later
                        val selectedControlled = controlledMap.get(selected)
                        selectedControlled.coverUp    = if(obstacles.contains(Pair<Int,Int>(touchPos.x.toInt(), touchPos.y.toInt() + 1))){CoverLevel.Low} else {CoverLevel.None}
                        selectedControlled.coverRight = if(obstacles.contains(Pair<Int,Int>(touchPos.x.toInt() + 1, touchPos.y.toInt()))){CoverLevel.Low} else {CoverLevel.None}
                        selectedControlled.coverDown  = if(obstacles.contains(Pair<Int,Int>(touchPos.x.toInt(), touchPos.y.toInt() - 1))){CoverLevel.Low} else {CoverLevel.None}
                        selectedControlled.coverLeft  = if(obstacles.contains(Pair<Int,Int>(touchPos.x.toInt() - 1, touchPos.y.toInt()))){CoverLevel.Low} else {CoverLevel.None}

                        println(selectedControlled.coverUp    )
                        println(selectedControlled.coverRight )
                        println(selectedControlled.coverDown  )
                        println(selectedControlled.coverLeft  )

                        changeTurns()
                    }
                }
            }
        }
    }

    private fun findSquaredDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return abs(x2 - x1) + abs(y2 - y1)
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

    private fun changeTurns() {
        state.selected?.let { entity ->
            controlledMap.get(entity).selected = false
            state.selected = null
        }
        state.playerTurn = !state.playerTurn
    }
}
