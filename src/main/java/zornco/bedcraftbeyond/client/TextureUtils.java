package zornco.bedcraftbeyond.client;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import zornco.bedcraftbeyond.BedCraftBeyond;

public class TextureUtils {

    /*
     * Thanks to BluSunrize/TTFTCUTS
     */
    static HashMap<String, ResourceLocation> textureMap = new HashMap<String, ResourceLocation>();

    public static ResourceLocation getResource(String path) {
        ResourceLocation rl = textureMap.containsKey(path) ? textureMap.get(path) : new ResourceLocation(path);
        if (!textureMap.containsKey(path))
            textureMap.put(path, rl);
        return rl;
    }

    public static BufferedImage getImageForResource(ResourceLocation resource) throws IOException {
        InputStream layer = Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream();
        return ImageIO.read(layer);
    }

    @SuppressWarnings("deprecation")
    public static int getItemTextureColor(ItemStack item) throws Exception {
        //= item.getSpriteNumber()==1?TextureMap.locationItemsTexture:TextureMap.locationBlocksTexture;
        try {

            Block b = Block.getBlockFromItem(item.getItem());
            TextureAtlasSprite icon = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                .getModelForState(Block.getBlockFromItem(item.getItem())
                    .getStateFromMeta(item.getItemDamage()))
                        .getParticleTexture();

            if (!(icon instanceof TextureAtlasSprite))
                throw new Exception("Icon for the itemstack is not a valid sprite.");
            if (icon.getIconName().equalsIgnoreCase("missingno"))
                throw new Exception("Icon does not have a valid sprite- seems to be missing.");

            String iconName = icon.getIconName();
            iconName = iconName.substring(0, Math.max(0, iconName.indexOf(":") + 1)) + "textures/" + iconName.substring(Math.max(0, iconName.indexOf(":") + 1)) + ".png";
            ResourceLocation resource = getResource(iconName);
            BufferedImage buffered = getImageForResource(resource);

            int colour = getAverageTextureColor(buffered, 0, 0, icon.getIconWidth(), icon.getIconHeight()).getRGB();
            // colour = colour & 0xffffff; // NOT USED?
            return colour;
        } catch (Exception e) {
            BedCraftBeyond.LOGGER.error(e);
            throw e;
        }
    }

    /*
     * Where bi is your image, (x0,y0) is your upper left coordinate, and (w,h)
     * are your width and height respectively
     */
    public static Color getAverageTextureColor(BufferedImage bi, int x0, int y0, int w, int h) {
        int x1 = x0 + w;
        int y1 = y0 + h;
        long sumr = 0, sumg = 0, sumb = 0;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                Color pixel = new Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        int num = w * h;
        return new Color((int) (sumr / num), (int) (sumg / num), (int) (sumb / num));
    }
}