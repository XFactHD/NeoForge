/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.Window;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.EarlyLoadingScreenController;
import org.lwjgl.glfw.GLFW;

public final class EarlyWindowHandoff {
    /** Completes the renderer handoff and restores the early display's native window state. */
    public static void completeWindowHandoff(Window window) {
        EarlyLoadingScreenController earlyLoadingScreen = EarlyLoadingScreenController.current();
        if (earlyLoadingScreen == null) {
            GLFW.glfwShowWindow(window.handle());
            return;
        }

        boolean nativeFullscreen = MacosUtil.IS_MACOS
                && earlyLoadingScreen instanceof DisplayWindow displayWindow
                && MacosUtil.isInNativeFullscreen(displayWindow.getWindowHandle());
        EarlyLoadingScreenController.WindowState state = earlyLoadingScreen.handOverToMinecraft(() -> new Blaze3DRenderBackend(window));
        restoreWindowState(window, state, nativeFullscreen);
    }

    private static void restoreWindowState(Window window, EarlyLoadingScreenController.WindowState state, boolean nativeFullscreen) {
        long windowHandle = window.handle();
        if (state.posValid() && !state.minimized()) {
            GLFW.glfwSetWindowPos(windowHandle, state.x(), state.y());
        }

        if (state.maximized()) {
            // A maximized window reports its maximized size, not its restore size.
            GLFW.glfwMaximizeWindow(windowHandle);
        } else {
            GLFW.glfwSetWindowSize(windowHandle, state.width(), state.height());
        }

        if (state.minimized() && !window.isFullscreen()) {
            GLFW.glfwIconifyWindow(windowHandle);
            GLFW.glfwPollEvents();
        } else {
            GLFW.glfwShowWindow(windowHandle);
            if (nativeFullscreen) {
                MacosUtil.enterNativeFullscreen(window);
            }
        }
    }

    private EarlyWindowHandoff() {}
}
