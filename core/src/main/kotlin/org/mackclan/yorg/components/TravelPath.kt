package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import org.mackclan.yorg.utils.bfsTile

class TravelPath(val path: MutableList<bfsTile> = mutableListOf<bfsTile>()) : Component

