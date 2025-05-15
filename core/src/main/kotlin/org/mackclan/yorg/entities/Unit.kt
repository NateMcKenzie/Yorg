package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Movable
import org.mackclan.yorg.components.Sprite

fun createUnit() : Entity{
    val entity = Entity()
    entity.add(Sprite(0f, 0f, 1f, 1f, "graphics/mech.png"))
    entity.add(Movable(5))
    return entity
}
