package dev.shadowsoffire.placebo.codec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.resources.ResourceLocation;

/**
 * Util class for codecs.
 */
public class PlaceboCodecs {

    /**
     * Creates a map-backed codec with a default codec to use as a fallback.
     *
     * @param <V>          The type being de/serialized.
     * @param name         The name of the type being de/serialized for error logging.
     * @param reg          The codec map.
     * @param defaultCodec The default codec to use if the deserialized object has no type field.
     * @return A codec backed by the provided map, that will fallback if necessary.
     */
    public static <T extends CodecProvider<T>> Codec<T> mapBackedDefaulted(String name, BiMap<ResourceLocation, Codec<? extends T>> reg, Codec<? extends T> defaultCodec) {
        return new MapBackedCodec<>(name, reg, () -> defaultCodec);
    }

    /**
     * Creates a map-backed codec. Deserialized objects must have a 'type' field declaring the target codec name.
     *
     * @param <V>  The type being de/serialized.
     * @param name The name of the type being de/serialized for error logging.
     * @param reg  The codec map.
     * @return A codec backed by the provided map.
     */
    public static <T extends CodecProvider<? super T>> Codec<T> mapBacked(String name, BiMap<ResourceLocation, Codec<? extends T>> reg) {
        return new MapBackedCodec<>(name, reg);
    }

    /**
     * Converts a codec into a set codec.
     */
    public static <T> Codec<Set<T>> setOf(Codec<T> elementCodec) {
        return setFromList(elementCodec.listOf());
    }

    /**
     * Converts a list codec into a set codec.
     */
    public static <T> Codec<Set<T>> setFromList(Codec<List<T>> listCodec) {
        return listCodec.<Set<T>>xmap(HashSet::new, ArrayList::new);
    }

    /**
     * Creates an enum codec using the lowercase name of the enum values as the keys.
     */
    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> clazz) {
        return stringResolverCodec(e -> e.name().toLowerCase(Locale.ROOT), name -> Enum.valueOf(clazz, name.toUpperCase(Locale.ROOT)));
    }

    public static <E> Codec<E> stringResolverCodec(Function<E, String> p_184406_, Function<String, E> p_184407_) {
        return Codec.STRING.flatXmap((p_184404_) -> {
            return Optional.ofNullable(p_184407_.apply(p_184404_)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    return "Unknown element name:" + p_184404_;
                });
            });
        }, (p_184401_) -> {
            return Optional.ofNullable(p_184406_.apply(p_184401_)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    return "Element with unknown name: " + p_184401_;
                });
            });
        });
    }

}
