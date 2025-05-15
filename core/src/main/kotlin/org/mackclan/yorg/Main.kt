package org.mackclan.yorg

import com.badlogic.gdx.ApplicationListener
import org.mackclan.yorg.views.GameView

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms.  */
class Main : ApplicationListener {
    //TODO: Generalize this into a main menu
    private val currentState by lazy { GameView() }
    override fun create() {
        currentState.create()
    }

    override fun resize(width: Int, height: Int) {
        currentState.resize(width, height)
    }

    override fun render() {
        currentState.render()
    }

    override fun pause() {
        currentState.pause()
    }

    override fun resume() {
        currentState.resume()
    }

    override fun dispose() {
        currentState.dispose()
    }
}
