/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # TintIndexBakedModel.java
 * - A `BakedModel` wrapper that applies a specific `tintIndex` on all returned quads.
 * - Used to tint models that don't already have the desired tint index in their baked quads (e.g. ring tint index = 1).
 */

package corvaeoboro.botania_pylon_crystal;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TintIndexBakedModel implements BakedModel {
	private final BakedModel delegate;
	private final int tintIndex;

	public TintIndexBakedModel(BakedModel delegate, int tintIndex) {
		this.delegate = delegate;
		this.tintIndex = tintIndex;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
		List<BakedQuad> base = delegate.getQuads(state, side, rand);
		if (base.isEmpty()) {
			return base;
		}
		List<BakedQuad> out = new ArrayList<>(base.size());
		for (BakedQuad q : base) {
			out.add(reTint(q, tintIndex));
		}
		return out;
	}

	private static BakedQuad reTint(BakedQuad q, int tintIndex) {
		int[] vertices = q.getVertices();
		int[] vertsCopy = new int[vertices.length];
		System.arraycopy(vertices, 0, vertsCopy, 0, vertices.length);
		TextureAtlasSprite sprite = q.getSprite();
		return new BakedQuad(vertsCopy, tintIndex, q.getDirection(), sprite, q.isShade());
	}

	@Override
	public boolean useAmbientOcclusion() {
		return delegate.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return delegate.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return delegate.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return delegate.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return delegate.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return delegate.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return delegate.getOverrides();
	}
}
