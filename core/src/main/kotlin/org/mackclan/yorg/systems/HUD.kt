package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.mackclan.yorg.components.BoolCallback
import org.mackclan.yorg.components.FreshFlag
import org.mackclan.yorg.components.ShotInfo
import kotlin.random.Random

class HUD : EntitySystem() {

    private lateinit var entities: ImmutableArray<Entity>
    private val callbackMap = ComponentMapper.getFor(BoolCallback::class.java)
    private val infoMap = ComponentMapper.getFor(ShotInfo::class.java)
    private val freshMap = ComponentMapper.getFor(FreshFlag::class.java)

    private val screenViewport by lazy { ScreenViewport() }
    private val shapeRenderer by lazy { ShapeRenderer() }
    private val batch by lazy { SpriteBatch() }
    private val font by lazy { BitmapFont() }

    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(Family.all(ShotInfo::class.java).get())
    }

    override fun update(deltaTime: Float) {
        //TODO: Obviously the coordinate system here sucks
        if (entities.size() > 0) {
            //TODO: Should only  be one of these at a time, but currently that's not enforced
            val entity = entities.first()
            val freshFlag = freshMap.get(entity)
            if (Gdx.input.justTouched() && !freshFlag.fresh) {
                val touchPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
                screenViewport.unproject(touchPos)
                if (touchPos.x in 497f..607f && touchPos.y in 85f..119f) {
                    val roll = Random.nextFloat()
                    val hit = roll <= infoMap.get(entity).chance
                    callbackMap.get(entity).callback(hit)
                }
                //NOTE: We destroy component and early return for ANY click (click-off to cancel shot for now)
                engine.removeEntity(entity)
                return
            }

            freshFlag.fresh = false
            screenViewport.apply()

            shapeRenderer.projectionMatrix = screenViewport.camera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.BLACK
            shapeRenderer.rect(497f, 85f, 110f, 34f)
            shapeRenderer.end()

            batch.projectionMatrix = screenViewport.camera.combined
            batch.begin()
            font.draw(batch, "Chance: ${"%.1f".format(infoMap.get(entity).chance * 100)}%", 500f, 100f)
            font.draw(batch, "Damage: ${infoMap.get(entity).damage}", 500f, 116f)
            batch.end()
        }
    }

    fun resize(width: Int, height: Int) {
        screenViewport.update(width, height, true)
    }
}
