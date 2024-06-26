package com.lucidplugins.api.utils;


import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.lucidplugins.api.item.SlottedItem;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InventoryUtils
{
    public static void itemOnItem(Item item1, Item item2)
    {
        Optional<Widget> itemToUse = Inventory.search().withId(item1.getId()).first();
        Optional<Widget> itemToUseOn = Inventory.search().withId(item2.getId()).first();

        if (itemToUse.isPresent() && itemToUseOn.isPresent())
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(itemToUse.get(), itemToUseOn.get());
        }
    }

    public static List<SlottedItem> getAll()
    {
        return Inventory.search().result().stream().map(item -> new SlottedItem(item.getItemId(), item.getItemQuantity(), item.getIndex())).collect(Collectors.toList());
    }

    public static List<SlottedItem> getAll(Predicate<SlottedItem> filter)
    {
        return Inventory.search().result().stream().map(item -> new SlottedItem(item.getItemId(), item.getItemQuantity(), item.getIndex())).filter(filter).collect(Collectors.toList());
    }

    public static boolean contains(String itemName)
    {
        return Inventory.search().nameContains(itemName).first().isPresent();
    }

    public static boolean contains(int[] ids)
    {
        List<Integer> intIdList = Arrays.stream(ids).boxed().collect(Collectors.toList());
        return !Inventory.search().idInList(intIdList).result().isEmpty();
    }

    public static boolean contains(int id)
    {
        return !Inventory.search().idInList(List.of(id)).result().isEmpty();
    }

    public static int getFreeSlots()
    {
        return Inventory.getEmptySlots();
    }

    public static boolean itemHasAction(int itemId, String action)
    {
        return Arrays.stream(EthanApiPlugin.getClient().getItemDefinition(itemId).getInventoryActions()).anyMatch(a -> a != null && a.equalsIgnoreCase(action));
    }

    public static void itemInteract(int itemId, String action)
    {
        InventoryInteraction.useItem(itemId, action);
    }

    public static void castAlchemyOnItem(int id, boolean highAlchemy)
    {
        Optional<Widget> itemWidget = Inventory.search().withId(id).first();
        final int alchemyWidgetId = highAlchemy ? 14286892 : 14286869;
        Widget alchemyWidget = EthanApiPlugin.getClient().getWidget(alchemyWidgetId);

        if (alchemyWidget != null)
        {
            itemWidget.ifPresent(widget -> {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(alchemyWidget, widget);
            });
        }
    }

    public static int calculateWidgetId(Item item)
    {
        Widget inventoryWidget = EthanApiPlugin.getClient().getWidget(WidgetInfo.INVENTORY.getPackedId());
        if (inventoryWidget == null)
        {
            return -1;
        }
        else
        {
            Widget[] children = inventoryWidget.getChildren();
            return children == null ? -1 : (Integer) Arrays.stream(children).filter((x) -> {
                return x.getItemId() == item.getId();
            }).findFirst().map(Widget::getId).orElse(-1);
        }
    }

    public static SlottedItem getItemInSlot(int slot)
    {
        Widget itemWidget = Inventory.search().indexIs(slot).first().orElse(null);

        int id = -1;
        int amount = -1;

        if (itemWidget != null)
        {
            amount = itemWidget.getItemQuantity();
            id = itemWidget.getItemId();
        }

        if (id == -1 || amount == -1)
        {
            return null;
        }

        return new SlottedItem(itemWidget.getItemId(), amount, itemWidget.getIndex());
    }

    public static SlottedItem getFirstItemSlotted(int id)
    {
        Widget itemWidget = Inventory.search().withId(id).first().orElse(null);
        int amount = -1;

        if (itemWidget != null)
        {
            amount = itemWidget.getItemQuantity();
        }

        if (id == -1 || amount == -1)
        {
            return null;
        }

        return new SlottedItem(id, amount, itemWidget.getIndex());
    }

    public static SlottedItem getLastItemSlotted(int id)
    {
        List<Widget> itemWidgets = Inventory.search().withId(id).result();
        Widget itemWidget = itemWidgets.get(itemWidgets.size() - 1);
        int amount = -1;

        if (itemWidget != null)
        {
            amount = itemWidget.getItemQuantity();
        }

        if (id == -1 || amount == -1)
        {
            return null;
        }

        return new SlottedItem(id, amount, itemWidget.getIndex());
    }

    public static SlottedItem getFirstItemSlotted(int[] ids)
    {
        List<Integer> intIdList = Arrays.stream(ids).boxed().collect(Collectors.toList());

        Widget itemWidget = Inventory.search().idInList(intIdList).first().orElse(null);
        int amount = -1;

        if (itemWidget != null)
        {
            amount = itemWidget.getItemQuantity();
        }

        if (itemWidget.getItemId() == -1 || amount == -1)
        {
            return null;
        }

        return new SlottedItem(itemWidget.getItemId(), amount, itemWidget.getIndex());
    }

    public static int getItemId(String name)
    {
        final Widget itemWidget = Inventory.search().filter(item -> item.getName().contains(name)).first().orElse(null);
        return itemWidget != null ? itemWidget.getItemId() : -1;
    }

    public static Item getFirstItem(int id)
    {
        Widget itemWidget = Inventory.search().withId(id).first().orElse(null);
        Item item = null;

        if (itemWidget != null)
        {
            item = new Item(itemWidget.getItemId(), itemWidget.getItemQuantity());
        }
        return item;
    }

    public static Item getFirstItem(String name)
    {
        Widget itemWidget = Inventory.search().nameContains(name).first().orElse(null);
        Item item = null;

        if (itemWidget != null)
        {
            item = new Item(itemWidget.getItemId(), itemWidget.getItemQuantity());
        }
        return item;
    }

    public static void wieldItem(int id)
    {
        itemInteract(id, "Wield");
    }

    public static int count(String name)
    {
        List<SlottedItem> itemsToCount = InventoryUtils.getAll(item -> {
            final ItemComposition itemDef = EthanApiPlugin.getClient().getItemDefinition(item.getItem().getId());
            return itemDef != null && itemDef.getName() != null && itemDef.getName().toLowerCase().contains(name.toLowerCase());
        });
        int count = 0;
        for (SlottedItem i : itemsToCount)
        {
            if (i != null)
            {
                count += i.getItem().getQuantity();
            }
        }
        return count;
    }

    public static int count(int id)
    {
        List<SlottedItem> itemsToCount = InventoryUtils.getAll(item -> item.getItem().getId() == id);

        int count = 0;
        for (SlottedItem i : itemsToCount)
        {
            if (i != null)
            {
                count += i.getItem().getQuantity();
            }
        }

        return count;
    }

    public static void interactSlot(int slot, String action)
    {
        InventoryInteraction.useItemIndex(slot, action);
    }
}
