package com.createchance.imageeditor.ops;

import android.opengl.GLES20;

import com.createchance.imageeditor.drawers.GammaAdjustDrawer;

/**
 * Gamma adjust operator.
 *
 * @author createchance
 * @date 2018/11/17
 */
public class GammaAdjustOperator extends AbstractOperator {

    private static final String TAG = "GammaAdjustOperator";

    private final float MIN_GAMMA = 0f;
    private final float MAX_GAMMA = 4f;

    private float mGamma = 0.0f;

    private GammaAdjustDrawer mDrawer;

    private GammaAdjustOperator() {
        super(GammaAdjustOperator.class.getSimpleName(), OP_GAMMA);
    }

    @Override
    public boolean checkRational() {
        return mGamma >= MIN_GAMMA && mGamma <= MAX_GAMMA;
    }

    @Override
    public void exec() {
        mWorker.bindOffScreenFrameBuffer(mWorker.getTextures()[mWorker.getOutputTextureIndex()]);
        if (mDrawer == null) {
            mDrawer = new GammaAdjustDrawer();
        }
        mDrawer.setGamma(mGamma);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(mWorker.getImgShowLeft(),
                mWorker.getImgShowBottom(),
                mWorker.getImgShowWidth(),
                mWorker.getImgShowHeight());
        mDrawer.draw(mWorker.getTextures()[mWorker.getInputTextureIndex()],
                0,
                0,
                mWorker.getSurfaceWidth(),
                mWorker.getSurfaceHeight());
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        mWorker.bindDefaultFrameBuffer();
        mWorker.swapTexture();
    }

    public float getGamma() {
        return mGamma;
    }

    public void setGamma(float gamma) {
        this.mGamma = gamma;
    }

    public static class Builder {
        private GammaAdjustOperator operator = new GammaAdjustOperator();

        public Builder gamma(float gamma) {
            operator.mGamma = gamma;

            return this;
        }

        public GammaAdjustOperator build() {
            return operator;
        }
    }
}
