# 🤖 Copper Ledger

#### **Help your golems remember where things go!**

![Copper Ledger Icon](src/main/resources/assets/copper_ledgers/icon.png)


## ℹ️ The mod

Ever taken your entire supply of an item out of a chest, only for your copper golems to immediately forget that's where that item belongs?

This mod adds the **Copper Ledger**, which allows you to create a checklist of any items from your inventory. When you place the ledger into a chest, a copper golem will put items specified by the ledger into the chest, even if there are none already there.

If a copper golem reaches a chest with no ledger, or a ledger that does not list the item, it falls back to the default behavior. 

## 📙 Recipe

Craft a ledger by surrounding a **book** with **8 copper ingots**

![Crafting recipe](design/img/recipe.png)

## ⁉️ How to use

### Adding and removing items

Let's dedicate a chest to ores.

To add an item to the ledger, click on it from your inventory. This adds an entry to the ledger, and does not consume the item from your inventory.

![Adding item](design/img/adding_items.png)

We decided we want to keep lapis in a different location. To remove an item from the ledger, simply click on it

![Removing item](design/img/removing_items.png)

### In action

To tell your golems to follow a ledger, simply put it anywhere in the desired chest.

In the example below, we'll give the golem a diamond, a netherite ingot, a cookie, and an oak log.  We have two chests. On the left, we have a double chest with the ore ledger, along with a cookie. On the right, we have a small chest, with various types of wood.

![Demo](design/img/demo_setup.png)

When searching a chest, golems first check for any ledgers. If no ledgers are found, or if none of them specify the item in the golem's hand, the golem will fall back on vanilla behavior. 

With this in mind, we expect the golem to put the diamond, netherite, and cookie into the double chest, and the oak log into the single.

And as you can see, that's exactly what happens!

![Demo gif](design/img/copper_ledger_demo.gif)

## 🚮 Clear and de-craft

If you'd like to clear a ledger, you can either spam click the items away, or scrape it off in a grindstone

![Clear with grindstone](design/img/scrape.png)

Or if you just want your book back, throw it into a crafting table (at the cost of your copper)

![De-crafting recipe](design/img/decraft.png)