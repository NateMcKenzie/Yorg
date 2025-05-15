package org.mackclan.yorg.views

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import org.mackclan.yorg.entities.createCamera
import org.mackclan.yorg.entities.createUnit
import org.mackclan.yorg.systems.Clicks
import org.mackclan.yorg.systems.Render

class GameView : ApplicationListener{
    private val engine by lazy { Engine() }

    //Entities

    //Systems
    private val render by lazy { Render() }
    private val clicks by lazy { Clicks() }

    override fun create() {
        engine.addEntity(createUnit())
        engine.addEntity(createCamera())

        engine.addSystem(render)
        engine.addSystem(clicks)
    }

    override fun resize(width: Int, height: Int) {
        render.resize(width, height)
    }

    override fun render() {
        render.update(Gdx.graphics.deltaTime)
        clicks.update(Gdx.graphics.deltaTime)
    }

    override fun pause() {
        //TODO("Not yet implemented")
    }

    override fun resume() {
        //TODO("Not yet implemented")
    }

    override fun dispose() {
        //TODO("Not yet implemented")
    }
}
