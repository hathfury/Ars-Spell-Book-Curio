package com.example.an_addon;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class Networking {
    public static SimpleChannel INSTANCE;

    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(ExampleANAddon.MODID, "network"), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(nextID(),
                PacketCurioBookQuickCast.class,
                PacketCurioBookQuickCast::toBytes,
                PacketCurioBookQuickCast::new,
                PacketCurioBookQuickCast::handle);
    }

    public static void sendToServer(Object msg) {
        INSTANCE.sendToServer(msg);
    }
}