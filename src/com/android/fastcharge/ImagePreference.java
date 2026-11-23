/*
 * SPDX-FileCopyrightText: 2025 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.fastcharge;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ImagePreference extends Preference {

    private int mTint;

    public ImagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.fastcharge_image_layout);
        mTint = context.getColor(R.color.mode_super_fast);
    }

    /** Colors the illustration for the current mode. */
    public void setTint(int color) {
        if (mTint == color) return;
        mTint = color;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        ImageView imageView = (ImageView) holder.findViewById(R.id.fastcharge_image);
        if (imageView != null) {
            imageView.setImageResource(R.drawable.ic_fastcharge_illustration);
            // The artwork is a single-color shape on transparency, so SRC_IN recolors it.
            imageView.setColorFilter(mTint, PorterDuff.Mode.SRC_IN);
        }
    }
}
