package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.AnimationComponent
import org.mackclan.yorg.components.Animations
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.Directions
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.Position
import org.mackclan.yorg.components.Velocity

class UnitAnimation : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>
    private lateinit var state: GameState

    private val animationComponentMap = ComponentMapper.getFor(AnimationComponent::class.java)
    private val positionMap = ComponentMapper.getFor(Position::class.java)
    private val batch by lazy { SpriteBatch() }
    private val screenViewport by lazy { ScreenViewport() }

    private val animations: List<Animation<TextureRegion>>

    init {
        var robotAtlas: TextureAtlas = TextureAtlas("animations/robot1/robot1.atlas")
        animations =
                listOf(
                        Animation<TextureRegion>(
                                0.1667f,
                                robotAtlas.findRegions("idle"),
                                Animation.PlayMode.LOOP
                        ),
                        Animation<TextureRegion>(
                                0.0834f,
                                robotAtlas.findRegions("run"),
                                Animation.PlayMode.LOOP
                        ),
                        Animation<TextureRegion>(
                                0.0417f,
                                robotAtlas.findRegions("turn_to_run"),
                                Animation.PlayMode.NORMAL
                        ),
                        Animation<TextureRegion>(
                                0.0834f,
                                robotAtlas.findRegions("fire"),
                                Animation.PlayMode.NORMAL
                        ),
                )
    }

    override fun addedToEngine(engine: Engine) {
        entities =
                engine.getEntitiesFor(
                        Family.all(AnimationComponent::class.java, Position::class.java, Controlled::class.java).get()
                )
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        batch.begin()

        for (entity in entities) {
            val animation = animationComponentMap.get(entity)
            val position = positionMap.get(entity)
            val activeAnimation = animations[animation.activeAnimation.ordinal]

            if (activeAnimation.isAnimationFinished(animation.time)) {
                if (animation.activeAnimation == Animations.turn_to_run) {
                    animation.activeAnimation = Animations.run
                    animation.time = 0f
                } else {
                    animation.activeAnimation = Animations.idle
                }
            }

            val frame = activeAnimation.getKeyFrame(animation.time, true)
            if (animation.facing == Directions.left)
                    batch.draw(frame, position.position.x + 1, position.position.y, -1f, 1f)
            else batch.draw(frame, position.position.x, position.position.y, 1f, 1f)
            animation.time += deltaTime
        }
        batch.end()
    }

    fun resize(width: Int, height: Int) {
        state.viewport.update(width, height, true)
        screenViewport.update(width, height, true)
    }
}
