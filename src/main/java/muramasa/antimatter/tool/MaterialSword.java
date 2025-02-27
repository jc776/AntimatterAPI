package muramasa.antimatter.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mcp.MethodsReturnNonnullByDefault;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Ref;
import muramasa.antimatter.material.Material;
import muramasa.antimatter.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialSword extends SwordItem implements IAntimatterTool {

    protected String domain;
    protected AntimatterToolType type;

    protected int energyTier;
    protected long maxEnergy;

    public MaterialSword(String domain, AntimatterToolType type, Properties properties) {
        super(AntimatterItemTier.NULL, 0, type.getBaseAttackSpeed(), properties);  // 0 as base attack as it adds
        this.domain = domain;
        this.type = type;
        this.energyTier = -1;
        this.maxEnergy = -1;
        AntimatterAPI.register(IAntimatterTool.class, getId(), this);
    }

    public MaterialSword(String domain, AntimatterToolType type, Properties properties, int energyTier) {
        super(AntimatterItemTier.NULL, (int) type.getBaseAttackDamage(), type.getBaseAttackSpeed(), properties);
        this.domain = domain;
        this.type = type;
        this.energyTier = energyTier;
        this.maxEnergy = type.getBaseMaxEnergy() * energyTier;
        AntimatterAPI.register(IAntimatterTool.class, getId(), this);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getId() {
        return type.isPowered() ? String.join("_", type.getId(), Ref.VN[energyTier].toLowerCase(Locale.ENGLISH)) : type.getId();
    }

    @Nonnull
    @Override
    public AntimatterToolType getType() {
        return type;
    }

    @Nonnull
    @Override
    public Set<ToolType> getToolTypes(ItemStack stack) {
        return type.getToolTypes().stream().map(ToolType::get).collect(Collectors.toSet());
    }

    /** Returns -1 if its not a powered tool **/
    public int getEnergyTier() {
        return energyTier;
    }

    @Nonnull
    @Override
    public ItemStack asItemStack(@Nonnull Material primary, @Nonnull Material secondary) {
        return resolveStack(primary, secondary, 0, maxEnergy);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> list) {
        onGenericFillItemGroup(group, list, maxEnergy);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        onGenericAddInformation(stack, tooltip, flag);
        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return type.getUseAction();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return type.getUseAction() == UseAction.NONE ? super.getUseDuration(stack) : 72000;
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state) {
        return Utils.isToolEffective(this, state) && getTier(stack).getHarvestLevel() >= state.getHarvestLevel();
    }

    @Override
    public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        return getTier(stack).getHarvestLevel();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getTier(stack).getMaxUses();
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return onGenericHitEntity(stack, target, attacker, 0.75F, 0.75F);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (type.getToolTypes().contains("sword") && state.getBlock() == Blocks.COBWEB) return 15.0F;
        return Utils.isToolEffective(this, state) ? getTier(stack).getEfficiency() : 1.0F;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entity) {
        return onGenericBlockDestroyed(stack, world, state, pos, entity);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        return onGenericItemUse(ctx);
    }

    @Override
    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        return type.getBlockBreakability();
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return type.getToolTypes().contains("axe");
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slotType, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slotType == EquipmentSlotType.MAINHAND) {
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", type.getBaseAttackDamage() + getTier(stack).getAttackDamage(), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", type.getBaseAttackSpeed(), AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) ? 0 : damage(stack, amount);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return getTier(stack).getEnchantability();
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return !type.isPowered();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return type.isPowered() ? enchantment != Enchantments.UNBREAKING : super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack oldStack) {
        return getGenericContainerItem(oldStack);
    }

}
