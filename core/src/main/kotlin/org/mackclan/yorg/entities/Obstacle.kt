package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Cover
import org.mackclan.yorg.components.CoverLevel
import org.mackclan.yorg.components.SpriteComponent

fun createObstacle(xPos: Float, yPos: Float, coverLevel: CoverLevel): Entity {
    val entity = Entity()
    entity.add(SpriteComponent(xPos, yPos, 1f, 1f, "graphics/block.png"))
    entity.add(Cover(coverLevel))
    return entity
}
