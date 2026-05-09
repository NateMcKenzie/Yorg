package org.mackclan.yorg.systems

import kotlin.math.max
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
import org.mackclan.yorg.components.Animations

class ProjectileAnimation : EntitySystem() {
    private lateinit var projectiles: ImmutableArray<Entity>
    private lateinit var state: GameState

    private val animationComponentMap = ComponentMapper.getFor(AnimationComponent::class.java)
    private val positionMap = ComponentMapper.getFor(Position::class.java)
    private val velocityMap = ComponentMapper.getFor(Velocity::class.java)
    private val targetMap = ComponentMapper.getFor(Target::class.java)
    private val batch by lazy { SpriteBatch() }
    private val screenViewport by lazy { ScreenViewport() }

    private val animations: List<Animation<TextureRegion>>

    init {
        var shotAtlas: TextureAtlas = TextureAtlas("animations/shot/shot.atlas")
        animations = listOf(
            Animation<TextureRegion>(0.100f, shotAtlas.findRegions("launch"), Animation.PlayMode.NORMAL),
            Animation<TextureRegion>(0.100f, shotAtlas.findRegions("travel"), Animation.PlayMode.LOOP),
            Animation<TextureRegion>(0.100f, shotAtlas.findRegions("collide"), Animation.PlayMode.NORMAL)
        )
    }

    override fun addedToEngine(engine: Engine) {
        projectiles =
                engine.getEntitiesFor(
                        Family.all(Position::class.java, Velocity::class.java, Target::class.java)
                                .get()
                )
        val gameState = engine.getEntitiesFor(Family.all(GameState::class.java).get()).first()
        state = gameState.components.first() as GameState
    }

    override fun update(deltaTime: Float) {
        state.viewport.apply()
        batch.projectionMatrix = state.viewport.camera.combined
        batch.begin()

        for (projectile in projectiles) {
            val animation = animationComponentMap.get(projectile)
            if (animation.time < 0f){
                animation.time += deltaTime
                continue
            }
            val position = positionMap.get(projectile)
            val velocity = velocityMap.get(projectile)
            val target = targetMap.get(projectile)
            val nextPos = position.position.cpy().add(velocity.direction.cpy().scl(velocity.speed))


            if (nextPos.dst(target.target) <= position.position.dst(target.target)) {
                position.position = nextPos.cpy()
            } else {
                position.position = target.target.cpy()
                velocity.speed = 0f
                animation.activeAnimation = Animations.collide
                animation.time = 0f
            }

            var animatedPos = position.position.cpy()

            val activeAnimation = animation.activeAnimation
            if (activeAnimation == Animations.launch){
                if(animations[activeAnimation.ordinal - 4].isAnimationFinished(animation.time)){
                    animation.activeAnimation = Animations.travel
                    animation.time = 0f
                    velocity.speed = 0.1f
                } else {
                    animatedPos.add(0.4f, 0.2f)
                }
            } else if(activeAnimation == Animations.travel){
                val xDrift = max(0f, 0.4f - animation.time * 0.1f)
                val yDrift = max(0f, 0.2f - animation.time * 0.1f)
                animatedPos.add(xDrift, yDrift)
            } else if (activeAnimation == Animations.collide && animations[activeAnimation.ordinal -4].isAnimationFinished(animation.time)){
                engine.removeEntity(projectile)
            }
            batch.draw(
                    animations[activeAnimation.ordinal - 4].getKeyFrame(animation.time, true),
                    animatedPos.x + 1,
                    animatedPos.y,
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
