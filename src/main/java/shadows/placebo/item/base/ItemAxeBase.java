package shadows.placebo.item.base;

import net.minecraft.item.ItemAxe;
import shadows.placebo.client.IHasModel;
import shadows.placebo.registry.RegistryInformation;

public class ItemAxeBase extends ItemAxe implements IHasModel {

	public ItemAxeBase(String name, RegistryInformation info, ToolMaterial mat) {
		super(mat, 8, -3);
		setRegistryName(name);
		setUnlocalizedName(info.getID() + "." + name);
		setCreativeTab(info.getDefaultTab());
		info.getItemList().add(this);
	}
}
