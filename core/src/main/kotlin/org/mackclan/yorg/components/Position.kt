package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import org.mackclan.yorg.utils.bfsTile

class Position(posX: Float, posY: Float) : Component {
    var position: Vector2 = Vector2(posX, posY)
}

