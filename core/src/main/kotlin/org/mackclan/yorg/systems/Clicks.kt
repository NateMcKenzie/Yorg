package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.Sprite
import kotlin.math.abs
import kotlin.math.floor

class Clicks : EntitySystem() {
    private lateinit var entities : ImmutableArray<Entity>

    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)
    private val spriteMap = ComponentMapper.getFor(Sprite::class.java)

    private lateinit var state : GameState

    override fun addedToEngine(engine: Engine){
        entities = engine.getEntitiesFor(Family.all(Sprite::class.java, Controlled::class.java).get())
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime : Float){
        if (Gdx.input.justTouched()){
            val touchPos = Vector2()
            touchPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()) // This is window coords (in pixels y increases from top down)
            state.viewport.unproject(touchPos)
            touchPos.set(floor(touchPos.x), floor(touchPos.y))

            var unitClicked = false
            var selected : Entity? = null

            for (entity in entities){
                val sprite = spriteMap.get(entity).sprite
                val controlled = controlledMap.get(entity)
                if (sprite.x == touchPos.x && sprite.y == touchPos.y
                    && state.playerTurn == controlled.playerControlled){
                    controlled.selected = !controlled.selected
                    unitClicked = true
                } else if (selected != null){
                    controlled.selected = false
                }
                if (controlled.selected){
                    selected?.getComponent(Controlled::class.java)?.selected = false
                    selected = entity
                }
            }
            state.selected = selected
            if (!unitClicked && selected != null){
                // Move selected
                val sprite = spriteMap.get(selected).sprite
                val distance = findSquaredDistance(sprite.x.toInt(), sprite.y.toInt(), touchPos.x.toInt(), touchPos.y.toInt())
                if (distance <= 5){
                    sprite.x = touchPos.x
                    sprite.y = touchPos.y

                    // Change turns
                    state.playerTurn = !state.playerTurn
                    state.selected = null
                    controlledMap.get(selected).selected = false
                }
            }
        }
    }

    private fun findSquaredDistance(x1 : Int, y1 : Int, x2 : Int, y2 : Int) : Int{
        // TODO: Does not account for obstacles, probably use BFS in future
        // In fact, the drawing won't work either. Distance might be its own whole system that does logic and rendering
        return abs(x2 - x1) + abs(y2 - y1)
    }
}
