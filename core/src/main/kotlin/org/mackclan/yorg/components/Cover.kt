package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component

class Cover(val level: CoverLevel) : Component

enum class CoverLevel {
    None,
    Low,
    High
}
