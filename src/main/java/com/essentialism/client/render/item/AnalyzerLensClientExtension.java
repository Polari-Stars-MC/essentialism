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

    /** Pre-computed unit-circle trigonometry for 120-segment circles. */
    private static final float[] CIRCLE_120_TRIG = precomputeTrig(120);
    /** Pre-computed unit-circle trigonometry for 96-segment circles. */
    private static final float[] CIRCLE_96_TRIG = precomputeTrig(96);
    /** Pre-computed hexagram vertices at unit radius and rotation=0. */
    private static final Point[] HEXAGRAM_BASE = precomputeHexagram(1.0F);

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
            renderSigil(event.getPoseStack(), consumer, camera, sigil, minecraft.level.getGameTime());
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
        ItemStack lens;
        if (mainHand.is(EItems.ANALYZERS_LENS.get())) {
            lens = mainHand;
        } else if (offHand.is(EItems.ANALYZERS_LENS.get())) {
            lens = offHand;
        } else {
            lens = ItemStack.EMPTY;
        }
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
        ), minecraft.level.getGameTime());
    }

    private static void renderSigil(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, ActiveSigil sigil, long gameTime) {
        double time = ((gameTime % 360000L) / 1000.0D);
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
        float cosRot = Mth.cos((float) Math.toRadians(rotationDegrees));
        float sinRot = Mth.sin((float) Math.toRadians(rotationDegrees));

        // Transform all 6 vertices once
        float[] x = new float[6];
        float[] z = new float[6];
        for (int i = 0; i < 6; i++) {
            x[i] = (HEXAGRAM_BASE[i].x * cosRot - HEXAGRAM_BASE[i].z * sinRot) * radius;
            z[i] = (HEXAGRAM_BASE[i].x * sinRot + HEXAGRAM_BASE[i].z * cosRot) * radius;
        }

        // First triangle (points 0,2,4)
        addLine(poseStack, consumer, x[0], z[0], x[2], z[2], color, lineWidth);
        addLine(poseStack, consumer, x[2], z[2], x[4], z[4], color, lineWidth);
        addLine(poseStack, consumer, x[4], z[4], x[0], z[0], color, lineWidth);
        // Second triangle (points 1,3,5)
        addLine(poseStack, consumer, x[1], z[1], x[3], z[3], color, lineWidth);
        addLine(poseStack, consumer, x[3], z[3], x[5], z[5], color, lineWidth);
        addLine(poseStack, consumer, x[5], z[5], x[1], z[1], color, lineWidth);
        // Outer edges
        for (int i = 0; i < 6; i++) {
            int ni = (i + 1) % 6;
            addLine(poseStack, consumer, x[i], z[i], x[ni], z[ni], color & 0x66FFFFFF, 1.8F);
        }
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
        // Use pre-computed unit-circle vertices for common segment counts
        float[] trig = getUnitCircle(segments);
        int offset = (int) Math.round(rotationDegrees / (360.0D / segments)) % segments;
        if (offset < 0) offset += segments;

        for (int i = 0; i < segments; i++) {
            int idx = (i + offset) % segments;
            int nextIdx = (i + offset + 1) % segments;
            float x1 = trig[idx * 2] * radius;
            float z1 = trig[idx * 2 + 1] * radius;
            float x2 = trig[nextIdx * 2] * radius;
            float z2 = trig[nextIdx * 2 + 1] * radius;
            addLine(poseStack, consumer, x1, z1, x2, z2, color, lineWidth);
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

    private static float[] getUnitCircle(int segments) {
        return segments == 120 ? CIRCLE_120_TRIG : CIRCLE_96_TRIG;
    }

    private static float[] precomputeTrig(int segments) {
        float[] trig = new float[segments * 2];
        for (int i = 0; i < segments; i++) {
            double angle = Math.toRadians(360.0D * i / segments);
            trig[i * 2] = Mth.cos((float) angle);
            trig[i * 2 + 1] = Mth.sin((float) angle);
        }
        return trig;
    }

    private static Point[] precomputeHexagram(float radius) {
        return regularPolygon(radius, 6, 0.0D);
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
