package com.createchance.imageeditordemo.panels;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.createchance.imageeditor.IEManager;
import com.createchance.imageeditor.ops.AbstractOperator;
import com.createchance.imageeditor.ops.TextOperator;
import com.createchance.imageeditordemo.R;
import com.createchance.imageeditordemo.SetTextDialog;
import com.createchance.imageeditordemo.TextFontAdapter;
import com.createchance.imageeditordemo.TextTextureAdapter;
import com.createchance.imageeditordemo.utils.AssetsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Text edit panel, add text to image.
 *
 * @author createchance
 * @date 2018/10/31
 */
public class EditTextPanel extends AbstractPanel implements
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "EditTextPanel";

    private View mTextPanel;
    private ViewGroup mSubPanelContainer;

    private View mFontPanel, mColorPanel;

    private List<TextFontAdapter.FontItem> mFontList;
    private List<TextTextureAdapter.Texture> mTextureList;

    List<AbstractOperator> mTextOpList;
    private int mCurOp;

    private float mCurTextPosX = 0.5f, mCurTextPosY = 0.5f;

    private int mLastPosX, mLastPosY;
    private GestureDetector mGestureDetector;

    private final int DEFAULT_TEXT_SIZE = 100;

    public EditTextPanel(Context context, PanelListener listener) {
        super(context, listener, TYPE_TEXT);
        mTextOpList = new ArrayList<>();
        mContext = context;

        // init panels
        mTextPanel = LayoutInflater.from(mContext).inflate(R.layout.edit_panel_text, mParent, false);
        mTextPanel.findViewById(R.id.iv_apply).setOnClickListener(this);
        mTextPanel.findViewById(R.id.iv_cancel).setOnClickListener(this);
        mTextPanel.findViewById(R.id.iv_text_modify).setOnClickListener(this);
        mTextPanel.findViewById(R.id.iv_text_font).setOnClickListener(this);
        mTextPanel.findViewById(R.id.iv_text_color_bg).setOnClickListener(this);
        mTextPanel.findViewById(R.id.iv_text_add).setOnClickListener(this);
        mSubPanelContainer = mTextPanel.findViewById(R.id.vw_text_sub_panel_container);

        initFontPanel();
        initColorPanel();

        // init gesture detector
        mGestureDetector = new GestureDetector(mContext, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                mLastPosX = (int) e.getX();
                mLastPosY = (int) e.getY();
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                moveText((int) e2.getX() - mLastPosX, (int) e2.getY() - mLastPosY);
                mLastPosX = (int) e2.getX();
                mLastPosY = (int) e2.getY();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return true;
            }
        });
        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_text_modify:
                SetTextDialog.start(mContext, new SetTextDialog.OnClickListener() {
                    @Override
                    public void onConfirm(String text) {
                        if (!mTextOpList.isEmpty()) {
                            TextOperator operator = (TextOperator) mTextOpList.get(mCurOp);
                            operator.setText(text);
                            IEManager.getInstance().updateOperator(0, mTextOpList.get(mCurOp), true);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                break;
            case R.id.iv_text_font:
                changeColor(v);
                mSubPanelContainer.removeAllViews();
                mSubPanelContainer.addView(mFontPanel);
                break;
            case R.id.iv_text_color_bg:
                changeColor(v);
                mSubPanelContainer.removeAllViews();
                mSubPanelContainer.addView(mColorPanel);
                break;
            case R.id.iv_text_add:
                SetTextDialog.start(mContext, new SetTextDialog.OnClickListener() {
                    @Override
                    public void onConfirm(String text) {
                        TextOperator textOperator = new TextOperator.Builder()
                                .text(text)
                                .color(1f, 1f, 1f)
                                .size(DEFAULT_TEXT_SIZE)
                                .font(new File(mContext.getFilesDir(), mFontList.get(0).fontPath).getAbsolutePath())
                                .position(mCurTextPosX, mCurTextPosY)
                                .build();
                        IEManager.getInstance().addOperator(0, textOperator, true);
                        mCurTextPosX += 0.1f;
                        mCurTextPosY -= 0.1f;
                        mTextOpList.add(textOperator);
                        mCurOp = mTextOpList.size() - 1;
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                break;
            case R.id.iv_apply:
                close(false);
                break;
            case R.id.iv_cancel:
                close(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void show(ViewGroup parent, int surfaceWidth, int surfaceHeight) {
        super.show(parent, surfaceWidth, surfaceHeight);
        mCurTextPosY = mSurfaceHeight / 2;
        mParent.addView(mTextPanel);

        if (mTextOpList.isEmpty()) {
            SetTextDialog.start(mContext, new SetTextDialog.OnClickListener() {
                @Override
                public void onConfirm(String text) {
                    TextOperator textOperator = new TextOperator.Builder()
                            .text(text)
                            .color(1f, 1f, 1f)
                            .size(DEFAULT_TEXT_SIZE)
                            .font(new File(mContext.getFilesDir(), mFontList.get(0).fontPath).getAbsolutePath())
                            .position(mCurTextPosX, mCurTextPosY)
                            .build();
                    IEManager.getInstance().addOperator(0, textOperator, true);
                    mCurTextPosX += 0.1f;
                    mCurTextPosY -= 0.1f;
                    mTextOpList.add(textOperator);
                    mCurOp = mTextOpList.size() - 1;
                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

    @Override
    public void close(boolean discard) {
        super.close(discard);

        if (discard && mTextOpList != null && mTextOpList.size() > 0) {
            List<AbstractOperator> tempList = new ArrayList<>();
            tempList.addAll(mTextOpList);
            if (!IEManager.getInstance().removeOperator(0, tempList, true)) {
                Log.e(TAG, "Close panel, but remove op failed!!!");
            }
            mTextOpList.clear();
        }
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
    }

    private void moveText(int deltaX, int deltaY) {
        if (mTextOpList.size() == 0) {
            return;
        }
        TextOperator textOperator = (TextOperator) mTextOpList.get(mCurOp);
        float curX = textOperator.getPosX() + deltaX * 1.0f / IEManager.getInstance().getRenderWidth(0);
        float curY = textOperator.getPosY() - deltaY * 1.0f / IEManager.getInstance().getRenderHeight(0);
        textOperator.setPosX(curX);
        textOperator.setPosY(curY);
        IEManager.getInstance().updateOperator(0, textOperator, true);
    }

    private void initFontPanel() {
        mFontPanel = LayoutInflater.from(mContext).inflate(R.layout.edit_panel_text_font, mSubPanelContainer, false);
        RecyclerView fontListView = mFontPanel.findViewById(R.id.rcv_font_list);
        fontListView.setLayoutManager(new GridLayoutManager(mContext, 5));
        // init font list.
        initFontList();
        TextFontAdapter textFontAdapter = new TextFontAdapter(mContext, mFontList, new TextFontAdapter.OnFontSelectListener() {
            @Override
            public void fontSelected(int position) {
                TextOperator textOperator = (TextOperator) mTextOpList.get(mCurOp);
                textOperator.setFontPath(new File(mContext.getFilesDir(), mFontList.get(position).fontPath).getAbsolutePath());
                IEManager.getInstance().updateOperator(0, textOperator, true);
            }
        });
        fontListView.setAdapter(textFontAdapter);

        changeColor(mTextPanel.findViewById(R.id.iv_text_font));
        mSubPanelContainer.removeAllViews();
        mSubPanelContainer.addView(mFontPanel);
    }

    private void initColorPanel() {
        mColorPanel = LayoutInflater.from(mContext).inflate(R.layout.edit_panel_text_color, mSubPanelContainer, false);
        ((SeekBar) mColorPanel.findViewById(R.id.sb_change_transparent)).setOnSeekBarChangeListener(this);
        ((SeekBar) mColorPanel.findViewById(R.id.sb_change_size)).setOnSeekBarChangeListener(this);
        ((SeekBar) mColorPanel.findViewById(R.id.sb_change_size)).setProgress(DEFAULT_TEXT_SIZE);
        ((SeekBar) mColorPanel.findViewById(R.id.sb_change_color_red)).setOnSeekBarChangeListener(this);
        ((SeekBar) mColorPanel.findViewById(R.id.sb_change_color_green)).setOnSeekBarChangeListener(this);
        ((SeekBar) mColorPanel.findViewById(R.id.sb_change_color_blue)).setOnSeekBarChangeListener(this);
        RecyclerView textureListView = mColorPanel.findViewById(R.id.rcv_text_texture_list);
        textureListView.setLayoutManager(new GridLayoutManager(mContext, 8));
        initTextTexture();
        textureListView.setAdapter(new TextTextureAdapter(mContext, mTextureList, new TextTextureAdapter.OnTextureClickListener() {
            @Override
            public void onTextureSelected(int position) {
                Log.d(TAG, "onTextureSelected: " + position);
                TextOperator textOperator = (TextOperator) mTextOpList.get(mCurOp);
                textOperator.setBackground(BitmapFactory.decodeResource(mContext.getResources(), mTextureList.get(position).resId));
                IEManager.getInstance().updateOperator(0, textOperator, true);
            }
        }));
    }

    private void initFontList() {
        mFontList = AssetsUtil.parseJsonToList(mContext, "fonts/fonts.json", TextFontAdapter.FontItem.class);
        mFontList.get(0).selected = true;
    }

    private void changeColor(View selectedView) {
        mTextPanel.findViewById(R.id.iv_text_font).setBackgroundColor(mContext.getResources().getColor(R.color.theme_dark));
        mTextPanel.findViewById(R.id.iv_text_color_bg).setBackgroundColor(mContext.getResources().getColor(R.color.theme_dark));
        selectedView.setBackgroundColor(mContext.getResources().getColor(R.color.theme_red));
    }

    private void initTextTexture() {
        mTextureList = new ArrayList<>();
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture1));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture2));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture3));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture4));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture5));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture6));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture7));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture8));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture9));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture10));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture11));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture12));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture13));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture14));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture15));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture16));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture17));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture18));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture19));
        mTextureList.add(new TextTextureAdapter.Texture(R.drawable.icon_text_texture20));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) {
            return;
        }
        TextOperator textOperator = (TextOperator) mTextOpList.get(mCurOp);
        switch (seekBar.getId()) {
            case R.id.sb_change_transparent:
                textOperator.setAlpha(progress * 1.0f / seekBar.getMax());
                break;
            case R.id.sb_change_size:
                textOperator.setSize(progress);
                break;
            case R.id.sb_change_color_red:
                textOperator.setRed(progress * 1.0f / seekBar.getMax());
                break;
            case R.id.sb_change_color_green:
                textOperator.setGreen(progress * 1.0f / seekBar.getMax());
                break;
            case R.id.sb_change_color_blue:
                textOperator.setBlue(progress * 1.0f / seekBar.getMax());
                break;
            default:
                break;
        }
        IEManager.getInstance().updateOperator(0, textOperator, true);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
