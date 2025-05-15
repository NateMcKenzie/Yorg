package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.Viewport

fun createCamera() : Entity{
    val entity = Entity()
    entity.add(Viewport())
    return entity
}
