package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionSliderMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class GuiPersistentMapOptions extends GuiScreenMinimap {
   private final Screen parent;
   private static EnumOptionsMinimap[] relevantOptions;
   private final PersistentMapSettingsManager options;
   private final Text screenTitle = new TranslatableText("options.worldmap.title");
   private final Text cacheSettings = new TranslatableText("options.worldmap.cachesettings");
   private final Text warning = new TranslatableText("options.worldmap.warning");
   private static EnumOptionsMinimap[] relevantOptions2;

   public GuiPersistentMapOptions(Screen parent, IVoxelMap master) {
      this.parent = parent;
      this.options = master.getPersistentMapOptions();
   }

   public void init() {
      relevantOptions = new EnumOptionsMinimap[]{EnumOptionsMinimap.SHOWWAYPOINTS, EnumOptionsMinimap.SHOWWAYPOINTNAMES};
      int var2 = 0;

      for(int t = 0; t < relevantOptions.length; ++t) {
         EnumOptionsMinimap option = relevantOptions[t];
         this.addDrawableChild(
            new GuiOptionButtonMinimap(
               this.getWidth() / 2 - 155 + var2 % 2 * 160,
               this.getHeight() / 6 + 24 * (var2 >> 1),
               option,
               new LiteralText(this.options.getKeyText(option)),
               buttonx -> this.optionClicked(buttonx)
            )
         );
         ++var2;
      }

      relevantOptions2 = new EnumOptionsMinimap[]{EnumOptionsMinimap.MINZOOM, EnumOptionsMinimap.MAXZOOM, EnumOptionsMinimap.CACHESIZE};
      var2 += 2;

      for(int t = 0; t < relevantOptions2.length; ++t) {
         EnumOptionsMinimap option = relevantOptions2[t];
         if (option.isFloat()) {
            float sValue = this.options.getOptionFloatValue(option);
            float fValue = 0.0F;

            this.addDrawableChild(
               new GuiOptionSliderMinimap(
                  this.getWidth() / 2 - 155 + var2 % 2 * 160,
                  this.getHeight() / 6 + 24 * (var2 >> 1),
                  option,
                  switch(option) {
                     case MINZOOM -> (sValue - -3.0F) / (float)(5 - -3);
                     case MAXZOOM -> (sValue - -3.0F) / (float)(5 - -3);
                     case CACHESIZE -> sValue / 5000.0F;
                     default -> throw new IllegalArgumentException(
                     "Add code to handle EnumOptionMinimap: " + option.getName() + ". (possibly not a float value applicable to persistent map)"
                  );
                  },
                  this.options
               )
            );
         } else {
            this.addDrawableChild(
               new GuiOptionButtonMinimap(
                  this.getWidth() / 2 - 155 + var2 % 2 * 160,
                  this.getHeight() / 6 + 24 * (var2 >> 1),
                  option,
                  new LiteralText(this.options.getKeyText(option)),
                  buttonx -> this.optionClicked(buttonx)
               )
            );
         }

         ++var2;
      }

      this.addDrawableChild(
         new ButtonWidget(
            this.getWidth() / 2 - 100,
            this.getHeight() / 6 + 168,
            200,
            20,
            new TranslatableText("gui.done"),
            buttonx -> this.getMinecraft().setScreen(this.parent)
         )
      );

      for(Object buttonObj : this.getButtonList()) {
         if (buttonObj instanceof GuiOptionButtonMinimap) {
            GuiOptionButtonMinimap button = (GuiOptionButtonMinimap)buttonObj;
            if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWWAYPOINTNAMES)) {
               button.active = this.options.showWaypoints;
            }
         }
      }

   }

   protected void optionClicked(ButtonWidget par1GuiButton) {
      EnumOptionsMinimap option = ((GuiOptionButtonMinimap)par1GuiButton).returnEnumOptions();
      this.options.setOptionValue(option);
      par1GuiButton.setMessage(new LiteralText(this.options.getKeyText(option)));

      for(Object buttonObj : this.getButtonList()) {
         if (buttonObj instanceof GuiOptionButtonMinimap) {
            GuiOptionButtonMinimap button = (GuiOptionButtonMinimap)buttonObj;
            if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWWAYPOINTNAMES)) {
               button.active = this.options.showWaypoints;
            }
         }
      }

   }

   public void render(MatrixStack matrixStack, int par1, int par2, float par3) {
      for(Object buttonObj : this.getButtonList()) {
         if (buttonObj instanceof GuiOptionSliderMinimap) {
            GuiOptionSliderMinimap slider = (GuiOptionSliderMinimap)buttonObj;
            EnumOptionsMinimap option = slider.returnEnumOptions();
            float sValue = this.options.getOptionFloatValue(option);
            float fValue = 0.0F;

            fValue = switch(option) {
               case MINZOOM -> (sValue - -3.0F) / (float)(5 - -3);
               case MAXZOOM -> (sValue - -3.0F) / (float)(5 - -3);
               case CACHESIZE -> sValue / 5000.0F;
               default -> throw new IllegalArgumentException(
               "Add code to handle EnumOptionMinimap: " + option.getName() + ". (possibly not a float value applicable to persistent map)"
            );
            };
            if (this.getFocused() != slider) {
               slider.setValue(fValue);
            }
         }
      }

      super.drawMap(matrixStack);
      this.renderBackground(matrixStack);
      drawCenteredText(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
      drawCenteredText(matrixStack, this.getFontRenderer(), this.cacheSettings, this.getWidth() / 2, this.getHeight() / 6 + 24, 16777215);
      drawCenteredText(matrixStack, this.getFontRenderer(), this.warning, this.getWidth() / 2, this.getHeight() / 6 + 34, 16777215);
      super.render(matrixStack, par1, par2, par3);
   }
}
