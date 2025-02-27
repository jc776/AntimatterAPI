package muramasa.antimatter.tool.behaviour;

import muramasa.antimatter.behaviour.IItemUse;
import muramasa.antimatter.tool.IAntimatterTool;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

public class BehaviourWaterlogToggle implements IItemUse<IAntimatterTool> {

    public static final BehaviourWaterlogToggle INSTANCE = new BehaviourWaterlogToggle();

    @Override
    public String getId() {
        return "waterlog_toggle";
    }

    @Override
    public ActionResultType onItemUse(IAntimatterTool instance, ItemUseContext c) {
        BlockState state = c.getWorld().getBlockState(c.getPos());
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            if (state.get(BlockStateProperties.WATERLOGGED)) {
                c.getWorld().setBlockState(c.getPos(), state.with(BlockStateProperties.WATERLOGGED, false), 11);
                c.getWorld().playSound(c.getPlayer(), c.getPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                c.getItem().damageItem(instance.getType().getUseDurability(), c.getPlayer(), (p) -> p.sendBreakAnimation(c.getHand()));
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }
}
