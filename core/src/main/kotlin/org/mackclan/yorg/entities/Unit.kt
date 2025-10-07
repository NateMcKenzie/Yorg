package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.SpriteComponent
import org.mackclan.yorg.components.UnitInfo

fun createUnit(xPos: Float, yPos: Float, walkRange: Int, playerControlled: Boolean): Entity {
    val entity = Entity()
    entity.add(SpriteComponent(xPos, yPos, 1f, 1f, if (playerControlled) "graphics/mech.png" else "graphics/mech2.png"))
    entity.add(Controlled(walkRange, playerControlled))
    entity.add(UnitInfo())
    return entity
}
