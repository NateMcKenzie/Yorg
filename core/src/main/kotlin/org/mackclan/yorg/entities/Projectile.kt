package org.mackclan.yorg.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.AnimationComponent

fun createProjectile (position : Vector2, direction: Vector2): Entity {
    val entity = Entity()
    entity.add(AnimationComponent("animations/shot/shot.atlas", 0f))
    // Position, velocity, target etc. here
    return entity
}
