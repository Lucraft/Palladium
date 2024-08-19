package net.threetag.palladium.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.threetag.palladium.client.renderer.entity.HumanoidRendererModifications;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.awt.*;

@Mixin(GeoRenderer.class)
public interface GeoRendererMixin {

    /**
     * @author Lucraft
     * @reason Used to globally change alpha
     */
    @Overwrite
    default void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, VertexConsumer buffer,
                                      int packedLight, int packedOverlay, int colour) {
        if (HumanoidRendererModifications.ALPHA_MULTIPLIER < 1F) {
            var c = new Color(colour, true);
            colour = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * HumanoidRendererModifications.ALPHA_MULTIPLIER)).getRGB();
        }

        for (GeoVertex vertex : quad.vertices()) {
            Vector3f position = vertex.position();
            Vector4f vector4f = poseState.transform(new Vector4f(position.x(), position.y(), position.z(), 1.0f));

            buffer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), colour, vertex.texU(),
                    vertex.texV(), packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
        }
    }

}
