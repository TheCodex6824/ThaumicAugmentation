package thecodex6824.thaumicaugmentation.api.augment.impl.custom;

public interface IBuilderEffectProvider extends IBuilderAugmentCallback {

    boolean compatibleWith(IBuilderStrengthProvider strengthProvider);
}
