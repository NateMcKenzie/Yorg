package org.mackclan.yorg.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.AnimationComponent
import org.mackclan.yorg.components.Position
import org.mackclan.yorg.components.Velocity
import org.mackclan.yorg.components.Target
import org.mackclan.yorg.components.Animations

fun createProjectile (position : Vector2, target: Vector2): Entity {
    val entity = Entity()
    val animation = AnimationComponent(-0.7f)
    animation.activeAnimation = Animations.launch
    entity.add(animation)
    entity.add(Position(position.x, position.y))
    entity.add(Target(target))
    entity.add(Velocity(0f, target.cpy().sub(position).nor()))
    return entity
}
