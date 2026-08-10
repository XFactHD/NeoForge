/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.platform.Window;
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

        EarlyLoadingScreenController.WindowState state = earlyLoadingScreen.handOverToMinecraft(() -> new Blaze3DRenderBackend(window));
        restoreWindowState(window.handle(), state);
    }

    private static void restoreWindowState(long window, EarlyLoadingScreenController.WindowState state) {
        if (state.posValid()) {
            GLFW.glfwSetWindowPos(window, state.x(), state.y());
        }

        if (state.maximized()) {
            // A maximized window reports its maximized size, not its restore size.
            GLFW.glfwMaximizeWindow(window);
        } else {
            GLFW.glfwSetWindowSize(window, state.width(), state.height());
        }

        if (state.minimized()) {
            GLFW.glfwIconifyWindow(window);
            GLFW.glfwPollEvents();
        } else {
            GLFW.glfwShowWindow(window);
        }
    }

    private EarlyWindowHandoff() {}
}
