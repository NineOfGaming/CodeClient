package dev.dfonline.codeclient.dev.menu.customchest;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.hypercube.item.NamedItem;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomChestMenu extends HandledScreen<CustomChestHandler> implements ScreenHandlerProvider<CustomChestHandler> {

    private final CustomChestNumbers Size;
    private double scroll = 0;
    private final ArrayList<Widget> widgets = new ArrayList<>();
    private final ArrayList<VarItem> varItems = new ArrayList<>();

    public CustomChestMenu(CustomChestHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        Size = handler.numbers;
        this.titleY = 4;
        this.playerInventoryTitleY = 123;
        this.backgroundHeight = Size.MENU_HEIGHT;
        this.backgroundWidth = Size.MENU_WIDTH;
    }

    @Override
    protected void init() {
        super.init();
        update((int) scroll);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0);
        for (Widget widget : widgets) {
            if (widget instanceof Drawable drawable) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }
        List<Slot> subList = this.getScreenHandler().slots.subList((int) scroll, (int) scroll + Size.SLOTS);
        for (int i = 0; i < subList.size(); i++) {
            var slot = subList.get(i);
            final int x = Size.SLOT_X;
            final int y = i * 18 + Size.SLOT_Y;
            context.drawItem(slot.getStack(),x,y);
            context.drawItemInSlot(textRenderer,slot.getStack(),x,y);
            int relX = mouseX - this.x;
            int relY = mouseY - this.y;
            if(
                       relX > x
                    && relX < x+18
                    && relY > y
                    && relY < y+18
            ) {
                drawSlotHighlight(context, x, y, -10);
                focusedSlot = slot;
            }
        }

        if(focusedSlot != null) {
            if(focusedSlot.hasStack()) context.drawItemTooltip(textRenderer,focusedSlot.getStack(),mouseX - this.x,mouseY - this.y);
        }
        context.getMatrices().pop();
    }

    private void update(int previousScroll) {
        Integer focused = null;
        for (int i = 0; i < widgets.size(); i++) {
            var widget = widgets.get(i);
            if (widget instanceof ClickableWidget clickable) {
                if (clickable.isFocused()) focused = (i + previousScroll) - (int) scroll;
            }
        }
        widgets.clear();
        List<Slot> subList = this.getScreenHandler().slots.subList((int) scroll, (int) scroll + Size.SLOTS);
        for (int i = 0; i < subList.size(); i++) {
            Slot slot = subList.get(i);
            if(!slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
            final int x = Size.WIDGET_X;
            final int y = i * 18 + Size.SLOT_Y;
            VarItem varItem = (VarItems.parse(stack));
            varItems.add(varItem);
            if(i >= Size.WIDGETS) continue;
            if(varItem instanceof NamedItem named) {
                TextFieldWidget widget = new TextFieldWidget(textRenderer,x,y,128,16,Text.of(named.getName()));
                widget.setText(named.getName());
                widget.setFocused(Objects.equals(i,focused));
                widgets.add(widget);
                varItems.add(named);
                continue;
            }

            widgets.add(new TextWidget(x + 3,y,Size.WIDGET_WIDTH,16,stack.getName(),textRenderer).alignLeft());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double prev = scroll;
        scroll = Math.min(Math.max(0, scroll - verticalAmount), 27 - Size.WIDGETS);
        update((int) prev);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Slot> subList = this.getScreenHandler().slots.subList((int) scroll, (int) scroll + 6);
        for (Widget widget : widgets) {
            if (widget instanceof ClickableWidget clickable) {
                clickable.setFocused(clickable.isMouseOver(mouseX - this.x, mouseY - this.y));
            }
        }
        for (int i = 0; i < subList.size(); i++) {
            var slot = subList.get(i);
            final int x = 8;
            final int y = i * 18 - 11 + 25;
            double relX = mouseX - this.x;
            double relY = mouseY - this.y;
            if(
                    relX > x
                            && relX < x+18
                            && relY > y
                            && relY < y+18
            ) {
                if(button == 2) {
                    this.onMouseClick(slot, slot.id, button, SlotActionType.CLONE);
                    return true;
                }
                if(hasShiftDown()) {
                    this.onMouseClick(slot, slot.id, button, SlotActionType.QUICK_MOVE);
                    return true;
                }
                this.onMouseClick(slot, slot.id, button, SlotActionType.PICKUP);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void setItems() {
        List<Slot> subList = this.getScreenHandler().slots.subList((int) scroll, (int) scroll + 6);
        if(widgets == null) return;
        for (int i = 0; i < widgets.size(); i++) {
            Slot slot = subList.get(i);
            VarItem item = varItems.get(i);
            Widget widget = widgets.get(i);
            if(widget == null || slot == null || item == null) continue;
            if (widget instanceof ClickableWidget) {
                if (item instanceof NamedItem named && widget instanceof TextFieldWidget text) {
                    named.setName(text.getText());
                    slot.setStack(named.toStack());
                    super.onMouseClick(slot,slot.id,0,SlotActionType.SWAP);
                    CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36, named.toStack()));
                    super.onMouseClick(slot,slot.id,0,SlotActionType.SWAP);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : widgets) {
            if (widget instanceof ClickableWidget clickable) {
                if(clickable.isFocused()) {
                    clickable.keyPressed(keyCode, scanCode, modifiers);
//                    setItems();
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : widgets) {
            if (widget instanceof ClickableWidget clickable) {
                if(clickable.isFocused()) {
                    clickable.keyReleased(keyCode, scanCode, modifiers);
                    setItems();
                    return true;
                }
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Widget widget : widgets) {
            if (widget instanceof ClickableWidget clickable) {
                if(clickable.isFocused()) {
                    clickable.charTyped(chr, modifiers);
//                    setItems();
                    return true;
                }
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        super.onMouseClick(slot, slotId, button, actionType);
        update((int) scroll);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.getMatrices().push();
        RenderSystem.enableBlend();
        int centerX = this.width / 2 - (Size.MENU_WIDTH / 2);
        int centerY = this.height / 2 - (Size.MENU_HEIGHT / 2);
        context.drawTexture(Size.TEXTURE, centerX, centerY, 0.0F, 0.0F, Size.MENU_WIDTH, Size.MENU_HEIGHT, Size.TEXTURE_WIDTH, Size.TEXTURE_HEIGHT);

        boolean disabled = false;
        float scrollProgress = (float) scroll / (27 - Size.WIDGETS);
        context.drawTexture(Size.TEXTURE,
                centerX + Size.SCROLL_POS_X,
                (int) (centerY + Size.SCROLL_POS_Y + scrollProgress * (Size.SCROLL_ROOM - Size.SCROLL_HEIGHT)),
                (disabled ? Size.MENU_WIDTH + Size.SCROLL_WIDTH : Size.MENU_WIDTH),
                0,
                Size.SCROLL_WIDTH,
                Size.SCROLL_HEIGHT,
                Size.TEXTURE_WIDTH,
                Size.TEXTURE_HEIGHT
        );
        context.getMatrices().pop();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
//        super.drawForeground(context, mouseX, mouseY);
    }
}
