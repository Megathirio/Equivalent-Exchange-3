package com.pahimar.ee3.exchange;

import com.pahimar.ee3.api.EnergyValue;
import com.pahimar.ee3.recipe.RecipeRegistry;
import com.pahimar.ee3.util.EnergyValueHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class EnergyValueRegistry
{
    private static EnergyValueRegistry energyValueRegistry = null;
    private static Map<WrappedStack, EnergyValue> preAssignedMappings;
    private static Map<WrappedStack, EnergyValue> postAssignedMappings;
    private SortedMap<WrappedStack, EnergyValue> stackMappings;
    private SortedMap<EnergyValue, List<WrappedStack>> valueMappings;

    private EnergyValueRegistry()
    {
    }

    public static EnergyValueRegistry getInstance()
    {
        if (energyValueRegistry == null)
        {
            energyValueRegistry = new EnergyValueRegistry();
            energyValueRegistry.init();
        }

        return energyValueRegistry;
    }

    public static void addPreAssignedEnergyValue(Object object, float energyValue)
    {
        addPreAssignedEnergyValue(object, new EnergyValue(energyValue));
    }

    public static void addPreAssignedEnergyValue(Object object, EnergyValue energyValue)
    {
        if (preAssignedMappings == null)
        {
            preAssignedMappings = new HashMap<WrappedStack, EnergyValue>();
        }

        if (WrappedStack.canBeWrapped(object) && energyValue != null && Float.compare(energyValue.getEnergyValue(), 0f) > 0)
        {
            WrappedStack wrappedStack = new WrappedStack(object);

            if (wrappedStack.getStackSize() > 0)
            {
                WrappedStack factoredWrappedStack = new WrappedStack(wrappedStack, 1);
                EnergyValue factoredEnergyValue = EnergyValueHelper.factorEnergyValue(energyValue, wrappedStack.getStackSize());

                if (preAssignedMappings.containsKey(factoredWrappedStack))
                {
                    if (factoredEnergyValue.compareTo(preAssignedMappings.get(factoredWrappedStack)) < 0)
                    {
                        preAssignedMappings.put(factoredWrappedStack, factoredEnergyValue);
                    }
                }
                else
                {
                    preAssignedMappings.put(factoredWrappedStack, factoredEnergyValue);
                }
            }
        }
    }

    public static void addPostAssignedEnergyValue(Object object, float energyValue)
    {
        if (postAssignedMappings == null)
        {
            postAssignedMappings = new HashMap<WrappedStack, EnergyValue>();
        }

        if (WrappedStack.canBeWrapped(object) && Float.compare(energyValue, 0f) > 0)
        {
            WrappedStack wrappedStack = new WrappedStack(object);

            if (wrappedStack.getStackSize() > 0)
            {
                WrappedStack factoredWrappedStack = new WrappedStack(wrappedStack, 1);
                EnergyValue factoredEnergyValue = new EnergyValue(energyValue * 1f / wrappedStack.getStackSize(), EnergyValue.EnergyType.CORPOREAL);

                postAssignedMappings.put(factoredWrappedStack, factoredEnergyValue);
            }
        }
    }

    public static void addPostAssignedEnergyValue(Object object, EnergyValue energyValue)
    {
        if (postAssignedMappings == null)
        {
            postAssignedMappings = new HashMap<WrappedStack, EnergyValue>();
        }

        if (WrappedStack.canBeWrapped(object) && energyValue != null && Float.compare(energyValue.getEnergyValue(), 0f) > 0)
        {
            WrappedStack wrappedStack = new WrappedStack(object);

            if (wrappedStack.getStackSize() > 0)
            {
                WrappedStack factoredWrappedStack = new WrappedStack(wrappedStack, 1);
                EnergyValue factoredEnergyValue = EnergyValueHelper.factorEnergyValue(energyValue, wrappedStack.getStackSize());

                postAssignedMappings.put(factoredWrappedStack, factoredEnergyValue);
            }
        }
    }

    public boolean hasEnergyValue(Object object, boolean strict)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            WrappedStack stack = new WrappedStack(object);

            if (energyValueRegistry.stackMappings.containsKey(new WrappedStack(stack.getWrappedStack())))
            {
                return true;
            }
            else
            {
                if (!strict)
                {
                    if (stack.getWrappedStack() instanceof ItemStack)
                    {
                        ItemStack wrappedItemStack = (ItemStack) stack.getWrappedStack();

                        // If its an OreDictionary item, scan its siblings for values
                        if (OreDictionary.getOreIDs(wrappedItemStack).length > 0)
                        {

                            OreStack oreStack = new OreStack(wrappedItemStack);

                            if (energyValueRegistry.stackMappings.containsKey(new WrappedStack(oreStack)))
                            {
                                return true;
                            }
                            else
                            {
                                for (ItemStack itemStack : OreDictionary.getOres(OreDictionary.getOreID(wrappedItemStack)))
                                {
                                    if (energyValueRegistry.stackMappings.containsKey(new WrappedStack(itemStack)))
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                        // Else, scan for if there is a wildcard value for it
                        else
                        {
                            for (WrappedStack valuedStack : energyValueRegistry.stackMappings.keySet())
                            {
                                if (valuedStack.getWrappedStack() instanceof ItemStack)
                                {
                                    ItemStack valuedItemStack = (ItemStack) valuedStack.getWrappedStack();

                                    if (Item.getIdFromItem(valuedItemStack.getItem()) == Item.getIdFromItem(wrappedItemStack.getItem()))
                                    {
                                        if (valuedItemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || wrappedItemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
                                        {
                                            return true;
                                        }
                                        else if (wrappedItemStack.getItem().isDamageable() && wrappedItemStack.isItemDamaged())
                                        {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else if (stack.getWrappedStack() instanceof OreStack)
                    {
                        OreStack oreStack = (OreStack) stack.getWrappedStack();
                        for (ItemStack oreItemStack : OreDictionary.getOres(oreStack.oreName))
                        {
                            if (energyValueRegistry.stackMappings.containsKey(new WrappedStack(oreItemStack)))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean hasEnergyValue(Object object)
    {
        return hasEnergyValue(object, false);
    }

    public EnergyValue getEnergyValue(Object object)
    {
        return getEnergyValue(object, false);
    }

    public EnergyValue getEnergyValue(Object object, boolean strict)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            WrappedStack stack = new WrappedStack(object);

            if (energyValueRegistry.stackMappings.containsKey(new WrappedStack(stack.getWrappedStack())))
            {
                return energyValueRegistry.stackMappings.get(new WrappedStack(stack.getWrappedStack()));
            }
            else
            {
                if (!strict)
                {
                    if (stack.getWrappedStack() instanceof ItemStack)
                    {
                        EnergyValue lowestValue = null;
                        ItemStack wrappedItemStack = (ItemStack) stack.getWrappedStack();

                        if (OreDictionary.getOreIDs(wrappedItemStack).length > 0)
                        {
                            OreStack oreStack = new OreStack(wrappedItemStack);

                            if (energyValueRegistry.stackMappings.containsKey(new WrappedStack(oreStack)))
                            {
                                return energyValueRegistry.stackMappings.get(new WrappedStack(oreStack));
                            }
                            else
                            {
                                for (ItemStack itemStack : OreDictionary.getOres(OreDictionary.getOreID(wrappedItemStack)))
                                {
                                    if (energyValueRegistry.stackMappings.containsKey(new WrappedStack(itemStack)))
                                    {
                                        if (lowestValue == null)
                                        {
                                            lowestValue = energyValueRegistry.stackMappings.get(new WrappedStack(itemStack));
                                        }
                                        else
                                        {
                                            EnergyValue itemValue = energyValueRegistry.stackMappings.get(new WrappedStack(itemStack));

                                            if (itemValue.compareTo(lowestValue) < 0)
                                            {
                                                lowestValue = itemValue;
                                            }
                                        }
                                    }
                                }

                                return lowestValue;
                            }
                        }
                        else
                        {
                            for (WrappedStack valuedStack : energyValueRegistry.stackMappings.keySet())
                            {
                                if (valuedStack.getWrappedStack() instanceof ItemStack)
                                {
                                    ItemStack valuedItemStack = (ItemStack) valuedStack.getWrappedStack();

                                    if (Item.getIdFromItem(valuedItemStack.getItem()) == Item.getIdFromItem(wrappedItemStack.getItem()))
                                    {
                                        if (valuedItemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || wrappedItemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
                                        {
                                            EnergyValue stackValue = energyValueRegistry.stackMappings.get(valuedStack);

                                            if (stackValue.compareTo(lowestValue) < 0)
                                            {
                                                lowestValue = stackValue;
                                            }
                                        }
                                        else if (wrappedItemStack.getItem().isDamageable() && wrappedItemStack.isItemDamaged())
                                        {
                                            EnergyValue stackValue = new EnergyValue(energyValueRegistry.stackMappings.get(valuedStack).getEnergyValue() * (1 - (wrappedItemStack.getItemDamage() * 1.0F / wrappedItemStack.getMaxDamage())));

                                            if (stackValue.compareTo(lowestValue) < 0)
                                            {
                                                lowestValue = stackValue;
                                            }
                                        }
                                    }
                                }
                            }

                            return lowestValue;
                        }
                    }
                    else if (stack.getWrappedStack() instanceof OreStack)
                    {
                        OreStack oreStack = (OreStack) stack.getWrappedStack();
                        for (ItemStack oreItemStack : OreDictionary.getOres(oreStack.oreName))
                        {
                            if (energyValueRegistry.stackMappings.containsKey(new WrappedStack(oreItemStack)))
                            {
                                return energyValueRegistry.stackMappings.get(new WrappedStack(oreItemStack));
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private void init()
    {
        stackMappings = new TreeMap<WrappedStack, EnergyValue>();

        /*
         *  Auto-assignment
         */
        // Initialize the maps for the first pass to happen
        stackMappings.putAll(preAssignedMappings);
        Map<WrappedStack, EnergyValue> computedStackValues = computeStackMappings();

        // Initialize the pass counter
        int passNumber = 0;

        while ((computedStackValues.size() > 0) && (passNumber < 16))
        {
            // Increment the pass counter
            passNumber++;

            // Compute stack mappings from existing stack mappings
            computedStackValues = computeStackMappings();
            for (WrappedStack keyStack : computedStackValues.keySet())
            {
                EnergyValue factoredExchangeEnergyValue = null;
                WrappedStack factoredKeyStack = null;

                if (keyStack != null && keyStack.getWrappedStack() != null && keyStack.getStackSize() > 0)
                {
                    if (computedStackValues.get(keyStack) != null && Float.compare(computedStackValues.get(keyStack).getEnergyValue(), 0f) > 0)
                    {
                        factoredExchangeEnergyValue = EnergyValueHelper.factorEnergyValue(computedStackValues.get(keyStack), keyStack.getStackSize());
                        factoredKeyStack = new WrappedStack(keyStack, 1);
                    }
                }

                if (factoredExchangeEnergyValue != null)
                {
                    if (stackMappings.containsKey(factoredKeyStack))
                    {
                        if (factoredExchangeEnergyValue.compareTo(stackMappings.get(factoredKeyStack)) == -1)
                        {
                            stackMappings.put(factoredKeyStack, factoredExchangeEnergyValue);
                        }
                    }
                    else
                    {
                        stackMappings.put(factoredKeyStack, factoredExchangeEnergyValue);
                    }
                }
            }
        }

        /*
         *  Post-assigned values
         */
        if (postAssignedMappings != null)
        {
            for (WrappedStack wrappedStack : postAssignedMappings.keySet())
            {
                stackMappings.put(wrappedStack, postAssignedMappings.get(wrappedStack));
            }
        }

        
        /*
         *  Value map resolution
         */
        valueMappings = new TreeMap<EnergyValue, List<WrappedStack>>();

        for (WrappedStack stack : stackMappings.keySet())
        {
            if (stack != null)
            {
                EnergyValue value = stackMappings.get(stack);

                if (value != null)
                {
                    if (valueMappings.containsKey(value))
                    {
                        if (!(valueMappings.get(value).contains(stack)))
                        {
                            valueMappings.get(value).add(stack);
                        }
                    }
                    else
                    {
                        valueMappings.put(value, new ArrayList<WrappedStack>(Arrays.asList(stack)));
                    }
                }
            }
        }
    }

    private Map<WrappedStack, EnergyValue> computeStackMappings()
    {
        Map<WrappedStack, EnergyValue> computedStackMap = new HashMap<WrappedStack, EnergyValue>();

        for (WrappedStack recipeOutput : RecipeRegistry.getInstance().getRecipeMappings().keySet())
        {
            if (!hasEnergyValue(recipeOutput.getWrappedStack(), false) && !computedStackMap.containsKey(recipeOutput))
            {
                EnergyValue lowestValue = null;

                for (List<WrappedStack> recipeInputs : RecipeRegistry.getInstance().getRecipeMappings().get(recipeOutput))
                {
                    EnergyValue computedValue = EnergyValueHelper.computeEnergyValueFromList(recipeInputs);
                    computedValue = EnergyValueHelper.factorEnergyValue(computedValue, recipeOutput.getStackSize());

                    if (computedValue != null)
                    {
                        if (computedValue.compareTo(lowestValue) < 0)
                        {
                            lowestValue = computedValue;
                        }
                    }
                }

                if ((lowestValue != null) && (lowestValue.getEnergyValue() > 0f))
                {
                    computedStackMap.put(new WrappedStack(recipeOutput.getWrappedStack()), lowestValue);
                }
            }
        }

        return computedStackMap;
    }

    public List<WrappedStack> getStacksInRange(int start, int finish)
    {
        return getStacksInRange(new EnergyValue(start), new EnergyValue(finish));
    }

    public List<WrappedStack> getStacksInRange(float start, float finish)
    {
        return getStacksInRange(new EnergyValue(start), new EnergyValue(finish));
    }

    public List<WrappedStack> getStacksInRange(EnergyValue start, EnergyValue finish)
    {
        List<WrappedStack> stacksInRange = new ArrayList<WrappedStack>();

        SortedMap<EnergyValue, List<WrappedStack>> tailMap = energyValueRegistry.valueMappings.tailMap(start);
        SortedMap<EnergyValue, List<WrappedStack>> headMap = energyValueRegistry.valueMappings.headMap(finish);

        SortedMap<EnergyValue, List<WrappedStack>> smallerMap;
        SortedMap<EnergyValue, List<WrappedStack>> biggerMap;

        if (!tailMap.isEmpty() && !headMap.isEmpty())
        {

            if (tailMap.size() <= headMap.size())
            {
                smallerMap = tailMap;
                biggerMap = headMap;
            }
            else
            {
                smallerMap = headMap;
                biggerMap = tailMap;
            }

            for (EnergyValue value : smallerMap.keySet())
            {
                if (biggerMap.containsKey(value))
                {
                    stacksInRange.addAll(energyValueRegistry.valueMappings.get(value));
                }
            }
        }

        return stacksInRange;
    }
}
