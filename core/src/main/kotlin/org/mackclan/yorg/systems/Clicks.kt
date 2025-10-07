package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import org.mackclan.yorg.components.*
import org.mackclan.yorg.entities.createPopup
import kotlin.math.abs
import kotlin.math.floor

class Clicks : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>
    private lateinit var obstacles: ImmutableArray<Entity>

    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val spriteComponentMap = ComponentMapper.getFor(SpriteComponent::class.java)
    private val unitInfoMap = ComponentMapper.getFor(UnitInfo::class.java)
    private val coverMap = ComponentMapper.getFor(Cover::class.java)

    private lateinit var state: GameState

    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(Family.all(SpriteComponent::class.java, Controlled::class.java).get())
        obstacles = engine.getEntitiesFor(Family.all(SpriteComponent::class.java, Cover::class.java).get())

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
                val sprite = spriteComponentMap.get(entity).sprite
                if (sprite.x == touchPos.x && sprite.y == touchPos.y) {
                    clickedEntity = entity
                }
            }
            if (clickedEntity != null) {
                val controlled = controlledMap.get(clickedEntity)
                val sprite = spriteComponentMap.get(clickedEntity).sprite
                if (state.playerTurn == controlled.playerControlled) {
                    // Deselect
                    controlled.selected = !controlled.selected
                    state.selected = clickedEntity
                } else {
                    // Open shoot popup
                    state.selected?.let { selected ->
                        val selectedSprite = spriteComponentMap.get(selected).sprite
                        val selectedInfo = unitInfoMap.get(selected)
                        val distance =
                            findSquaredDistance(
                                sprite.x,
                                sprite.y,
                                selectedSprite.x,
                                selectedSprite.y
                            ) - 1
                        val chance = calculateChance(selectedSprite, sprite, distance, selectedInfo.range)
                        val damage = (selectedInfo.damage * (selectedInfo.range - distance)).toInt()
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

    private fun calculateChance(sourceSprite: Sprite, targetSprite: Sprite, distance: Float, range: Int): Float {
        val baseChance = (range - distance) / range

        // Factor in cover. TODO: Currently very simplistic
        val absY = abs(sourceSprite.y - targetSprite.y)
        val absX = abs(sourceSprite.x - targetSprite.x)
        val direction = Vector2()
        if (absY > absX) {
            direction.y = if (sourceSprite.y < targetSprite.y) -1f else 1f
        } else {
            direction.x = if (sourceSprite.x < targetSprite.x) -1f else 1f
        }
        val primaryCoverPos = Vector2(targetSprite.x, targetSprite.y).add(direction)

        var chanceModifier = 0f
        for (obstacle in obstacles) {
            val sprite = spriteComponentMap.get(obstacle).sprite
            val location = Vector2(sprite.x, sprite.y)
            if (location == primaryCoverPos) {
                val cover = coverMap.get(obstacle).level
                chanceModifier = cover.ordinal * -0.2f
            }
        }

        return baseChance + chanceModifier
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
