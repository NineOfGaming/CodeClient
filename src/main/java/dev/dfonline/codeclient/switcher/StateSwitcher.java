package dev.dfonline.codeclient.switcher;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.location.*;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class StateSwitcher extends GenericSwitcher {
    public StateSwitcher() {
        super(Text.literal("Mode Switcher"), GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4);
    }

    @Override
    protected void init() {
        selected = 0;
        if(CodeClient.lastLocation instanceof Plot) {
            if(CodeClient.lastLocation instanceof Play) selected = 0;
            if(CodeClient.lastLocation instanceof Build) selected = 1;
            if(CodeClient.lastLocation instanceof Dev) selected = 2;
        }
        if(CodeClient.lastLocation instanceof Spawn) {
            if(CodeClient.location instanceof Creator) selected = 0;
            if(CodeClient.location instanceof Play) selected = 2;
        }
        this.options.add(new Option(Text.literal("Play"), Items.DIAMOND.getDefaultStack(), () -> joinMode("play")));
        this.options.add(new Option(Text.literal("Build"), Items.GRASS_BLOCK.getDefaultStack(), () -> joinMode("build")));
        this.options.add(new Option(Text.literal("Code"), Items.COMMAND_BLOCK.getDefaultStack(), () -> joinMode("dev")));
        super.init();
    }



    private void joinMode(String mode) {
        CodeClient.MC.getNetworkHandler().sendCommand(mode);
    }
}
