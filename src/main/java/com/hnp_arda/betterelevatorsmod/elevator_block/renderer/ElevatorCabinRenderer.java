package com.hnp_arda.betterelevatorsmod.elevator_block.renderer;

import com.hnp_arda.betterelevatorsmod.elevator_block.entity.ElevatorCabinEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ElevatorCabinRenderer extends EntityRenderer<ElevatorCabinEntity> {

    public ElevatorCabinRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(ElevatorCabinEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.getCollisionLayer() != ElevatorCabinEntity.COLLISION_LAYER_MAIN) {
            return;
        }
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.pushPose();

        int width = pEntity.getWidth();
        int depth = pEntity.getDepth();

        // Draw a simple box representing the cabin platform
        float halfWidth = width / 2.0f;
        float halfDepth = depth / 2.0f;
        float height = ElevatorCabinEntity.PLATFORM_HEIGHT;

        VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.lines());
        Matrix4f matrix = pPoseStack.last().pose();

        // Draw platform outline (just edges for now)
        drawBox(matrix, vertexConsumer, -halfWidth, 0, -halfDepth, halfWidth, height, halfDepth);

        // Draw cabin walls (2 blocks high)
        float wallHeight = pEntity.getWallHeight();
        drawWallOutline(matrix, vertexConsumer, -halfWidth, height, -halfDepth, halfWidth, wallHeight, halfDepth);

        pPoseStack.popPose();
    }

    private void drawBox(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        // Bottom edges
        consumer.addVertex(matrix, minX, minY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, maxX, minY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, minX, minY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, minY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        // Top edges
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        // Vertical edges
        consumer.addVertex(matrix, minX, minY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, maxX, minY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, minX, minY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
    }

    private void drawWallOutline(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        // Draw vertical lines for cabin walls
        consumer.addVertex(matrix, minX, minY, minZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, maxX, minY, minZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);

        consumer.addVertex(matrix, minX, minY, maxZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
    
        // Draw top edge lines
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
    
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
    
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
    
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(200, 200, 200, 255).setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(ElevatorCabinEntity pEntity) {
        return null; // We're using lines rendering, no texture needed
    }
}
