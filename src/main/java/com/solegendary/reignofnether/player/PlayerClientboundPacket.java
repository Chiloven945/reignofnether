package com.solegendary.reignofnether.player;

import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class PlayerClientboundPacket {

    PlayerAction playerAction;
    String playerName;
    Long value;

    public static void enableRTSStatus(String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.ENABLE_RTS, playerName, 0L));
    }

    public static void disableRTSStatus(String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.DISABLE_RTS, playerName, 0L));
    }

    public static void defeat(String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.DEFEAT, playerName, 0L));
    }

    public static void victory(String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.VICTORY, playerName, 0L));
    }

    public static void resetRTS() {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.RESET_RTS, "", 0L));
    }

    public static void syncRtsGameTime(Long rtsGameTicks) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.SYNC_RTS_GAME_TIME, "", rtsGameTicks));
    }

    public static void lockRTS(String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.LOCK_RTS, playerName, 0L));
    }

    public static void unlockRTS(String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.UNLOCK_RTS, playerName, 0L));
    }

    // prevent one particular player from joining the match
    public static void disableStartRTS(String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.DISABLE_START_RTS, playerName, 0L));
    }
    public static void enableStartRTS(String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.ENABLE_START_RTS, playerName, 0L));
    }

    public static void syncMaxPopulation(long maxPopulation) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.SYNC_MAX_POPULATION, "", maxPopulation));
    }

    public static void setOrthoviewMinY(long orthoviewMinY) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.SET_MIN_ORTHOVIEW_Y, "", orthoviewMinY));
    }

    public static void syncNeutralAggro(boolean neutralAggro) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new PlayerClientboundPacket(PlayerAction.SYNC_NEUTRAL_AGGRO, "", neutralAggro ? 1L : 0L));
    }

    public PlayerClientboundPacket(PlayerAction playerAction, String playerName, Long value) {
        this.playerAction = playerAction;
        this.playerName = playerName;
        this.value = value;
    }

    public PlayerClientboundPacket(FriendlyByteBuf buffer) {
        this.playerAction = buffer.readEnum(PlayerAction.class);
        this.playerName = buffer.readUtf();
        this.value = buffer.readLong();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.playerAction);
        buffer.writeUtf(this.playerName);
        buffer.writeLong(this.value);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> {
                        switch (playerAction) {
                            case DEFEAT -> PlayerClientEvents.defeat(playerName);
                            case VICTORY -> PlayerClientEvents.victory(playerName);
                            case DISABLE_RTS -> PlayerClientEvents.disableRTS(playerName);
                            case ENABLE_RTS -> PlayerClientEvents.enableRTS(playerName);
                            case RESET_RTS -> PlayerClientEvents.resetRTS();
                            case SYNC_RTS_GAME_TIME -> PlayerClientEvents.syncRtsGameTime(value);
                            case LOCK_RTS -> PlayerClientEvents.setRTSLock(true);
                            case UNLOCK_RTS -> PlayerClientEvents.setRTSLock(false);
                            case ENABLE_START_RTS -> PlayerClientEvents.setCanStartRTS(true);
                            case DISABLE_START_RTS -> PlayerClientEvents.setCanStartRTS(false);
                            case SYNC_MAX_POPULATION -> UnitClientEvents.setMaxPopulation(Math.toIntExact(value));
                            case SET_MIN_ORTHOVIEW_Y -> OrthoviewClientEvents.setMinOrthoviewY(value);
                            case SYNC_NEUTRAL_AGGRO -> UnitClientEvents.neutralAggro = value == 1L;
                        }
                        success.set(true);
                    });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
