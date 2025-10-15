package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.TextureAtlas

class AnimationComponent(fileName: String, var time : Float = 0f) : Component {
    val atlas : TextureAtlas = TextureAtlas(fileName)
    val animation: Animation<TextureRegion> = Animation<TextureRegion>(0.1667f, atlas.findRegions("idle"), Animation.PlayMode.LOOP)
}

