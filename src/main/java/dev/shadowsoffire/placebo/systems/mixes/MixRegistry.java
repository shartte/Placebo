package dev.shadowsoffire.placebo.systems.mixes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.PlaceboClient;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import dev.shadowsoffire.placebo.systems.mixes.JsonMix.Type;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class MixRegistry extends DynamicRegistry<JsonMix<?>> {

    public static final MixRegistry INSTANCE = new MixRegistry();

    public MixRegistry() {
        super(Placebo.LOGGER, "brewing_mixes", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Placebo.loc("mix"), JsonMix.CODEC);
    }

    @Override
    protected void beginReload() {
        removeAll(resolveBrewing());
        super.beginReload();
    }

    @Override
    protected void onReload() {
        addAll(resolveBrewing());
        super.onReload();
    }

    /**
     * Attempts to resolve the {@link PotionBrewing} instance from the given global context.
     * <p>
     * This is nullable because it fails to resolve during world creation in singleplayer, since an instance has not been created yet.
     */
    @Nullable
    private static PotionBrewing resolveBrewing() {
        if (FMLEnvironment.dist.isClient() && EffectiveSide.get().isClient()) {
            return PlaceboClient.getBrewingRegistry();
        }
        return ServerLifecycleHooks.getCurrentServer() == null ? null : ServerLifecycleHooks.getCurrentServer().potionBrewing();
    }

    @SuppressWarnings("unchecked")
    private static List<PotionBrewing.Mix<?>> getMixList(PotionBrewing brewing, Type type) {
        return (List<PotionBrewing.Mix<?>>) (Object) switch (type) {
            case POTION -> brewing.potionMixes;
            case CONTAINER -> brewing.containerMixes;
        };
    }

    private static void makeMutable(PotionBrewing brewing) {
        brewing.containerMixes = new ArrayList<>(brewing.containerMixes);
        brewing.potionMixes = new ArrayList<>(brewing.potionMixes);
    }

    private void removeAll(@Nullable PotionBrewing brewing) {
        if (brewing != null) {
            makeMutable(brewing);
            this.getValues().forEach(mix -> {
                getMixList(brewing, mix.type()).remove(mix.mix());
            });
        }
    }

    private void addAll(@Nullable PotionBrewing brewing) {
        if (brewing != null) {
            makeMutable(brewing);
            this.getValues().forEach(mix -> {
                getMixList(brewing, mix.type()).add(mix.mix());
            });
        }
    }

}
