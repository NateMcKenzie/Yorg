package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.Sprite
import org.mackclan.yorg.components.UnitInfo
import org.mackclan.yorg.components.Cover
import org.mackclan.yorg.components.CoverLevel

fun createObstacle(xPos : Float, yPos : Float, coverLevel : CoverLevel) : Entity{
    val entity = Entity()
    entity.add(Sprite(xPos, yPos, 1f, 1f, "graphics/block.png"))
    entity.add(Cover(coverLevel))
    return entity
}
