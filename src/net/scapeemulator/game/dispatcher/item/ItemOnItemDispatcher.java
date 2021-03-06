/**
 * Copyright (c) 2012, Hadyn Richard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
 * THE SOFTWARE.
 */

package net.scapeemulator.game.dispatcher.item;

import java.util.HashMap;
import java.util.Map;

import net.scapeemulator.game.model.player.Player;
import net.scapeemulator.game.model.player.SlottedItem;
import net.scapeemulator.game.model.player.inventory.Inventory;

/**
 * Created by Hadyn Richard
 */
public final class ItemOnItemDispatcher {

    private Map<Integer, ItemOnItemHandler> handlers = new HashMap<>();

    public ItemOnItemDispatcher() {
    }

    public void bind(ItemOnItemHandler handler) {
        int hash = calculateHash(handler.getItemOne(), handler.getItemTwo());
        if (handlers.containsKey(hash)) {
            System.out.println("Overwriting existing ItemOnItem handler! <id1: " + handler.getItemOne() + "; id2: " + handler.getItemTwo() + ">");
        }
        handlers.put(hash, handler);
    }

    public void unbindAll() {
        handlers.clear();
    }

    public void handle(Player player, int idOne, int idTwo, int hashOne, int hashTwo, int slotOne, int slotTwo) {
        if (player.actionsBlocked()) {
            return;
        }
        Inventory inventoryOne = player.getInventorySet().get(hashOne);
        Inventory inventoryTwo = inventoryOne;
        if (hashOne != hashTwo) {
            inventoryTwo = player.getInventorySet().get(hashTwo);
        }

        if (inventoryOne == null || !checkInventory(inventoryOne, slotOne, idOne) || inventoryTwo == null || !checkInventory(inventoryTwo, slotTwo, idTwo)) {
            return;
        }

        ItemOnItemHandler handler = handlers.get(calculateHash(idOne, idTwo));
        if (handler != null) {

            SlottedItem itemOne = new SlottedItem(slotOne, inventoryOne.get(slotOne));
            SlottedItem itemTwo = new SlottedItem(slotTwo, inventoryTwo.get(slotTwo));

            /* Swap the items and inventories if they are out of place */
            if (idOne != handler.getItemOne()) {
                Inventory tempInv = inventoryOne;
                inventoryOne = inventoryTwo;
                inventoryTwo = tempInv;

                SlottedItem tempItem = itemOne;
                itemOne = itemTwo;
                itemTwo = tempItem;
            }

            handler.handle(player, inventoryOne, inventoryTwo, itemOne, itemTwo);
        } else {
            player.sendMessage("Nothing interesting happens.");
        }
    }

    private static boolean checkInventory(Inventory inventory, int slot, int itemId) {
        return inventory.get(slot) != null && inventory.get(slot).getId() == itemId;
    }

    static int calculateHash(int itemOne, int itemTwo) {

        int high = itemOne;
        int low = itemTwo;

        if (itemTwo > itemOne) {
            high = itemTwo;
            low = itemOne;
        }

        return high << 16 | low;
    }

    static int[] reverseHash(int hash) {
        int[] ids = new int[2];
        ids[0] = hash & 0xffff;
        ids[1] = hash >> 16;
        return ids;
    }
}
