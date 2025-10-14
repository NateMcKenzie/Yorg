package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class SpriteComponent(posX: Float, posY: Float, fileName: String) : Component {
    val sprite: Sprite = Sprite(Texture(fileName))

    init {
        sprite.setBounds(posX, posY, 1f, 1f)
    }
}

