package dev.shadowsoffire.placebo.systems.gear;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;

public class GearSetRegistry extends WeightedDynamicRegistry<GearSet> {

    public static final GearSetRegistry INSTANCE = new GearSetRegistry();

    public GearSetRegistry() {
        super(Placebo.LOGGER, "gear_sets", false, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Placebo.loc("gear_set"), GearSet.CODEC);
    }

}
