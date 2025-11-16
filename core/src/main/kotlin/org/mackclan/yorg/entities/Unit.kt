package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.AnimatablePosition
import org.mackclan.yorg.components.AnimationComponent
import org.mackclan.yorg.components.Controlled
import org.mackclan.yorg.components.UnitInfo
import org.mackclan.yorg.components.Position
import org.mackclan.yorg.components.Velocity
import org.mackclan.yorg.components.TravelPath
import kotlin.random.Random

fun createUnit(xPos: Float, yPos: Float, walkRange: Int, playerControlled: Boolean): Entity {
    val entity = Entity()
    entity.add(AnimationComponent("animations/robot1/robot1.atlas", Random.nextFloat()))
    entity.add(TravelPath())
    entity.add(Velocity())
    entity.add(Position(xPos, yPos))
    entity.add(Controlled(walkRange, playerControlled))
    entity.add(UnitInfo())
    return entity
}
