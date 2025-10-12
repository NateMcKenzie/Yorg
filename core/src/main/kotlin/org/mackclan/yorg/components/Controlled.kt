package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2

class Controlled(val walkRange: Int, var playerControlled: Boolean) : Component {
    var selected: Boolean = false
    var desiredMove: Vector2? = null
    var actionPoints: Int = 2
}
