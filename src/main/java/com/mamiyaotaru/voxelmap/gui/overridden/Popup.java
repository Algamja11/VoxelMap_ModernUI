package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;

public class Popup {
    MinecraftClient client;
    TextRenderer fontRendererObj;
    int x;
    int y;
    PopupEntry[] entries;
    int w;
    int h;
    public int clickedX;
    public int clickedY;
    public int clickedDirectX;
    public int clickedDirectY;
    boolean shouldClose = false;
    PopupGuiScreen parentGui;
    int padding = 6;

    public Popup(int x, int y, int directX, int directY, ArrayList entries, PopupGuiScreen parentGui) {
        this.client = MinecraftClient.getInstance();
        this.fontRendererObj = this.client.textRenderer;
        this.parentGui = parentGui;
        this.clickedX = x;
        this.clickedY = y;
        this.clickedDirectX = directX;
        this.clickedDirectY = directY;
        this.x = x - 1;
        this.y = y - 1;
        this.entries = new PopupEntry[entries.size()];
        entries.toArray(this.entries);
        this.w = 0;
        this.h = this.entries.length * 20;

        for (int t = 0; t < this.entries.length; ++t) {
            int entryWidth = this.fontRendererObj.getWidth(this.entries[t].name);
            if (entryWidth > this.w) {
                this.w = entryWidth;
            }
        }

        this.w += this.padding * 2;
        if (x + this.w > parentGui.width) {
            this.x = x - this.w + 2;
        }

        if (y + this.h > parentGui.height) {
            this.y = y - this.h + 2;
        }

    }

    public boolean clickedMe(double mouseX, double mouseY) {
        boolean clicked = mouseX > (double) this.x && mouseX < (double) (this.x + this.w) && mouseY > (double) this.y && mouseY < (double) (this.y + this.h);
        if (clicked) {
            for (int t = 0; t < this.entries.length; ++t) {
                if (this.entries[t].enabled) {
                    boolean entryClicked = mouseX >= (double) this.x && mouseX <= (double) (this.x + this.w) && mouseY >= (double) (this.y + t * 20) && mouseY <= (double) (this.y + (t + 1) * 20);
                    if (entryClicked) {
                        this.shouldClose = this.entries[t].causesClose;
                        this.parentGui.popupAction(this, this.entries[t].action);
                    }
                }
            }
        }

        return clicked;
    }

    public boolean overMe(int x, int y) {
        return x > this.x && x < this.x + this.w && y > this.y && y < this.y + this.h;
    }

    public boolean shouldClose() {
        return this.shouldClose;
    }

    public void drawPopup(MatrixStack matrixStack, int mouseX, int mouseY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        GLShim.glDisable(2929);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, Screen.OPTIONS_BACKGROUND_TEXTURE);
        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var6 = 32.0F;
        vertexBuffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        vertexBuffer.vertex((double) this.x, (double) (this.y + this.h), 0.0).texture((float) this.x / var6, (float) this.y / var6).color(64, 64, 64, 255).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) (this.y + this.h), 0.0).texture((float) (this.x + this.w) / var6, (float) this.y / var6).color(64, 64, 64, 255).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) (this.y + 0), 0.0).texture((float) (this.x + this.w) / var6, (float) (this.y + this.h) / var6).color(64, 64, 64, 255).next();
        vertexBuffer.vertex((double) this.x, (double) (this.y + 0), 0.0).texture((float) this.x / var6, (float) (this.y + this.h) / var6).color(64, 64, 64, 255).next();
        tessellator.draw();
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 771);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        GLShim.glDisable(3553);
        byte fadeWidth = 4;
        vertexBuffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        vertexBuffer.vertex((double) this.x, (double) (this.y + 4), 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) (this.y + 4), 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) this.y, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex((double) this.x, (double) this.y, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex((double) this.x, (double) (this.y + this.h), 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) (this.y + this.h), 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) (this.y + this.h - 4), 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) this.x, (double) (this.y + this.h - 4), 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) this.x, (double) this.y, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex((double) this.x, (double) (this.y + this.h), 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex((double) (this.x + 4), (double) (this.y + this.h), 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) (this.x + 4), (double) this.y, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) (this.x + this.w - 4), (double) this.y, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) (this.x + this.w - 4), (double) (this.y + this.h), 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) (this.y + this.h), 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) this.y, 0.0).color(0, 0, 0, 255).next();
        tessellator.draw();
        vertexBuffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        vertexBuffer.vertex((double) (this.x + this.w - 4), (double) this.y, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) (this.x + this.w - 4), (double) (this.y + this.h), 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) (this.y + this.h), 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex((double) (this.x + this.w), (double) this.y, 0.0).color(0, 0, 0, 255).next();
        tessellator.draw();
        GLShim.glEnable(3553);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        GLShim.glDisable(3042);

        for (int t = 0; t < this.entries.length; ++t) {
            int color = !this.entries[t].enabled ? 10526880 : (mouseX >= this.x && mouseX <= this.x + this.w && mouseY >= this.y + t * 20 && mouseY <= this.y + (t + 1) * 20 ? 16777120 : 14737632);
            this.fontRendererObj.drawWithShadow(matrixStack, this.entries[t].name, (float) (this.x + this.padding), (float) (this.y + this.padding + t * 20), color);
        }

    }

    public static class PopupEntry {
        public String name;
        public int action;
        boolean causesClose;
        boolean enabled;

        public PopupEntry(String name, int action, boolean causesClose, boolean enabled) {
            this.name = name;
            this.action = action;
            this.causesClose = causesClose;
            this.enabled = enabled;
        }
    }
}
