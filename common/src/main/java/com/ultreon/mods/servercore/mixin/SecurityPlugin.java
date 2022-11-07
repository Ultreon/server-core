package com.ultreon.mods.servercore.mixin;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.throwables.MixinException;

import java.util.List;
import java.util.Set;

/**
 * Mixing security plugin.
 */
@ApiStatus.Internal
public class SecurityPlugin implements IMixinConfigPlugin {
    public SecurityPlugin() {
        try {
            Class.forName(AntiMixin.class.getName());
        } catch (ClassNotFoundException e) {
            // Should not happen.
            throw new Error(e);
        }
    }

    @Override
    @ApiStatus.Internal
    public void onLoad(String mixinPackage) {

    }

    @Override
    @ApiStatus.Internal
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    @ApiStatus.Internal
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    @ApiStatus.Internal
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    @ApiStatus.Internal
    public List<String> getMixins() {
        return null;
    }

    @Override
    @ApiStatus.Internal
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        List<AnnotationNode> visibleAnnotations = targetClass.visibleAnnotations;
        if (visibleAnnotations == null) return;
        for (AnnotationNode anno : visibleAnnotations) {
            if (anno.desc.equals(AntiMixin.class.getName())) {
                throw new MixinException("Illegal mixin targets mixin-blocking class.");
            }
        }
    }

    @Override
    @ApiStatus.Internal
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
