package com.createchance.imageeditor.shaders;

import android.opengl.GLES20;

/**
 * Water drop transition shader.
 *
 * @author createchance
 * @date 2019/1/1
 */
public class WaterDropTransShader extends TransitionMainFragmentShader {
    private final String TRANS_SHADER = "WaterDrop.glsl";

    private final String U_AMPLITUDE = "amplitude";
    private final String U_SPEED = "speed";

    public WaterDropTransShader() {
        initShader(new String[]{TRANSITION_FOLDER + BASE_SHADER, TRANSITION_FOLDER + TRANS_SHADER}, GLES20.GL_FRAGMENT_SHADER);
    }

    @Override
    public void initLocation(int programId) {
        super.initLocation(programId);
        addLocation(U_AMPLITUDE, true);
        addLocation(U_SPEED, true);
        loadLocation(programId);
    }

    public void setUAmplitude(float amplitude) {
        setUniform(U_AMPLITUDE, amplitude);
    }

    public void setUSpeed(float speed) {
        setUniform(U_SPEED, speed);
    }
}
