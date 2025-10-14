package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Cover
import org.mackclan.yorg.components.CoverLevel
import org.mackclan.yorg.components.SpriteComponent

fun createObstacle(xPos: Float, yPos: Float, coverLevel: CoverLevel): Entity {
    val entity = Entity()
    val image = if (coverLevel == CoverLevel.Low) "graphics/block.png" else "graphics/block_high.png"
    entity.add(SpriteComponent(xPos, yPos, image))
    entity.add(Cover(coverLevel))
    return entity
}
