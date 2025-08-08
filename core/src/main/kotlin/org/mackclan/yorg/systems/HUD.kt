package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.UnitInfo

class HUD : EntitySystem() {
    private lateinit var state: GameState

    private val batch by lazy { SpriteBatch() }
    private val font by lazy { BitmapFont() }

    private val viewport by lazy { ScreenViewport() }

    override fun addedToEngine(engine: Engine) {
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }
    override fun update(deltaTime: Float) {
        viewport.apply()
        batch.projectionMatrix = viewport.camera.combined
        batch.begin()
        // Kotlin idiom: let executes the lambda if the preceding code does not evaluate to null
        state.selected?.getComponent(UnitInfo::class.java)?.let { info ->
            font.draw(batch, "Health: ${info.health}", 10f, viewport.worldHeight - 10f)
        }
        batch.end()
    }

    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }
}
