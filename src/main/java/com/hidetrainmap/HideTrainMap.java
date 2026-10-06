package com.hidetrainmap;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(HideTrainMap.MOD_ID)
public class HideTrainMap {
    public static final String MOD_ID = "hidetrainmap";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TRAIN_MAP_PACKAGE = "com.simibubi.create.compat.trainmap.";

    public HideTrainMap(IEventBus modEventBus) {
        ForbiddenModsCheck.run();
        verifyPatches();
        modEventBus.addListener(HideTrainMap::registerPayloads);
    }

    /**
     * Loads every patched Create class right away. A missing injection target makes
     * Mixin fail during this load ({@code defaultRequire: 1}), and a missing class or
     * a patch that was not applied is reported here, so the game never starts
     * with the train map silently back.
     */
    private static void verifyPatches() {
        List<String> targets = new ArrayList<>();
        targets.add("TrainMapSync");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            targets.add("TrainMapManager");
            if (ModList.get().isLoaded("xaeroworldmap")) {
                targets.add("XaeroTrainMap");
            }
        }

        for (String name : targets) {
            String className = TRAIN_MAP_PACKAGE + name;
            Class<?> target;
            try {
                target = Class.forName(className, false, HideTrainMap.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(failure(className + " no longer exists"), e);
            } catch (Throwable t) {
                throw new IllegalStateException(failure("could not patch " + className + ": " + t), t);
            }
            if (!PatchedMarker.class.isAssignableFrom(target)) {
                throw new IllegalStateException(failure("the patch was not applied to " + className));
            }
        }
        LOGGER.info("Create train map disabled ({} classes patched: {})", targets.size(), targets);
    }

    private static String failure(String reason) {
        return "[Hide Train Map] Incompatible Create version, " + reason
                + ". Update Hide Train Map or downgrade Create; refusing to start rather than showing the train map.";
    }

    /**
     * A non-optional payload: NeoForge refuses the connection when the other side
     * does not have this mod (same channel and version on both ends).
     */
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(PresencePayload.TYPE, PresencePayload.STREAM_CODEC, (payload, context) -> {});
    }

    public record PresencePayload() implements CustomPacketPayload {
        public static final PresencePayload INSTANCE = new PresencePayload();
        public static final Type<PresencePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "presence"));
        public static final StreamCodec<ByteBuf, PresencePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
