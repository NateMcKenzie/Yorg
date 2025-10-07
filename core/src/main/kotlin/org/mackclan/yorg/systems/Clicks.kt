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

    private lateinit var state: GameState

    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(Family.all(Sprite::class.java, Controlled::class.java).get())

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
                    val selectedControlled = controlledMap.get(selected)
                    selectedControlled.desiredMove = touchPos
                }
            }
        }
    }

    private fun findSquaredDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return abs(x2 - x1) + abs(y2 - y1)
    }


    private fun changeTurns() {
        state.selected?.let { entity ->
            controlledMap.get(entity).selected = false
            state.selected = null
        }
        state.playerTurn = !state.playerTurn
    }
}
