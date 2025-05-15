package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.viewport.FitViewport

class Viewport : Component{
    val viewport by lazy { FitViewport(20f, 20f) }
}
