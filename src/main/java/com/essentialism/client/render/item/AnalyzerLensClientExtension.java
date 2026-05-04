package com.essentialism.client.render.item;

import com.essentialism.Essentialism;
import com.essentialism.content.item.AnalyzerLensItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.essentialism.init.EItems;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class AnalyzerLensClientExtension {
    public static final AnalyzerLensClientExtension INSTANCE = new AnalyzerLensClientExtension();
    private final List<ActiveSigil> activeSigils = new ArrayList<>();

    private AnalyzerLensClientExtension() {}

    public void addSigil(double x, double y, double z, float radius, int color, int durationTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        long expireGameTime = minecraft.level.getGameTime() + durationTicks;
        this.activeSigils.add(new ActiveSigil(x, y, z, radius, color, expireGameTime));
    }

    public void renderWorld(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderTypes.linesTranslucent());
        renderLocalPreview(minecraft, event.getPoseStack(), consumer, camera);
        Iterator<ActiveSigil> iterator = this.activeSigils.iterator();
        while (iterator.hasNext()) {
            ActiveSigil sigil = iterator.next();
            if (sigil.expireGameTime <= minecraft.level.getGameTime()) {
                iterator.remove();
                continue;
            }
            renderSigil(event.getPoseStack(), consumer, camera, sigil);
        }
        buffers.endBatch(RenderTypes.linesTranslucent());
    }

    private void renderLocalPreview(
            Minecraft minecraft,
            PoseStack poseStack,
            VertexConsumer consumer,
            Vec3 camera
    ) {
        ItemStack mainHand = minecraft.player.getMainHandItem();
        ItemStack offHand = minecraft.player.getOffhandItem();
        ItemStack lens = mainHand.is(EItems.ANALYZERS_LENS.get()) ? mainHand : offHand.is(EItems.ANALYZERS_LENS.get()) ? offHand : ItemStack.EMPTY;
        if (lens.isEmpty()) {
            return;
        }

        Entity cameraEntity = minecraft.getCameraEntity() != null ? minecraft.getCameraEntity() : minecraft.player;
        HitResult hitResult = minecraft.player.raycastHitResult(
                minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true),
                cameraEntity
        );
        if (hitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        if (!Essentialism.ESSENCE_CONFIG.isPlayerEssence
                && hitResult instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof Player) {
            return;
        }

        Vec3 focus = hitResult.getLocation();
        double groundY = AnalyzerLensItem.resolveGroundY(minecraft.level, focus);
        if (Double.isNaN(groundY)) {
            return;
        }

        renderSigil(poseStack, consumer, camera, new ActiveSigil(
                focus.x,
                groundY,
                focus.z,
                1.85F,
                lens.hasFoil() ? 0xD0FFB000 : 0xC040F0FF,
                Long.MAX_VALUE
        ));
    }

    private static void renderSigil(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, ActiveSigil sigil) {
        double time = (System.currentTimeMillis() % 360000L) / 1000.0D;
        double outerRotation = time * 18.0D;
        double innerRotation = -time * 32.0D;
        double starRotation = time * 24.0D;
        float innerRadius = sigil.radius * 0.78F;
        float starRadius = sigil.radius * 0.88F;

        poseStack.pushPose();
        poseStack.translate(sigil.x - camera.x, sigil.y - camera.y + 0.02D, sigil.z - camera.z);
        drawCircle(poseStack, consumer, sigil.radius, sigil.color, 120, outerRotation, 3.2F);
        drawCircle(poseStack, consumer, innerRadius, sigil.color & 0x88FFFFFF, 96, innerRotation, 2.2F);
        drawHexagram(poseStack, consumer, starRadius, sigil.color, starRotation, 3.2F);
        poseStack.popPose();
    }

    private static void drawHexagram(
            PoseStack poseStack,
            VertexConsumer consumer,
            float radius,
            int color,
            double rotationDegrees,
            float lineWidth
    ) {
        Point[] points = regularPolygon(radius, 6, rotationDegrees);
        drawTriangle(poseStack, consumer, points[0], points[2], points[4], color, lineWidth);
        drawTriangle(poseStack, consumer, points[1], points[3], points[5], color, lineWidth);
        for (int i = 0; i < points.length; i++) {
            Point current = points[i];
            Point next = points[(i + 1) % points.length];
            addLine(poseStack, consumer, current.x, current.z, next.x, next.z, color & 0x66FFFFFF, 1.8F);
        }
    }

    private static void drawTriangle(
            PoseStack poseStack,
            VertexConsumer consumer,
            Point a,
            Point b,
            Point c,
            int color,
            float lineWidth
    ) {
        addLine(poseStack, consumer, a.x, a.z, b.x, b.z, color, lineWidth);
        addLine(poseStack, consumer, b.x, b.z, c.x, c.z, color, lineWidth);
        addLine(poseStack, consumer, c.x, c.z, a.x, a.z, color, lineWidth);
    }

    private static void drawCircle(
            PoseStack poseStack,
            VertexConsumer consumer,
            float radius,
            int color,
            int segments,
            double rotationDegrees,
            float lineWidth
    ) {
        Point[] points = regularPolygon(radius, segments, rotationDegrees);
        for (int i = 0; i < points.length; i++) {
            Point current = points[i];
            Point next = points[(i + 1) % points.length];
            addLine(poseStack, consumer, current.x, current.z, next.x, next.z, color, lineWidth);
        }
    }

    private static Point[] regularPolygon(float radius, int sides, double startAngleDegrees) {
        Point[] points = new Point[sides];
        for (int i = 0; i < sides; i++) {
            double angle = Math.toRadians(startAngleDegrees + (360.0D * i / sides));
            float x = Mth.cos((float) angle) * radius;
            float z = Mth.sin((float) angle) * radius;
            points[i] = new Point(x, z);
        }
        return points;
    }

    private static void addLine(
            PoseStack poseStack,
            VertexConsumer consumer,
            float x1,
            float z1,
            float x2,
            float z2,
            int color,
            float lineWidth
    ) {
        int a = color >>> 24 & 255;
        int r = color >>> 16 & 255;
        int g = color >>> 8 & 255;
        int b = color & 255;
        consumer.addVertex(poseStack.last(), x1, 0.0F, z1).setColor(r, g, b, a).setNormal(0.0F, 1.0F, 0.0F).setLineWidth(lineWidth);
        consumer.addVertex(poseStack.last(), x2, 0.0F, z2).setColor(r, g, b, a).setNormal(0.0F, 1.0F, 0.0F).setLineWidth(lineWidth);
    }

    private record Point(float x, float z) {}

    private record ActiveSigil(double x, double y, double z, float radius, int color, long expireGameTime) {}
}
