package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import org.mackclan.yorg.utils.bfsTile

class AnimatablePosition(posX: Float, posY: Float) : Component {
    var velocity: Vector2 = Vector2()
    val path: MutableList<bfsTile> = mutableListOf<bfsTile>()
    var position: Vector2 = Vector2(posX, posY)
    var speed: Float = 5f
}

