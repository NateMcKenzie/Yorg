package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.GameState

fun createGameState() : Entity{
    val entity = Entity()
    entity.add(GameState())
    return entity
}
