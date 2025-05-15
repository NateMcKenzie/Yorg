package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class Sprite (posX: Float, posY: Float, width: Float, height: Float, fileName : String) : Component {
    val sprite : Sprite = Sprite(Texture(fileName))

    init {
        sprite.setBounds(posX, posY, width, height)
    }
}

