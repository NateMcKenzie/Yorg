package org.mackclan.yorg.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.AnimationComponent
import org.mackclan.yorg.components.Position
import org.mackclan.yorg.components.Velocity
import org.mackclan.yorg.components.Target

fun createProjectile (position : Vector2, target: Vector2): Entity {
    val entity = Entity()
    entity.add(AnimationComponent("animations/shot/shot.atlas", 0f))
    entity.add(Position(position.x, position.y))
    entity.add(Target(target))
    entity.add(Velocity(10f, position.sub(target).nor()))
    return entity
}
