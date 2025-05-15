package org.mackclan.yorg.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.FitViewport
import org.mackclan.yorg.components.Selector
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.Viewport
import kotlin.math.floor

class Clicks : EntitySystem() {
    private lateinit var entities : ImmutableArray<Entity>

    private val selectorMap = ComponentMapper.getFor(Selector::class.java)
    private val spriteMap = ComponentMapper.getFor(Sprite::class.java)

    private lateinit var viewport : FitViewport

    override fun addedToEngine(engine: Engine){
        entities = engine.getEntitiesFor(Family.all(Sprite::class.java, Selector::class.java).get())
        val camera = engine.getEntitiesFor(Family.all(Viewport::class.java).get()).first()
        viewport = (camera.components.first() as Viewport).viewport
    }

    override fun update(deltaTime : Float){
        if (Gdx.input.justTouched()){
            val touchPos = Vector2()
            touchPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()) // This is window coords (in pixels y increases from top down)
            viewport.unproject(touchPos)
            touchPos.set(floor(touchPos.x), floor(touchPos.y))

            var unitClicked = false
            var selected = Array<Entity>()

            for (entity in entities){
                val sprite = spriteMap.get(entity).sprite
                val selector = selectorMap.get(entity)
                if (sprite.x == touchPos.x && sprite.y == touchPos.y){
                    selector.selected = !selector.selected
                    unitClicked = true
                }
                if (selector.selected) selected.add(entity)
            }
            if (!unitClicked){
                for (entity in selected){
                    val sprite = spriteMap.get(entity).sprite
                    sprite.x = touchPos.x
                    sprite.y = touchPos.y
                }
            }
        }
    }
}
