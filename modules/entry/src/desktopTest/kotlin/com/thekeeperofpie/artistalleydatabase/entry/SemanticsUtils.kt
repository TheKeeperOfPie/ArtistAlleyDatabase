package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.platform.PlatformRootForTest
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher

fun isDisplayed() = SemanticsMatcher("is displayed") {
    checkIsDisplayed(it, false)
}

private val isTransparent by lazy {
    SemanticsNode::class.java.getDeclaredMethod("isTransparent\$ui").apply { isAccessible = true }
}

// Copied out of internal source since it's not exposed as a matcher
private fun checkIsDisplayed(node: SemanticsNode, assertIsFullyVisible: Boolean): Boolean {
    if (isTransparent.invoke(node) as Boolean) {
        return false
    }

    @Suppress("DEPRECATION")
    if (node.config.contains(SemanticsProperties.HideFromAccessibility) ||
        node.config.contains(SemanticsProperties.InvisibleToUser)
    ) {
        return false
    }

    fun isNotPlaced(node: LayoutInfo): Boolean {
        return !node.isPlaced
    }

    val layoutInfo = node.layoutInfo
    if (isNotPlaced(layoutInfo) || layoutInfo.findClosestParentNode(::isNotPlaced) != null) {
        return false
    }

    // check node doesn't clip unintentionally (e.g. row too small for content)
    val globalRect = node.boundsInWindow
    if (!node.isInScreenBounds(assertIsFullyVisible)) {
        return false
    }

    return (globalRect.width > 0f && globalRect.height > 0f)
}

private fun LayoutInfo.findClosestParentNode(
    selector: (LayoutInfo) -> Boolean,
): LayoutInfo? {
    var currentParent = this.parentInfo
    while (currentParent != null) {
        if (selector(currentParent)) {
            return currentParent
        } else {
            currentParent = currentParent.parentInfo
        }
    }

    return null
}

@OptIn(InternalComposeUiApi::class)
private fun SemanticsNode.isInScreenBounds(assertIsFullyVisible: Boolean): Boolean {
    val platformRootForTest = root as PlatformRootForTest
    val visibleBounds = platformRootForTest.visibleBounds

    // Window relative bounds of our node
    val nodeBoundsInWindow = clippedNodeBoundsInWindow()
    if (nodeBoundsInWindow.width == 0f || nodeBoundsInWindow.height == 0f) {
        return false
    }

    // Window relative bounds of our compose root view that are visible on the screen
    return if (assertIsFullyVisible) {
        // assertIsNotDisplayed only throws if the element is fully onscreen
        return nodeBoundsInWindow.top >= visibleBounds.top &&
                nodeBoundsInWindow.left >= visibleBounds.left &&
                nodeBoundsInWindow.right <= visibleBounds.right &&
                nodeBoundsInWindow.bottom <= visibleBounds.bottom
    } else {
        // assertIsDisplayed only throws if the element is fully offscreen
        !nodeBoundsInWindow.intersect(visibleBounds).isEmpty
    }
}

private fun SemanticsNode.clippedNodeBoundsInWindow() = boundsInRoot.translate(Offset(0f, 0f))
