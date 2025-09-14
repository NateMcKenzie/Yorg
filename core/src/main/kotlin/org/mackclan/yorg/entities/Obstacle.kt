package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.UnitInfo

fun createObstacle(xPos : Float, yPos : Float) : Entity{
    val entity = Entity()
    entity.add(Sprite(xPos, yPos, 1f, 1f, "graphics/block.png"))
    return entity
}
