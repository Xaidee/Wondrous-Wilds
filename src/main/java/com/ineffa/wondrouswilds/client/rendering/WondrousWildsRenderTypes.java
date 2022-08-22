package com.ineffa.wondrouswilds.client.rendering;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class WondrousWildsRenderTypes extends RenderType {

    public WondrousWildsRenderTypes(String name, VertexFormat vertexFormat, VertexFormat.Mode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    private static final Function<ResourceLocation, RenderType> TRANSLUCENT_GLOW = Util.memoize(texture -> {
        RenderStateShard.TextureStateShard texture2 = new RenderStateShard.TextureStateShard(texture, false, false);
        return RenderType.create(WondrousWilds.MOD_ID + ":translucent_glow", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder().setShaderState(RENDERTYPE_EYES_SHADER).setTextureState(texture2).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    });

    public static RenderType getTranslucentGlow(ResourceLocation texture) {
        return TRANSLUCENT_GLOW.apply(texture);
    }
}
