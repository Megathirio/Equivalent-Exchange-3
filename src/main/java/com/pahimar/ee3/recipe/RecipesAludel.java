package com.pahimar.ee3.recipe;

import com.google.common.collect.ImmutableList;
import com.pahimar.ee3.api.RecipeRegistryProxy;
import com.pahimar.ee3.exchange.OreStack;
import com.pahimar.ee3.item.crafting.RecipeAludel;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipesAludel
{
    private static RecipesAludel aludelRegistry = null;

    private List<RecipeAludel> aludelRecipes;

    private RecipesAludel()
    {
        aludelRecipes = new ArrayList<RecipeAludel>();
    }

    public static RecipesAludel getInstance()
    {
        if (aludelRegistry == null)
        {
            aludelRegistry = new RecipesAludel();
        }

        return aludelRegistry;
    }

    public void addRecipe(ItemStack recipeOutput, ItemStack recipeInputStack, ItemStack recipeInputDust)
    {
        addRecipe(new RecipeAludel(recipeOutput, recipeInputStack, recipeInputDust));
    }

    public void addRecipe(RecipeAludel recipeAludel)
    {
        if (!aludelRecipes.contains(recipeAludel))
        {
            aludelRecipes.add(recipeAludel);
        }
    }

    public void addRecipe(ItemStack recipeOutput, OreStack recipeInputStack, ItemStack recipeInputDust)
    {
        addRecipe(new RecipeAludel(recipeOutput, recipeInputStack, recipeInputDust));
    }

    public ItemStack getResult(ItemStack recipeInputStack, ItemStack recipeInputDust)
    {
        for (RecipeAludel recipeAludel : aludelRecipes)
        {
            if (recipeAludel.matches(recipeInputStack, recipeInputDust))
            {
                return recipeAludel.getRecipeOutput();
            }
        }

        return null;
    }

    public RecipeAludel getRecipe(ItemStack recipeInputStack, ItemStack recipeInputDust)
    {
        for (RecipeAludel recipeAludel : aludelRecipes)
        {
            if (recipeAludel.matches(recipeInputStack, recipeInputDust))
            {
                return recipeAludel;
            }
        }

        return null;
    }

    public List<RecipeAludel> getRecipes()
    {
        return ImmutableList.copyOf(aludelRecipes);
    }

    public static void registerRecipes()
    {
        for (RecipeAludel recipeAludel : RecipesAludel.getInstance().getRecipes())
        {
            RecipeRegistryProxy.addRecipe(recipeAludel.getRecipeOutput(), recipeAludel.getRecipeInputsAsWrappedStacks());
        }
    }
}
