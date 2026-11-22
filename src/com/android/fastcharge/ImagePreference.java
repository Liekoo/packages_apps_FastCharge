/*
 * SPDX-FileCopyrightText: 2025 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.fastcharge;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ImagePreference extends Preference {

    private float mAlpha = 1f;

    public ImagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.fastcharge_image_layout);
    }

    /** Dims the illustration for slower modes (0.4 - 1.0). */
    public void setIntensity(float alpha) {
        if (mAlpha == alpha) return;
        mAlpha = alpha;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        ImageView imageView = (ImageView) holder.findViewById(R.id.fastcharge_image);
        if (imageView != null) {
            imageView.setImageResource(R.drawable.ic_fastcharge_illustration);
            imageView.animate().alpha(mAlpha).setDuration(250).start();
        }
    }
}
