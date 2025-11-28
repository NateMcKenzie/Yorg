package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.AnimationComponent
import org.mackclan.yorg.components.GameState
import org.mackclan.yorg.components.Directions
import org.mackclan.yorg.components.Animations
import org.mackclan.yorg.components.Position
import org.mackclan.yorg.components.Velocity
import org.mackclan.yorg.components.Target

class Animation : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>
    private lateinit var projectiles: ImmutableArray<Entity>
    private lateinit var state: GameState

    private val animationComponentMap = ComponentMapper.getFor(AnimationComponent::class.java)
    private val positionMap = ComponentMapper.getFor(Position::class.java)
    private val velocityMap = ComponentMapper.getFor(Velocity::class.java)
    private val batch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val screenViewport by lazy { ScreenViewport() }

    override fun addedToEngine(engine: Engine) {
        entities =
            engine.getEntitiesFor(Family.all(AnimationComponent::class.java, Position::class.java).get())
        projectiles =
            engine.getEntitiesFor(Family.all(Position::class.java, Velocity::class.java, Target::class.java).get())
        //TODO: Target isn't really used yet, add a poof when it hits target later
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        batch.begin()

        //TODO: two loops in one system, maybe should be two systems?? (Probably yes once we add poof on hit)
        for (projectile in projectiles) {
            val position = positionMap.get(projectile)
            val velocity = velocityMap.get(projectile)
            position.position = position.position.add(velocity.velocity)
            val animation = animationComponentMap.get(projectile)
            animation.time += deltaTime
        }

        for (entity in entities) {
            val animation = animationComponentMap.get(entity)
            val position = positionMap.get(entity)
            val frame = animation.animations[animation.activeAnimation.ordinal].getKeyFrame(animation.time, true)

            // Default back to idle
            // TODO: Might be able to move more animation logic into here by reading state similarly
            if (animation.animations.get(animation.activeAnimation.ordinal).isAnimationFinished(animation.time)){
                animation.activeAnimation = Animations.idle
            }

            if (animation.facing == Directions.left)
                batch.draw(frame, position.position.x + 1, position.position.y, -1f, 1f)
            else
                batch.draw(frame, position.position.x, position.position.y, 1f, 1f)
            animation.time += deltaTime
        }
        batch.end()
    }

    fun resize(width: Int, height: Int) {
        state.viewport.update(width, height, true)
        screenViewport.update(width, height, true)
    }
}
