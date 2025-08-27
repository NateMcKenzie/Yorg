package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.UnitInfo
import kotlin.math.abs
import kotlin.math.floor

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
            touchPos.set(
                Gdx.input.x.toFloat(),
                Gdx.input.y.toFloat()
            ) // This is window coords (in pixels y increases from top down)
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
                    controlled.selected = !controlled.selected
                    state.selected = clickedEntity
                } else {
                    //Kotlin idiom: if state.selected not null do this with 'selected' being that value, otherwise just skip
                    state.selected?.let { selected ->
                        val selectedSprite = selected.getComponent(Sprite::class.java).sprite
                        val distance = findSquaredDistance(sprite.x, selectedSprite.x, sprite.y, selectedSprite.y)
                        val selectedInfo = selected.getComponent(UnitInfo::class.java)
                        val damage = (selectedInfo.damage * (selectedInfo.range / distance)).toInt()
                        clickedEntity.getComponent(UnitInfo::class.java).health -= damage
                        changeTurns()
                    }
                }
            } else {
                state.selected?.let { selected ->
                    val selectedSprite = spriteMap.get(selected).sprite
                    val distance = findSquaredDistance(selectedSprite.x, selectedSprite.y, touchPos.x, touchPos.y)
                    // Move selected
                    if (distance <= 5) {
                        selectedSprite.x = touchPos.x
                        selectedSprite.y = touchPos.y

                        changeTurns()
                    }
                }
            }
            //controlledMap.get(state.selected).selected = false
            //state.selected = null
        }
    }

    private fun findSquaredDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        // TODO: Does not account for obstacles, probably use BFS in future
        // In fact, the drawing won't work either. Distance might be its own whole system that does logic and rendering
        return abs(x2 - x1) + abs(y2 - y1)
    }

    private fun changeTurns(){
        controlledMap.get(state.selected).selected = false
        state.selected = null
        state.playerTurn = !state.playerTurn
    }
}
