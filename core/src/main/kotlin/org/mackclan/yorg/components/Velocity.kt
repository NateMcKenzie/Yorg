package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import org.mackclan.yorg.utils.bfsTile

class Velocity(var speed: Float = 5f, var velocity: Vector2 = Vector2()) : Component

