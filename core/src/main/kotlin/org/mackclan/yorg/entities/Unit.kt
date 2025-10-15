package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.UnitInfo
import org.mackclan.yorg.components.AnimationComponent
import org.mackclan.yorg.components.AnimatablePosition
import kotlin.random.Random

fun createUnit(xPos: Float, yPos: Float, walkRange: Int, playerControlled: Boolean): Entity {
    val entity = Entity()
    //entity.add(SpriteComponent(xPos, yPos, if (playerControlled) "graphics/mech.png" else "graphics/mech2.png"))
    entity.add(AnimationComponent("animations/robot1/idle.atlas", Random.nextFloat()))
    entity.add(AnimatablePosition(xPos, yPos))
    entity.add(Controlled(walkRange, playerControlled))
    entity.add(UnitInfo())
    return entity
}
