package thecodex6824.thaumicaugmentation.api.augment.impl.custom;

public interface IBuilderStrengthProvider extends IBuilderAugmentCallback {

    public default int calculateTintColor(ICustomAugment augment) {
        return 0xFFFFFFFF;
    }
}
