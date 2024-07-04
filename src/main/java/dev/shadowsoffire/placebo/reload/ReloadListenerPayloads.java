package dev.shadowsoffire.placebo.reload;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry.SyncManagement;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@ApiStatus.Internal
public class ReloadListenerPayloads {

    public static record Start(String path) implements CustomPacketPayload {

        public static final Type<Start> TYPE = new Type<>(Placebo.loc("reload_sync_start"));

        public static final StreamCodec<FriendlyByteBuf, Start> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Start::path,
            Start::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<Start> {

            @Override
            public Type<Start> getType() {
                return TYPE;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Start> getCodec() {
                return CODEC;
            }

            @Override
            public void handle(Start msg, IPayloadContext ctx) {
                SyncManagement.initSync(msg.path);
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

            @Override
            public String getVersion() {
                return "1";
            }
        }
    }

    public static record Content<V extends CodecProvider<? super V>>(String path, ResourceLocation key, V item) implements CustomPacketPayload {

        public static final Type<Content<?>> TYPE = new Type<>(Placebo.loc("reload_sync_content"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Content<?>> CODEC = StreamCodec.of(Content::write, Content::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public static void write(RegistryFriendlyByteBuf buf, Content payload) {
            buf.writeUtf(payload.path, 50);
            buf.writeResourceLocation(payload.key);
            SyncManagement.writeItem(payload.path, payload.item, buf);
        }

        public static <V extends CodecProvider<? super V>> Content<V> read(RegistryFriendlyByteBuf buf) {
            String path = buf.readUtf(50);
            ResourceLocation key = buf.readResourceLocation();

            try {
                V value = SyncManagement.readItem(path, buf);
                return new Content<>(path, key, value);
            }
            catch (Exception ex) {
                Placebo.LOGGER.error("Failure when deserializing a dynamic registry object via network: Registry: {}, Object ID: {}", path, key);
                throw ex;
            }
        }

        public static class Provider<V extends CodecProvider<? super V>> implements PayloadProvider<Content<?>> {

            @Override
            public Type<Content<?>> getType() {
                return TYPE;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Content<?>> getCodec() {
                return CODEC;
            }

            @Override
            public void handle(Content<?> msg, IPayloadContext ctx) {
                SyncManagement.acceptItem(msg.path, msg.key, msg.item);
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

            @Override
            public String getVersion() {
                return "1";
            }
        }
    }

    public static record End(String path) implements CustomPacketPayload {

        public static final Type<End> TYPE = new Type<>(Placebo.loc("reload_sync_end"));

        public static final StreamCodec<FriendlyByteBuf, End> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, End::path,
            End::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<End> {

            @Override
            public Type<End> getType() {
                return TYPE;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, End> getCodec() {
                return CODEC;
            }

            @Override
            public void handle(End msg, IPayloadContext ctx) {
                SyncManagement.endSync(msg.path);
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

            @Override
            public String getVersion() {
                return "1";
            }
        }
    }
}
