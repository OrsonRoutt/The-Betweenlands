package thebetweenlands.client.render.models.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import thebetweenlands.common.block.terrain.BlockLifeCrystalOre;
import thebetweenlands.common.lib.ModInfo;
import thebetweenlands.util.StalactiteHelper;

public class LifeCrystalOreModel implements IModel {
	public static final ResourceLocation TEXTURE_BACKGROUND = new ResourceLocation(ModInfo.ID, "blocks/life_crystal_ore_background");
	public static final ResourceLocation TEXTURE_ORE = new ResourceLocation(ModInfo.ID, "blocks/life_crystal_ore");

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Collections.unmodifiableCollection(Arrays.asList(new ResourceLocation[]{TEXTURE_BACKGROUND, TEXTURE_ORE}));
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return new LifeCrystalOreBakedModel(format, bakedTextureGetter.apply(TEXTURE_BACKGROUND), bakedTextureGetter.apply(TEXTURE_ORE));
	}

	@Override
	public IModelState getDefaultState() {
		return TRSRTransformation.identity();
	}

	public static class LifeCrystalOreBakedModel implements IBakedModel {
		private final VertexFormat format;
		private final TextureAtlasSprite textureBackground;
		private final TextureAtlasSprite textureOre;

		private LifeCrystalOreBakedModel(VertexFormat format, TextureAtlasSprite textureBackground, TextureAtlasSprite textureOre) {
			this.format = format;
			this.textureBackground = textureBackground;
			this.textureOre = textureOre;
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState stateOld, EnumFacing side, long rand) {
			if(side != null)
				return new ArrayList<BakedQuad>();

			IExtendedBlockState state = (IExtendedBlockState) stateOld;

			int distUp = state.getValue(BlockLifeCrystalOre.DIST_UP);
			int distDown = state.getValue(BlockLifeCrystalOre.DIST_DOWN);
			boolean noTop = state.getValue(BlockLifeCrystalOre.NO_TOP);
			boolean noBottom = state.getValue(BlockLifeCrystalOre.NO_BOTTOM);
			int randX = state.getValue(BlockLifeCrystalOre.POS_X);
			int height = state.getValue(BlockLifeCrystalOre.POS_Y);
			int randZ = state.getValue(BlockLifeCrystalOre.POS_Z);

			List<BakedQuad> quads = new ArrayList<>();


			int totalHeight = 1 + distDown + distUp;
			float distToMidBottom, distToMidTop;

			double squareAmount = 1.2D;
			double halfTotalHeightSQ;

			if(noTop) {
				halfTotalHeightSQ = Math.pow(totalHeight, squareAmount);
				distToMidBottom = Math.abs(distUp + 1);
				distToMidTop = Math.abs(distUp);
			} else if(noBottom) {
				halfTotalHeightSQ = Math.pow(totalHeight, squareAmount);
				distToMidBottom = Math.abs(distDown);
				distToMidTop = Math.abs(distDown + 1);
			} else {
				float halfTotalHeight = totalHeight * 0.5F;
				halfTotalHeightSQ = Math.pow(halfTotalHeight, squareAmount);
				distToMidBottom = Math.abs(halfTotalHeight - distUp - 1);
				distToMidTop = Math.abs(halfTotalHeight - distUp);
			}

			int minValBottom = (noBottom && distDown == 0) ? 0 : 1;
			int minValTop = (noTop && distUp == 0) ? 0 : 1;
			int scaledValBottom = (int) (Math.pow(distToMidBottom, squareAmount) / halfTotalHeightSQ * (8 - minValBottom)) + minValBottom;
			int scaledValTop = (int) (Math.pow(distToMidTop, squareAmount) / halfTotalHeightSQ * (8 - minValTop)) + minValTop;

			float umin = 0;
			float umax = 16;
			float vmin = 0;
			float vmax = 16;

			float halfSize = (float) scaledValBottom / 16;
			float halfSizeTexW = halfSize * (umax - umin);
			float halfSize1 = (float) (scaledValTop) / 16;
			float halfSizeTex1 = halfSize1 * (umax - umin);

			StalactiteHelper core = StalactiteHelper.getValsFor(randX, height, randZ);


			QuadBuilder builder = new QuadBuilder(this.format).setSwitchUV(true);

			for(int i = 0; i < 2; i++) {
				if(i == 0) 
					builder.setSprite(this.textureBackground);
				else if(i == 1)
					builder.setSprite(this.textureOre);

				// front
				builder.addVertex(core.bX - halfSize, 0, core.bZ - halfSize, umin + halfSizeTexW * 2, vmax);
				builder.addVertex(core.bX - halfSize, 0, core.bZ + halfSize, umin, vmax);
				builder.addVertex(core.tX - halfSize1, 1, core.tZ + halfSize1, umin, vmin);
				builder.addVertex(core.tX - halfSize1, 1, core.tZ - halfSize1, umin + halfSizeTex1 * 2, vmin);
				// back
				builder.addVertex(core.bX + halfSize, 0, core.bZ + halfSize, umin + halfSizeTexW * 2, vmax);
				builder.addVertex(core.bX + halfSize, 0, core.bZ - halfSize, umin, vmax);
				builder.addVertex(core.tX + halfSize1, 1, core.tZ - halfSize1, umin, vmin);
				builder.addVertex(core.tX + halfSize1, 1, core.tZ + halfSize1, umin + halfSizeTex1 * 2, vmin);
				// left
				builder.addVertex(core.bX + halfSize, 0, core.bZ - halfSize, umin + halfSizeTexW * 2, vmax);
				builder.addVertex(core.bX - halfSize, 0, core.bZ - halfSize, umin, vmax);
				builder.addVertex(core.tX - halfSize1, 1, core.tZ - halfSize1, umin, vmin);
				builder.addVertex(core.tX + halfSize1, 1, core.tZ - halfSize1, umin + halfSizeTex1 * 2, vmin);
				// right
				builder.addVertex(core.bX - halfSize, 0, core.bZ + halfSize, umin + halfSizeTexW * 2, vmax);
				builder.addVertex(core.bX + halfSize, 0, core.bZ + halfSize, umin, vmax);
				builder.addVertex(core.tX + halfSize1, 1, core.tZ + halfSize1, umin, vmin);
				builder.addVertex(core.tX - halfSize1, 1, core.tZ + halfSize1, umin + halfSizeTex1 * 2, vmin);

				// top
				if(distUp == 0) {
					builder.addVertex(core.tX - halfSize1, 1, core.tZ - halfSize1, umin, vmin);
					builder.addVertex(core.tX - halfSize1, 1, core.tZ + halfSize1, umin + halfSizeTex1 * 2, vmin);
					builder.addVertex(core.tX + halfSize1, 1, core.tZ + halfSize1, umin + halfSizeTex1 * 2, vmin + halfSizeTex1 * 2);
					builder.addVertex(core.tX + halfSize1, 1, core.tZ - halfSize1, umin, vmin + halfSizeTex1 * 2);
				}

				// bottom
				if(distDown == 0) {
					builder.addVertex(core.bX - halfSize, 0, core.bZ + halfSize, umin + halfSizeTexW * 2, vmin);
					builder.addVertex(core.bX - halfSize, 0, core.bZ - halfSize, umin, vmin);
					builder.addVertex(core.bX + halfSize, 0, core.bZ - halfSize, umin, vmin + halfSizeTexW * 2);
					builder.addVertex(core.bX + halfSize, 0, core.bZ + halfSize, umin + halfSizeTexW * 2, vmin + halfSizeTexW * 2);
				}
			}

			quads.addAll(builder.build());

			return quads;
		}

		@Override
		public boolean isAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return this.textureBackground;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides() {
			return null;
		}
	}
}
