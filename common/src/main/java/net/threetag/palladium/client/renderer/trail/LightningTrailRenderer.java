package net.threetag.palladium.client.renderer.trail;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.LaserRenderer;
import net.threetag.palladium.client.renderer.entity.TrailSegmentEntityRenderer;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.entity.PalladiumEntityExtension;
import net.threetag.palladium.entity.TrailSegmentEntity;
import net.threetag.palladium.util.json.GsonUtil;

import java.awt.*;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unchecked")
public class LightningTrailRenderer extends TrailRenderer<LightningTrailRenderer.Cache> {

    private final LaserRenderer laserRenderer;
    private final float spacing;
    private final int lifetime;
    private final int amount;
    private final float spreadX, spreadY;

    public LightningTrailRenderer(LaserRenderer laserRenderer, float spacing, int lifetime, int amount, float spreadX, float spreadY) {
        this.laserRenderer = laserRenderer;
        this.spacing = spacing;
        this.lifetime = lifetime;
        this.amount = amount;
        this.spreadX = spreadX;
        this.spreadY = spreadY;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TrailSegmentEntityRenderer trailRenderer, Entity livingEntity, TrailSegmentEntity<Cache> segment, float partialTick, float entityYaw) {
        if (livingEntity instanceof PalladiumEntityExtension ext) {
            var trails = ext.palladium$getTrailHandler().getTrails().get(this);
            var index = trails.indexOf(segment);

            if (index == trails.size() - 1) {
                renderSegmentWithChild(poseStack, buffer, segment, trails, partialTick, index);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private void renderSegmentWithChild(PoseStack poseStack, MultiBufferSource buffer, TrailSegmentEntity<Cache> segment, List<TrailSegmentEntity<?>> segments, float partialTick, int index) {
        if (index > 0) {
            var previousSegment = segments.get(index - 1);
            var cache = segment.cache;
            var previousC = previousSegment.cache;

            if (index == segments.size() - 1 && segment.parent.isAlive()) {
                for (int i = 0; i < cache.offsets.length; i++) {
                    var start = getOffsetPos(segment, cache.offsets[i]);
                    var end = getOffsetPos(segment.parent, cache.offsets[i]).add(segment.parent.getPosition(partialTick).subtract(segment.position()));
                    float opacity = 1F - ((segment.tickCount + partialTick) / (float) segment.lifetime);

                    poseStack.pushPose();
                    poseStack.translate(start.x, start.y, start.z);
                    this.laserRenderer
                            .length((float) start.distanceTo(end))
                            .opacityAndSizeModifier(opacity).faceAndRender(poseStack, buffer, start, end, segment.parent.tickCount + (i * 42), partialTick);
                    poseStack.popPose();
                }
            }

            if (previousC instanceof Cache previousCache && cache.offsets.length == previousCache.offsets.length && previousSegment.isAlive()) {
                for (int i = 0; i < cache.offsets.length; i++) {
                    var start = getOffsetPos(segment, cache.offsets[i]);
                    var end = getOffsetPos(previousSegment, previousCache.offsets[i]).add(previousSegment.position().subtract(segment.position()));
                    float opacity = 1F - ((segment.tickCount + partialTick) / (float) segment.lifetime);

                    poseStack.pushPose();
                    poseStack.translate(start.x, start.y, start.z);
                    this.laserRenderer
                            .length((float) start.distanceTo(end))
                            .opacityAndSizeModifier(opacity).faceAndRender(poseStack, buffer, start, end, segment.parent.tickCount + (i * 42), partialTick);
                    poseStack.popPose();
                }

                poseStack.pushPose();
                var offsetPos = previousSegment.position().subtract(segment.position());
                poseStack.translate(offsetPos.x, offsetPos.y, offsetPos.z);
                this.renderSegmentWithChild(poseStack, buffer, (TrailSegmentEntity<Cache>) previousSegment, segments, partialTick, index - 1);
                poseStack.popPose();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static Vec3 getOffsetPos(Entity segment, Vec3 offset) {
        return new Vec3(offset.x * segment.getBbWidth(), (segment.getBbHeight() / 2D) + (offset.y * segment.getBbHeight()), offset.z * segment.getBbWidth());
    }

    @Override
    public SegmentCache createCache() {
        var random = new Random();
        var offsets = new Vec3[this.amount];

        for (int i = 0; i < this.amount; i++) {
            var spacingY = (1D / this.amount) * this.spreadY;
            offsets[i] = new Vec3((random.nextDouble() - 0.5D) * this.spreadX, ((spacingY * this.amount) / -2D) + (spacingY * i) + (spacingY / 2D) + ((random.nextDouble() - 0.5F) * spacingY / 1.5D), (random.nextDouble() - 0.5D) * this.spreadX);
        }

        return new Cache(offsets);
    }

    @Override
    public float getSpacing() {
        return this.spacing;
    }

    @Override
    public int getLifetime() {
        return this.lifetime;
    }

    @Override
    public Color getColor() {
        return this.laserRenderer.getGlowColor();
    }

    public static class Cache extends SegmentCache {

        private final Vec3[] offsets;

        public Cache(Vec3[] offsets) {
            this.offsets = offsets;
        }
    }

    public static class Serializer implements TrailRendererManager.TypeSerializer {

        @Override
        public TrailRenderer<Cache> parse(JsonObject json) {
            float spacing = GsonUtil.getAsFloatMin(json, "spacing", 0.1F, 1F);
            int lifetime = GsonUtil.getAsIntMin(json, "lifetime", 1, 20);
            int amount = GsonUtil.getAsIntMin(json, "amount", 1, 10);
            float spreadX = GsonUtil.getAsFloatMin(json, "spread_x", 0F, 1F);
            float spreadY = GsonUtil.getAsFloatMin(json, "spread_y", 0F, 1F);
            return new LightningTrailRenderer(LaserRenderer.fromJson(json, 1), spacing, lifetime, amount, spreadX, spreadY);
        }

        @Override
        public void generateDocumentation(JsonDocumentationBuilder builder) {
            builder.setTitle("Lightning Trail");
            builder.setDescription("Flash-like lightning trail");

            builder.addProperty("spacing", Float.class)
                    .description("Determines the space between two trail segments")
                    .fallback(1F).exampleJson(new JsonPrimitive(1F));
            builder.addProperty("lifetime", Integer.class)
                    .description("Determines how long one trail segment stays alive (in ticks)")
                    .fallback(20).exampleJson(new JsonPrimitive(20));
            builder.addProperty("amount", Integer.class)
                    .description("Determines how many lightnings the entity will generate behind it")
                    .fallback(7).exampleJson(new JsonPrimitive(7));
            builder.addProperty("spread_x", Float.class)
                    .description("Determines the spread of a lightning position relative to the player on the X/horizontal axis. 1 means across the normal player hitbox, 0 means always in the middle.")
                    .fallback(1F).exampleJson(new JsonPrimitive(1F));
            builder.addProperty("spread_y", Float.class)
                    .description("Determines the spread of a lightning position relative to the player on the Y/vertical axis. 1 means across the normal player hitbox, 0 means always in the middle.")
                    .fallback(1F).exampleJson(new JsonPrimitive(1F));
            LaserRenderer.generateDocumentation(builder, 1, false);
        }

        @Override
        public ResourceLocation getId() {
            return Palladium.id("lightning");
        }
    }

}
