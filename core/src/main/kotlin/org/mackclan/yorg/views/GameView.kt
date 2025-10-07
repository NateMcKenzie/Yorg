package org.mackclan.yorg.views

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import org.mackclan.yorg.components.CoverLevel
import org.mackclan.yorg.entities.createGameState
import org.mackclan.yorg.entities.createObstacle
import org.mackclan.yorg.entities.createUnit
import org.mackclan.yorg.systems.Clicks
import org.mackclan.yorg.systems.HUD
import org.mackclan.yorg.systems.Render

class GameView : ApplicationListener {
    private val engine by lazy { Engine() }

    // Systems
    private val render by lazy { Render() }
    private val clicks by lazy { Clicks() }
    private val hud by lazy { HUD() }

    override fun create() {
        engine.addEntity(createUnit(10f, 8f, 5, true))
        engine.addEntity(createUnit(12f, 9f, 5, true))
        engine.addEntity(createUnit(14f, 7f, 5, true))

        engine.addEntity(createUnit(7f, 14f, 5, false))
        engine.addEntity(createUnit(10f, 14f, 5, false))
        engine.addEntity(createUnit(13f, 14f, 5, false))

        engine.addEntity(createObstacle(9f, 8f, CoverLevel.Low))
        engine.addEntity(createObstacle(10f, 9f, CoverLevel.Low))
        engine.addEntity(createObstacle(11f, 10f, CoverLevel.Low))
        engine.addEntity(createObstacle(12f, 10f, CoverLevel.Low))
        engine.addEntity(createObstacle(13f, 9f, CoverLevel.Low))
        engine.addEntity(createObstacle(14f, 8f, CoverLevel.Low))
        // There should only ever be one; TODO?
        engine.addEntity(createGameState())

        engine.addSystem(render)
        engine.addSystem(clicks)
        engine.addSystem(hud)
    }

    override fun resize(width: Int, height: Int) {
        render.resize(width, height)
        hud.resize(width, height)
    }

    override fun render() {
        render.update(Gdx.graphics.deltaTime)
        clicks.update(Gdx.graphics.deltaTime)
        hud.update(Gdx.graphics.deltaTime)
    }

    override fun pause() {
        // TODO("Not yet implemented")
    }

    override fun resume() {
        // TODO("Not yet implemented")
    }

    override fun dispose() {
        // TODO("Not yet implemented")
    }
}
