package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.AnimationComponent
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.Position
import org.mackclan.yorg.components.Target
import org.mackclan.yorg.components.Velocity

class ProjectileAnimation : EntitySystem() {
    private lateinit var projectiles: ImmutableArray<Entity>
    private lateinit var state: GameState

    private val animationComponentMap = ComponentMapper.getFor(AnimationComponent::class.java)
    private val positionMap = ComponentMapper.getFor(Position::class.java)
    private val velocityMap = ComponentMapper.getFor(Velocity::class.java)
    private val batch by lazy { SpriteBatch() }
    private val screenViewport by lazy { ScreenViewport() }

    private val animations: List<Animation<TextureRegion>>

    init {
        var shotAtlas: TextureAtlas = TextureAtlas("animations/shot/shot.atlas")
        animations = listOf(Animation<TextureRegion>(0.1667f, shotAtlas.findRegions("shot"), Animation.PlayMode.LOOP))
    }

    override fun addedToEngine(engine: Engine) {
        projectiles =
                engine.getEntitiesFor(
                        Family.all(Position::class.java, Velocity::class.java, Target::class.java)
                                .get()
                )
        // TODO: Target isn't really used yet, add a poof when it hits target later
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        batch.begin()

        for (projectile in projectiles) {
            val position = positionMap.get(projectile)
            val velocity = velocityMap.get(projectile)
            position.position.add(velocity.direction.scl(velocity.speed))
            val animation = animationComponentMap.get(projectile)
            val activeAnimation = animations[0]
            batch.draw(
                    activeAnimation.getKeyFrame(animation.time, true),
                    position.position.x + 1,
                    position.position.y,
                    -1f,
                    1f
            )
            animation.time += deltaTime
        }

        batch.end()
    }

    fun resize(width: Int, height: Int) {
        state.viewport.update(width, height, true)
        screenViewport.update(width, height, true)
    }
}
