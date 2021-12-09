package shadows.placebo.container;

import java.util.function.Predicate;

import com.google.common.base.Predicates;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import shadows.placebo.cap.InternalItemHandler;

public class FilteredSlot extends SlotItemHandler {

	protected final Predicate<ItemStack> filter;
	protected final int index;

	public FilteredSlot(InternalItemHandler handler, int index, int x, int y, Predicate<ItemStack> filter) {
		super(handler, index, x, y);
		this.filter = filter;
		this.index = index;
	}

	public FilteredSlot(InternalItemHandler handler, int index, int x, int y) {
		this(handler, index, x, y, Predicates.alwaysTrue());
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return this.filter.test(stack);
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return !((InternalItemHandler) this.getItemHandler()).extractItemInternal(index, 1, true).isEmpty();
	}

	@Override
	public ItemStack remove(int amount) {
		return ((InternalItemHandler) this.getItemHandler()).extractItemInternal(this.index, amount, false);
	}

}