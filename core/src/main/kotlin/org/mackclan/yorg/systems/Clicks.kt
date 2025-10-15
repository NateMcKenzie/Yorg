package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.floor
import org.mackclan.yorg.components.*
import org.mackclan.yorg.entities.createPopup
import org.mackclan.yorg.systems.Turn

class Clicks : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>
    private lateinit var obstacles: ImmutableArray<Entity>

    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val spriteComponentMap = ComponentMapper.getFor(SpriteComponent::class.java)
    private val animatablePositionMap = ComponentMapper.getFor(AnimatablePosition::class.java)
    private val unitInfoMap = ComponentMapper.getFor(UnitInfo::class.java)
    private val coverMap = ComponentMapper.getFor(Cover::class.java)

    private lateinit var state: GameState

    override fun addedToEngine(engine: Engine) {
        entities =
                engine.getEntitiesFor(
                        Family.all(AnimatablePosition::class.java, Controlled::class.java).get()
                )
        obstacles =
                engine.getEntitiesFor(
                        Family.all(SpriteComponent::class.java, Cover::class.java).get()
                )

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
                val position = animatablePositionMap.get(entity).position
                if (position.x == touchPos.x && position.y == touchPos.y) {
                    clickedEntity = entity
                }
            }
            if (clickedEntity != null) {
                val controlled = controlledMap.get(clickedEntity)
                val clickedPosition = animatablePositionMap.get(clickedEntity).position
                if (state.playerTurn == controlled.playerControlled) {
                    // Toggle select of clicked unit
                    if(controlled.actionPoints > 0){
                        controlled.selected = !controlled.selected
                        state.selected = clickedEntity
                    }
                } else {
                    // Open shoot popup
                    state.selected?.let { selected ->
                        val selectedPosition = animatablePositionMap.get(selected).position
                        val selectedInfo = unitInfoMap.get(selected)
                        val selectedControlled = controlledMap.get(selected)
                        if (selectedControlled.actionPoints > 0) {
                            val distance =
                                    findSquaredDistance(
                                            clickedPosition.x,
                                            clickedPosition.y,
                                            selectedPosition.x,
                                            selectedPosition.y
                                    ) - 1
                            val chance =
                                    calculateChance(
                                            selectedPosition,
                                            clickedPosition,
                                            distance,
                                            selectedInfo.range
                                    )
                            val damage =
                                    (selectedInfo.damage * (selectedInfo.range - distance)).toInt()
                            val popup =
                                    createPopup(damage, chance) { hit ->
                                        if (hit) {
                                            val info = unitInfoMap.get(clickedEntity)
                                            info.health -= damage
                                            if (info.health <= 0) engine.removeEntity(clickedEntity)
                                        }
                                        spendUnit(selectedControlled, state)
                                    }
                            engine.addEntity(popup)
                        }
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

    private fun calculateChance(
            sourcePosition: Vector2,
            targetPosition: Vector2,
            distance: Float,
            range: Int
    ): Float {
        val baseChance = (range - distance) / range

        // Factor in cover. TODO: Currently very simplistic
        val absY = abs(sourcePosition.y - targetPosition.y)
        val absX = abs(sourcePosition.x - targetPosition.x)
        val direction = Vector2()
        if (absY > absX) {
            direction.y = if (sourcePosition.y < targetPosition.y) -1f else 1f
        } else {
            direction.x = if (sourcePosition.x < targetPosition.x) -1f else 1f
        }
        val primaryCoverPos = Vector2(targetPosition).add(direction)

        var chanceModifier = 0f
        for (obstacle in obstacles) {
            val sprite = spriteComponentMap.get(obstacle).sprite
            if (sprite.x == primaryCoverPos.x && sprite.y == primaryCoverPos.y) {
                val cover = coverMap.get(obstacle).level
                chanceModifier = cover.ordinal * -0.2f
            }
        }

        return baseChance + chanceModifier
    }

    private fun findSquaredDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return abs(x2 - x1) + abs(y2 - y1)
    }
}
