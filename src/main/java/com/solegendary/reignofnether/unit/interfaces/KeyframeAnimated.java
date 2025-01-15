package com.solegendary.reignofnether.unit.interfaces;

import net.minecraft.client.animation.AnimationDefinition;

public interface KeyframeAnimated {
    public void stopAllAnimations();
    public void setAnimateTicksLeft(int ticks);
    public int getAnimateTicksLeft();
    public default void startAnimation(AnimationDefinition animDef) {
        if (getAnimateTicksLeft() <= 0)
            setAnimateTicksLeft((int) (animDef.lengthInSeconds() * 20));
    }
}
