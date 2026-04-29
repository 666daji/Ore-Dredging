package org.oredredging.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ThunderSmeltRecipeSerializer implements RecipeSerializer<ThunderSmeltRecipe> {
    @Override
    public ThunderSmeltRecipe read(Identifier id, JsonObject json) {
        // 1. 原料
        Ingredient input = Ingredient.fromJson(json.get("input"));

        // 2. 主产物
        JsonObject outputObj = json.getAsJsonObject("output");
        ItemStack output = itemStackFromJson(outputObj);

        // 3. 可选：额外产物
        Item extraItem = null;
        int extraMin = 0;
        int extraMax = 0;
        if (json.has("extra")) {
            JsonObject extraObj = json.getAsJsonObject("extra");
            if (extraObj.has("item")) {  // item 字段存在且不为 null 时启用额外产物
                extraItem = Registries.ITEM.get(new Identifier(extraObj.get("item").getAsString()));
                extraMin = extraObj.get("min").getAsInt();
                extraMax = extraObj.get("max").getAsInt();
            }
        }
        return new ThunderSmeltRecipe(id, input, output, extraItem, extraMin, extraMax);
    }

    @Override
    public ThunderSmeltRecipe read(Identifier id, PacketByteBuf buf) {
        Ingredient input = Ingredient.fromPacket(buf);
        ItemStack output = buf.readItemStack();

        boolean hasExtra = buf.readBoolean();
        Item extraItem = null;
        int extraMin = 0;
        int extraMax = 0;
        if (hasExtra) {
            extraItem = Registries.ITEM.get(buf.readIdentifier());
            extraMin = buf.readVarInt();
            extraMax = buf.readVarInt();
        }
        return new ThunderSmeltRecipe(id, input, output, extraItem, extraMin, extraMax);
    }

    @Override
    public void write(PacketByteBuf buf, ThunderSmeltRecipe recipe) {
        recipe.getInput().write(buf);
        buf.writeItemStack(recipe.getOutput(null)); // 直接写主产物，无需注册管理器

        boolean hasExtra = recipe.getExtraItem() != null;
        buf.writeBoolean(hasExtra);
        if (hasExtra) {
            buf.writeIdentifier(Registries.ITEM.getId(recipe.getExtraItem()));
            buf.writeVarInt(recipe.getExtraMin());
            buf.writeVarInt(recipe.getExtraMax());
        }
    }

    /** 辅助方法：从 JSON 对象构建 ItemStack（只支持 item 和 count，不含NBT） */
    private static ItemStack itemStackFromJson(JsonObject json) {
        Item item = Registries.ITEM.get(new Identifier(JsonHelper.getString(json, "item")));
        int count = JsonHelper.getInt(json, "count", 1);
        return new ItemStack(item, count);
    }
}