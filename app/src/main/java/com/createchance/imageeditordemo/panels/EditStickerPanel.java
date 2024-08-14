package com.createchance.imageeditordemo.panels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.createchance.imageeditor.IEManager;
import com.createchance.imageeditor.ops.StickerOperator;
import com.createchance.imageeditordemo.R;
import com.createchance.imageeditordemo.StickerListAdapter;
import com.createchance.imageeditordemo.model.Sticker;
import com.createchance.imageeditordemo.utils.AssetsUtil;

import java.io.File;
import java.util.List;

/**
 * Sticker edit panel
 *
 * @author createchance
 * @date 2018/11/4
 */
public class EditStickerPanel extends AbstractPanel implements
        StickerListAdapter.OnStickerSelectListener,
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "EditStickerPanel";

    private View mStickerPanel;
    private ViewGroup mSubPanelContainer;
    private View mStickerAssetPanel, mStickerAdjustPanel;

    private RecyclerView mStickerListView;
    private StickerListAdapter mStickerListAdapter;
    private List<Sticker> mStickerList;

    private StickerOperator mCurOp;
    private Sticker mCurSticker;

    private int mLastX, mLastY;

    public EditStickerPanel(Context context, PanelListener listener) {
        super(context, listener, TYPE_STICKER);

        initStickerList();

        initMainPanel();
        initAssetPanel();
        initAdjustPanel();

        mSubPanelContainer.removeAllViews();
        mSubPanelContainer.addView(mStickerAssetPanel);
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        if (mCurOp == null) {
            return;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float curX = mCurOp.getPosX() + (event.getX() - mLastX) * 1.0f / IEManager.getInstance().getRenderWidth(0);
                float curY = mCurOp.getPosY() - (event.getY() - mLastY) * 1.0f / IEManager.getInstance().getRenderHeight(0);

                mCurOp.setPosX(curX);
                mCurOp.setPosY(curY);
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                IEManager.getInstance().updateOperator(0, mCurOp, true);
                break;
            default:
                break;
        }
    }

    @Override
    public void show(ViewGroup parent, int surfaceWidth, int surfaceHeight) {
        super.show(parent, surfaceWidth, surfaceHeight);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mParent.addView(mStickerPanel, params);
    }

    @Override
    public void close(boolean discard) {
        super.close(discard);

        if (discard && mCurOp != null) {
            IEManager.getInstance().removeOperator(0, mCurOp, true);
            mCurOp = null;
            mCurSticker = null;
        }
    }

    private void initStickerList() {
        mStickerList = AssetsUtil.parseJsonToList(mContext, "stickers/stickers.json", Sticker.class);
    }

    @Override
    public void onStickerSelected(Sticker sticker) {
        if (mCurSticker == sticker) {
            Log.w(TAG, "onStickerSelected, sticker not changed, so skip.");
            return;
        }
        mCurSticker = sticker;
        if (mCurOp == null) {
            Bitmap stickerImg = BitmapFactory.decodeFile(new File(mContext.getFilesDir(), sticker.mAsset).getAbsolutePath());
            mCurOp = new StickerOperator.Builder()
                    .sticker(stickerImg)
                    .position(0.5f, 0.5f)
                    .build();
            IEManager.getInstance().addOperator(0, mCurOp, true);
        } else {
            mCurOp.setSticker(BitmapFactory.decodeFile(new File(mContext.getFilesDir(), sticker.mAsset).getAbsolutePath()));
            IEManager.getInstance().updateOperator(0, mCurOp, true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sticker_asset:
                mStickerPanel.findViewById(R.id.tv_sticker_asset).setBackgroundResource(R.color.theme_dark);
                mStickerPanel.findViewById(R.id.tv_sticker_adjust).setBackgroundResource(R.color.theme_dark);
                v.setBackgroundResource(R.color.theme_red);
                mSubPanelContainer.removeAllViews();
                mSubPanelContainer.addView(mStickerAssetPanel);
                break;
            case R.id.tv_sticker_adjust:
                mStickerPanel.findViewById(R.id.tv_sticker_asset).setBackgroundResource(R.color.theme_dark);
                mStickerPanel.findViewById(R.id.tv_sticker_adjust).setBackgroundResource(R.color.theme_dark);
                v.setBackgroundResource(R.color.theme_red);
                mSubPanelContainer.removeAllViews();
                mSubPanelContainer.addView(mStickerAdjustPanel);
                break;
            case R.id.iv_cancel:
                close(true);
                break;
            case R.id.iv_apply:
                close(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mCurOp == null) {
            return;
        }
        switch (seekBar.getId()) {
            case R.id.sb_sticker_alpha:
                mCurOp.setAlphaFactor(progress * 1.0f / seekBar.getMax());
                break;
            case R.id.sb_sticker_size:
                mCurOp.setScaleFactor(progress * 1.0f / seekBar.getMax());
                break;
            case R.id.sb_sticker_red:
                mCurOp.setRed(progress * 1.0f / seekBar.getMax());
                break;
            case R.id.sb_sticker_green:
                mCurOp.setGreen(progress * 1.0f / seekBar.getMax());
                break;
            case R.id.sb_sticker_blue:
                mCurOp.setBlue(progress * 1.0f / seekBar.getMax());
                break;
            case R.id.sb_sticker_rotation:
                mCurOp.setRotationZ(progress);
                break;
            default:
                break;
        }

        IEManager.getInstance().updateOperator(0, mCurOp, true);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void initMainPanel() {
        mStickerPanel = LayoutInflater.from(mContext).inflate(R.layout.edit_panel_stickers, mParent, false);
        mSubPanelContainer = mStickerPanel.findViewById(R.id.vw_sticker_sub_panel_container);
        mStickerPanel.findViewById(R.id.tv_sticker_asset).setOnClickListener(this);
        mStickerPanel.findViewById(R.id.tv_sticker_adjust).setOnClickListener(this);
        mStickerPanel.findViewById(R.id.iv_cancel).setOnClickListener(this);
        mStickerPanel.findViewById(R.id.iv_apply).setOnClickListener(this);
    }

    private void initAssetPanel() {
        mStickerAssetPanel = LayoutInflater.from(mContext)
                .inflate(R.layout.edit_panel_stickers_asset, mSubPanelContainer, false);
        mStickerListView = mStickerAssetPanel.findViewById(R.id.rcv_sticker_list);
        mStickerListView.setLayoutManager(new GridLayoutManager(mContext, 4));
        mStickerListAdapter = new StickerListAdapter(mContext, mStickerList, this);
        mStickerListView.setAdapter(mStickerListAdapter);
    }

    private void initAdjustPanel() {
        mStickerAdjustPanel = LayoutInflater.from(mContext)
                .inflate(R.layout.edit_panel_stickers_adjust, mSubPanelContainer, false);
        ((SeekBar) mStickerAdjustPanel.findViewById(R.id.sb_sticker_alpha)).setOnSeekBarChangeListener(this);
        ((SeekBar) mStickerAdjustPanel.findViewById(R.id.sb_sticker_size)).setOnSeekBarChangeListener(this);
        ((SeekBar) mStickerAdjustPanel.findViewById(R.id.sb_sticker_red)).setOnSeekBarChangeListener(this);
        ((SeekBar) mStickerAdjustPanel.findViewById(R.id.sb_sticker_green)).setOnSeekBarChangeListener(this);
        ((SeekBar) mStickerAdjustPanel.findViewById(R.id.sb_sticker_blue)).setOnSeekBarChangeListener(this);
        ((SeekBar) mStickerAdjustPanel.findViewById(R.id.sb_sticker_rotation)).setOnSeekBarChangeListener(this);
    }
}
