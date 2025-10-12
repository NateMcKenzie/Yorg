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

class Turn : EntitySystem() {
    private lateinit var units: ImmutableArray<Entity>
    private lateinit var state: GameState

    private val controlledMap = ComponentMapper.getFor(Controlled::class.java)

    override fun addedToEngine(engine: Engine) {
        units = engine.getEntitiesFor(Family.all(Controlled::class.java).get())
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        var unspentUnitCount = 0
        for (unit in units){
            val controlled = controlledMap.get(unit)
            if (controlled.actionPoints > 0 && controlled.playerControlled == state.playerTurn){
                unspentUnitCount += 1
            }
        }
        if (unspentUnitCount == 0){
            changeTurns()
        }
    }

    private fun changeTurns() {
        for (unit in units){
            val controlled = controlledMap.get(unit)
            if (controlled.playerControlled == state.playerTurn){
                controlled.selected = false
                controlled.actionPoints = 2
            }
            state.selected = null
        }

        state.playerTurn = !state.playerTurn
    }
}


fun spendUnit(controlled : Controlled, state : GameState) {
    controlled.actionPoints = 0
    controlled.selected = false
    state.selected = null
}

