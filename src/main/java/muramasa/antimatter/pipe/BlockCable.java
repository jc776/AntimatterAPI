
package muramasa.antimatter.pipe;

import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.material.Material;
import muramasa.antimatter.registration.IColorHandler;
import muramasa.antimatter.registration.IItemBlockProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import tesseract.TesseractAPI;
import tesseract.api.GraphWrapper;
import tesseract.api.electric.IElectricCable;
import tesseract.util.Dir;

import javax.annotation.Nullable;

public class BlockCable extends BlockPipe implements IItemBlockProvider, IColorHandler, IElectricCable {

    // TODO: Finish cables
    protected boolean insulated;
    protected int loss, lossInsulated;
    protected int amps;
    protected Tier tier;
    protected GraphWrapper electric;

    public BlockCable(String domain, Material material, PipeSize size, boolean insulated, int loss, int lossInsulated, int amps, Tier tier) {
        super(domain, insulated ? PipeType.CABLE : PipeType.WIRE, material, size);
        this.insulated = insulated;
        this.loss = loss;
        this.lossInsulated = lossInsulated;
        this.tier = tier;
        this.amps = amps;
    }

    @Override
    public long getVoltage() {
        return tier.getVoltage();
    }

    @Override
    public int getLoss() {
        return insulated ? lossInsulated : loss;
    }

    @Override
    public int getAmps() {
        return amps;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public boolean connects(Dir direction) {
        return true;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (world.isRemote()) return; // Avoid client-side
        electric = TesseractAPI.asElectricCable(world.getDimension().getType().getId(), pos.toLong(), this);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        if (electric != null) electric.remove();
        return true;
    }

    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        if (electric != null) electric.remove();
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public boolean isFireSource(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return true;
    }

    //    @Nullable
//    @Override
//    public String getHarvestTool(BlockState state) {
//        return "wire_cutter";
//    }

//    @Override
//    public String getDisplayName(ItemStack stack) {
//        boolean ins = stack.getMetadata() > 7;
//        PipeSize size = PipeSize.VALUES[ins ? stack.getMetadata() - 8 : stack.getMetadata()];
//        return size.getCableThickness() + "x " + material.getDisplayName() + (ins ? " Cable" : " Wire");
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
//        boolean ins = stack.getMetadata() > 7;
//        PipeSize size = PipeSize.VALUES[ins ? stack.getMetadata() - 8 : stack.getMetadata()];
//        tooltip.add("Max Voltage: " + TextFormatting.GREEN + getVoltage() + " (" + getTier().getId().toUpperCase(Locale.ENGLISH) + ")");
//        tooltip.add("Max Amperage: " + TextFormatting.YELLOW + getAmps(size));
//        tooltip.add("Loss/Meter/Ampere: " + TextFormatting.RED + getLoss(ins) + TextFormatting.GRAY + " EU-Volt");
//    }

    @Override
    public int getBlockColor(BlockState state, @Nullable IBlockReader world, @Nullable BlockPos pos, int i) {
        if (!(state.getBlock() instanceof BlockCable) && world == null || pos == null) return -1;
        return insulated ? i == 2 ? getRGB() : -1 : i == 0 || i == 2 ? getRGB() : -1;
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        return insulated ? i == 1 ? getRGB() : -1 : getRGB();
    }

    public static class BlockCableBuilder extends BlockPipeBuilder {

        protected int loss, lossInsulated;
        protected Tier tier;
        protected int[] amps;
        protected boolean buildUninsulated = true, buildInsulated = true;

        public BlockCableBuilder(String domain, Material material, int loss, int lossInsulated, Tier tier, PipeSize[] sizes) {
            super(domain, material, sizes);
            this.loss = loss;
            this.lossInsulated = lossInsulated;
            this.tier = tier;
        }

        public BlockCableBuilder(String domain, Material material, int loss, int lossInsulated, Tier tier) {
            this(domain, material, loss, lossInsulated, tier, PipeSize.VALUES);
        }

        public BlockCableBuilder amps(int baseAmps) {
            this.amps = new int[]{baseAmps, baseAmps * 2, baseAmps * 4, baseAmps * 8, baseAmps * 12, baseAmps * 16};
            return this;
        }

        public BlockCableBuilder amps(int... amps) {
            this.amps = amps;
            return this;
        }

        public BlockCableBuilder insul(boolean buildInsulated, boolean buildUninsulated) {
            this.buildInsulated = buildInsulated;
            this.buildUninsulated = buildUninsulated;
            return this;
        }

        @Override
        public void build() {
            for (int i = 0; i < sizes.length; i++) {
                if (buildInsulated) new BlockCable(domain, material, sizes[i], true, loss, lossInsulated, amps[i], tier);
                if (buildUninsulated) new BlockCable(domain, material, sizes[i], false, loss, lossInsulated, amps[i], tier);
            }
        }
    }
}