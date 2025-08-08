package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.UnitInfo

fun createUnit(xPos : Float, yPos : Float, range: Int, playerControlled: Boolean) : Entity{
    val entity = Entity()
    entity.add(Sprite(xPos, yPos, 1f, 1f, "graphics/mech.png"))
    entity.add(Controlled(range, playerControlled))
    entity.add(UnitInfo())
    return entity
}
