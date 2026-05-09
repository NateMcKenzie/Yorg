package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component

class AnimationComponent(var time: Float = 0f) : Component {
    var activeAnimation : Animations = Animations.idle
    var facing : Directions = Directions.right
}

enum class Animations {
    idle,
    run,
    turn_to_run,
    fire,
    launch,
    travel,
    collide,
}

enum class Directions {
    up,
    right,
    down,
    left
}
