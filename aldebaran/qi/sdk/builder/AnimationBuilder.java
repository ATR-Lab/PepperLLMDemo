package com.aldebaran.qi.sdk.builder;

import android.support.annotation.RawRes;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.util.FutureUtils;
import com.aldebaran.qi.sdk.util.IOUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Build a new {@link Animation}
 */
public class AnimationBuilder {

    private final QiContext qiContext;
    private final List<String> texts;
    private List<Integer> resourceIds;
    private List<String> assets;

    private AnimationBuilder(QiContext context) {
        this.qiContext = context;
        this.resourceIds = new ArrayList<>();
        this.assets = new ArrayList<>();
        this.texts = new ArrayList<>();
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static AnimationBuilder with(QiContext context) {
        return new AnimationBuilder(context);
    }

    /**
     * Add assets into animation
     *
     * @param assetNames the assets
     * @return the builder
     */
    public AnimationBuilder withAssets(String... assetNames) {
        if (assetNames != null) {
            assets.addAll(Arrays.asList(assetNames));
        }
        return this;
    }

    /**
     * Add assets into animation
     *
     * @param assetNames the assets
     * @return the builder
     */
    public AnimationBuilder withAssets(List<String> assetNames) {
        if (assetNames != null) {
            assets.addAll(assetNames);
        }
        return this;
    }

    /**
     * Add resources into animation
     *
     * @param resIds the resources ids
     * @return the builder
     */
    public AnimationBuilder withResources(@RawRes Integer... resIds) {
        if (resIds != null) {
            resourceIds.addAll(Arrays.asList(resIds));
        }
        return this;
    }

    /**
     * Add resources into animation
     *
     * @param resIds the resources ids
     * @return the builder
     */
    public AnimationBuilder withResources(List<Integer> resIds) {
        if (resIds != null) {
            resourceIds.addAll(resIds);
        }
        return this;
    }

    /**
     * Add animation from texts
     *
     * @param texts the texts
     * @return the builder
     */
    public AnimationBuilder withTexts(String... texts) {
        if (texts != null) {
            this.texts.addAll(Arrays.asList(texts));
        }
        return this;
    }


    /**
     * Add animation from texts
     *
     * @param texts the texts
     * @return the builder
     */
    public AnimationBuilder withTexts(List<String> texts) {
        if (texts != null) {
            this.texts.addAll(texts);
        }
        return this;
    }


    /**
     * Create the {@link Animation} instance using configured values
     *
     * @return the Animation
     */
    public Animation build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Create the {@link Animation} instance using configured values
     *
     * @return the Animation
     */
    public Future<Animation> buildAsync() {

        List<String> anims = new ArrayList<>(texts);

        for (Integer resId : resourceIds) {
            anims.add(IOUtils.fromRaw(qiContext, resId));
        }

        for (String asset : assets) {
            anims.add(IOUtils.fromAsset(qiContext, asset));
        }

        if (anims.isEmpty()) {
            throw new IllegalStateException("Animations required.");
        }

        return qiContext.getActuationAsync()
                .andThenCompose(service -> service.async().makeAnimation(anims));
    }
}
