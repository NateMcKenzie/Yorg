package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2

class AnimatablePosition(posX : Float, posY : Float) : Component {
    var velocity : Vector2 = Vector2()
    var target : Vector2 = Vector2()
    var position : Vector2 = Vector2(posX, posY)
    var speed : Float = 5f
}

