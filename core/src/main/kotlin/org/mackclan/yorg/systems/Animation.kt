package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.AnimationComponent

class Animation : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>
    private lateinit var state: GameState

    private val animationComponentMap = ComponentMapper.getFor(AnimationComponent::class.java)
    private val batch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val screenViewport by lazy { ScreenViewport() }

    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(Family.all(AnimationComponent::class.java).get())
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        batch.begin()
        for (entity in entities) {
            val animation = animationComponentMap.get(entity)
            val frame = animation.animation.getKeyFrame(animation.time, true)
            batch.draw(frame, animation.posX, animation.posY, 1f, 1f)
            animation.time += deltaTime
        }
        batch.end()
    }

    fun resize(width: Int, height: Int) {
        state.viewport.update(width, height, true)
        screenViewport.update(width, height, true)
    }
}
