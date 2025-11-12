package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion

class AnimationComponent(fileName: String, var time: Float = 0f) : Component {
    val animations : List<Animation<TextureRegion>>
    var activeAnimation : Animations = Animations.idle
    var facing : Directions = Directions.right
    init {
        val atlas: TextureAtlas = TextureAtlas(fileName)
        animations = listOf(
            Animation<TextureRegion>(0.1667f, atlas.findRegions("idle"), Animation.PlayMode.LOOP),
            Animation<TextureRegion>(0.0834f, atlas.findRegions("run"), Animation.PlayMode.LOOP),
            Animation<TextureRegion>(0.0417f, atlas.findRegions("turn_to_run"), Animation.PlayMode.NORMAL),
        )
    }
}

enum class Animations {
    idle,
    run,
    turn_to_run,
}

enum class Directions {
    up,
    right,
    down,
    left
}
