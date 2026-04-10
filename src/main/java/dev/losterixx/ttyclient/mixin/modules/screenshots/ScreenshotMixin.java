package dev.losterixx.ttyclient.mixin.modules.screenshots;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import dev.losterixx.ttyclient.client.modules.screenshots.ScreenshotManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotMixin {

    @Inject(
            method = "grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onGrab(File gameDirectory, RenderTarget renderTarget, Consumer<Component> messageReceiver, CallbackInfo ci) {
        if (!ScreenshotManager.INSTANCE.getConfig().getEnabled()) return;

        ci.cancel();

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().player.sendOverlayMessage(Component.literal("§7Taking screenshot..."));
        }

        Screenshot.takeScreenshot(renderTarget, (NativeImage image) -> {
            Thread thread = new Thread(() -> {
                File screenshotsDir = new File(gameDirectory, "screenshots");
                screenshotsDir.mkdirs();

                File file = ttyGetUniqueFile(screenshotsDir);

                try {
                    int[] targetSize = ScreenshotManager.INSTANCE.calculateTargetSize(image.getWidth(), image.getHeight());

                    if (targetSize == null) {
                        image.writeToFile(file);
                    } else {
                        ttyScaleAndSave(image, file, targetSize[0], targetSize[1]);
                    }

                    ScreenshotManager.INSTANCE.sendScreenshotMessage(file);

                } catch (Exception e) {
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> {
                        if (mc.player != null) {
                            mc.player.sendSystemMessage(Component.literal("§cFailed to save screenshot: " + e.getMessage()));
                        }
                    });
                } finally {
                    image.close();
                }
            });

            thread.setDaemon(true);
            thread.setName("TTYClient-ScreenshotSaver");
            thread.start();
        });
    }

    @Unique
    private static void ttyScaleAndSave(NativeImage image, File outputFile, int targetW, int targetH) throws Exception {
        Path tempFile = Files.createTempFile("tty_client_screenshot_", ".png");
        try {
            image.writeToFile(tempFile);

            BufferedImage srcImg = ImageIO.read(tempFile.toFile());
            if (srcImg == null) throw new Exception("Could not decode screenshot for scaling");

            BufferedImage destImg = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = destImg.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(srcImg, 0, 0, targetW, targetH, null);
            g2d.dispose();

            ImageIO.write(destImg, "png", outputFile);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Unique
    private static File ttyGetUniqueFile(File dir) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File file = new File(dir, timestamp + ".png");

        int idx = 1;
        while (file.exists()) {
            idx++;
            file = new File(dir, timestamp + "_" + idx + ".png");
        }

        return file;
    }
}
