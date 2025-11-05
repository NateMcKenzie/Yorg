package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion

class AnimationComponent(fileName: String, var time: Float = 0f) : Component {
    val animations : List<Animation<TextureRegion>>
    var activeAnimation : Animations = Animations.idle
    init {
        val atlas: TextureAtlas = TextureAtlas(fileName)
        animations = listOf(
            Animation<TextureRegion>(0.1667f, atlas.findRegions("idle"), Animation.PlayMode.LOOP),
            Animation<TextureRegion>(0.0834f, atlas.findRegions("run"), Animation.PlayMode.LOOP),
        )
    }
}

enum class Animations {
    idle,
    run
}

