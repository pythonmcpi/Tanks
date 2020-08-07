package tanks.hotbar;

import tanks.Drawing;
import tanks.Game;
import tanks.Player;
import tanks.event.EventSetItem;
import tanks.event.EventSetItemBarSlot;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemEmpty;

public class ItemBar
{
	public static int size = 50; // The slot size.
	public static int count_margin_right = 26; // Item number's distance from right.
	public static int count_margin_bottom = 35; // Item number's distance from bottom.
	public static int gap = 75; // Gap between slots.
	public static int bar_margin = 60; // Bar's distance from bottom.

	public static double slotBgR = 0;
	public static double slotBgG = 0;
	public static double slotBgB = 0;
	public static double slotBgA = 127;

	public static double slotSelectedR = 255;
	public static double slotSelectedG = 128;
	public static double slotSelectedB = 0;

	public static double itemCountR = 255;
	public static double itemCountG = 255;
	public static double itemCountB = 255;

	public Item[] slots = new Item[5];
	public Button[] slotButtons = new Button[5];

	public int selected = -1;
	
	public Player player;

	public ItemBar(Player p)
	{
		for (int i = 0; i < slots.length; i++)
			slots[i] = new ItemEmpty();

		this.player = p;

		for (int i = 0; i < this.slotButtons.length; i++)
		{
			final int j = i;
			this.slotButtons[i] = new Button(0, 0, size + 2.5, size * 1.5, "", new Runnable()
			{
				@Override
				public void run()
				{
					setItem(j);
				}
			});
		}
	}

	public boolean addItem(Item item)
	{
		Item i = Item.parseItem(this.player, item.toString());
		int emptyAmount = 0;
		for (int x = 0; x < this.slots.length; x++)
		{
			if (this.slots[x].name.equals(i.name) || this.slots[x] instanceof ItemEmpty)
				emptyAmount += i.maxStackSize - slots[x].stackSize;
		}

		if (emptyAmount < i.stackSize)
			return false;

		for (int x = 0; x < this.slots.length; x++)
		{
			if (this.slots[x].name.equals(i.name) && this.slots[x].stackSize >= this.slots[x].maxStackSize)
				continue;

			if (this.slots[x].name.equals(i.name))
			{
				if (this.slots[x].stackSize + i.stackSize <= this.slots[x].maxStackSize)
				{
					this.slots[x].stackSize += i.stackSize;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					return true;
				}
				else
				{
					int remaining = this.slots[x].stackSize + i.stackSize - this.slots[x].maxStackSize;
					this.slots[x].stackSize = this.slots[x].maxStackSize;
					i.stackSize = remaining;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					this.addItem(i);
					return true;
				}
			}
			else if (this.slots[x] instanceof ItemEmpty)
			{
				if (i.stackSize <= i.maxStackSize)
				{
					this.slots[x] = i;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					return true;
				}
				else
				{
					int remaining = i.stackSize - i.maxStackSize;
					this.slots[x] = Item.parseItem(this.player, i.toString());
					this.slots[x].stackSize = this.slots[x].maxStackSize;
					i.stackSize = remaining;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					this.addItem(i);
					return true;
				}
			}
		}
		return true;
	}

	public boolean useItem(boolean rightClick)
	{
		if (selected == -1)
			return false;

		if (slots[selected] instanceof ItemEmpty)
			return false;

		if (slots[selected].rightClick != rightClick)
			return false;

		slots[selected].attemptUse();

		if (slots[selected].destroy)
			slots[selected] = new ItemEmpty();

		if (this.player != Game.player)
			Game.eventsOut.add(new EventSetItem(this.player, this.selected, this.slots[this.selected]));

		return true;
	}

	public void update()
	{
		checkKey(Game.game.input.hotbar1, 0);
		checkKey(Game.game.input.hotbar2, 1);
		checkKey(Game.game.input.hotbar3, 2);
		checkKey(Game.game.input.hotbar4, 3);
		checkKey(Game.game.input.hotbar5, 4);

		if (Game.game.window.touchscreen && this.player.hotbar.persistent)
		{
			for (int i = 0; i < this.slotButtons.length; i++)
			{
				Button b = this.slotButtons[i];
				b.posX = ((i - 2) * gap) + (Drawing.drawing.interfaceSizeX / 2);
				b.posY = Drawing.drawing.interfaceSizeY - bar_margin - this.player.hotbar.verticalOffset;
				b.update();
			}
		}

		if (Game.game.window.validScrollUp)
		{
			this.setItem(((this.selected - 1) + this.slots.length) % this.slots.length);
			Game.game.window.validScrollUp = false;
		}

		if (Game.game.window.validScrollDown)
		{
			this.setItem(((this.selected + 1) + this.slots.length) % this.slots.length);
			Game.game.window.validScrollDown = false;
		}
	}

	public void checkKey(InputBindingGroup input, int index)
	{
		if (input.isValid())
		{
			this.setItem(index);
			input.invalidate();
		}
	}

	public void setItem(int index)
	{
		if (this.player.hotbar != null)
		{
			this.player.hotbar.hidden = false;

			if (!Game.game.window.touchscreen)
				this.player.hotbar.hideTimer = 500;
		}

		this.selected = (this.selected == index ? -1 : index);

		if (ScreenPartyLobby.isClient)
			Game.eventsOut.add(new EventSetItemBarSlot(this.selected));
	}

	public void draw()
	{
		for (int i = -2; i <= 2; i++)
		{
			Drawing.drawing.setColor(slotBgR, slotBgG, slotBgB, slotBgA * (100 - this.player.hotbar.percentHidden) / 100.0);

			int x = (int) ((i * gap) + (Drawing.drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.drawing.interfaceSizeY - bar_margin + this.player.hotbar.percentHidden - this.player.hotbar.verticalOffset);

			Drawing.drawing.fillInterfaceRect(x, y, size, size);

			if (i + 2 == selected)
			{
				Drawing.drawing.setColor(slotSelectedR, slotSelectedG, slotSelectedB, (100 - this.player.hotbar.percentHidden) * 2.55);
				Drawing.drawing.fillInterfaceRect(x, y, size, size);
				Drawing.drawing.setColor(slotBgR, slotBgG, slotBgB, slotBgA);
			}

			Drawing.drawing.setColor(255, 255, 255, (100 - this.player.hotbar.percentHidden) * 2.55);
			if (slots[i + 2].icon != null)
				Drawing.drawing.drawInterfaceImage("/" + slots[i + 2].icon, x, y, size, size);

			if (slots[i + 2] != null)
			{
				Item item = slots[i + 2];
				if (item.stackSize > 1)
				{
					Drawing.drawing.setColor(itemCountR, itemCountG, itemCountB, (100 - this.player.hotbar.percentHidden) * 2.55);
					Drawing.drawing.setInterfaceFontSize(18);
					Drawing.drawing.drawInterfaceText(x + size - count_margin_right, y + size - count_margin_bottom, Integer.toString(item.stackSize), true);
					Drawing.drawing.setColor(slotBgR, slotBgG, slotBgB, slotBgA * (100 - this.player.hotbar.percentHidden) / 100.0);
				}
			}
		}
	}
}
