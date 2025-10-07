package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component

class BoolCallback(val callback: (Boolean) -> Unit) : Component
