/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a chat message is received on the client.
 * This can be used for filtering and detecting messages with specific words or phrases, and suppressing them.
 *
 * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * If the event is cancelled, the message is not displayed in the chat message window.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 *
 * @see ChatType
 */
public class ClientChatReceivedEvent extends Event implements ICancellableEvent {
    private Component message;
    @Nullable
    private final ChatType.Bound boundChatType;
    private final UUID sender;

    @ApiStatus.Internal
    public ClientChatReceivedEvent(@Nullable ChatType.Bound boundChatType, Component message, UUID sender) {
        this.boundChatType = boundChatType;
        this.message = message;
        this.sender = sender;
    }

    /**
     * {@return the message that will be displayed in the chat message window, if the event is not cancelled}
     */
    public Component getMessage() {
        return message;
    }

    /**
     * Sets the new message to be displayed in the chat message window, if the event is not cancelled.
     *
     * @param message the new message to be displayed
     */
    public void setMessage(Component message) {
        this.message = message;
    }

    /**
     * {@return the bound chat type of the chat message}.
     * This contains the chat type, display name of the sender, and nullable target name depending on the chat type.
     * <p>
     * This may be {@code null} when the message doesn't have a specific source (i.e. for system messages).
     */
    @Nullable
    public ChatType.Bound getBoundChatType() {
        return this.boundChatType;
    }

    /**
     * {@return the message sender}.
     * This will be {@link Util#NIL_UUID} if the message is a system message.
     */
    public UUID getSender() {
        return this.sender;
    }

    /**
     * {@return {@code true} if the message was sent by the system, {@code false} otherwise}
     */
    public boolean isSystem() {
        return this.sender.equals(Util.NIL_UUID);
    }

    /**
     * Fired when a player chat message is received on the client.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
     * If the event is cancelled, the message is not displayed in the chat message window.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @see ChatType
     */
    public static class Player extends ClientChatReceivedEvent {
        private final PlayerChatMessage playerChatMessage;

        @ApiStatus.Internal
        public Player(ChatType.Bound boundChatType, Component message, PlayerChatMessage playerChatMessage, UUID sender) {
            super(boundChatType, message, sender);
            this.playerChatMessage = playerChatMessage;
        }

        /**
         * {@return the full player chat message}.
         * This contains the sender UUID, various signing data, and the optional unsigned contents.
         */
        public PlayerChatMessage getPlayerChatMessage() {
            return this.playerChatMessage;
        }
    }

    /**
     * Fired when a system chat message is received on the client.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
     * If the event is cancelled, the message is not displayed in the chat message window or in the overlay.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class System extends ClientChatReceivedEvent {
        private final boolean overlay;

        @ApiStatus.Internal
        public System(Component message, boolean overlay) {
            super(null, message, Util.NIL_UUID);
            this.overlay = overlay;
        }

        /**
         * {@return whether the message goes to the overlay}
         */
        public boolean isOverlay() {
            return this.overlay;
        }
    }
}
