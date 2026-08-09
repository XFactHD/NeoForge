/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.opengl.GlBackend;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.GpuBackend;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.earlydisplay.render.backend.opengl.GlRenderer;
import net.neoforged.fml.loading.EarlyLoadingScreenController;
import org.lwjgl.glfw.GLFW;

public final class EarlyWindowHandoff {
    private static long reusedWindow;

    /**
     * Reuses the early display's native window where replacing it would cause a disruptive macOS window transition.
     * The early display temporarily retains its own renderer until the Blaze3D window surface is ready.
     */
    public static long tryTakeOverGlfwWindow(GpuBackend backend) {
        if (!MacosUtil.IS_MACOS || !(backend instanceof GlBackend) || reusedWindow != 0L) {
            return 0L;
        }

        EarlyLoadingScreenController earlyLoadingScreen = EarlyLoadingScreenController.current();
        if (!(earlyLoadingScreen instanceof DisplayWindow displayWindow)) {
            return 0L;
        }

        long window = displayWindow.getWindowHandle();
        displayWindow.handOverToMinecraft(() -> GlRenderer.setupBackend(window), false);
        reusedWindow = window;
        return window;
    }

    /** Completes the renderer handoff and restores the early display's native window state when it was not reused. */
    public static void completeWindowHandoff(Window window) {
        EarlyLoadingScreenController earlyLoadingScreen = EarlyLoadingScreenController.current();
        if (earlyLoadingScreen == null) {
            GLFW.glfwShowWindow(window.handle());
            return;
        }

        if (earlyLoadingScreen instanceof DisplayWindow displayWindow && isReusedGlfwWindow(window.handle())) {
            displayWindow.handOverToMinecraft(() -> new Blaze3DRenderBackend(window), false);
            return;
        }

        EarlyLoadingScreenController.WindowState state = earlyLoadingScreen.handOverToMinecraft(() -> new Blaze3DRenderBackend(window));
        restoreWindowState(window.handle(), state);
    }

    public static boolean isReusedGlfwWindow(long window) {
        return reusedWindow == window;
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
