package redstonedisorder.mendingtracker;

public class ConfigOptions {
    int xPos = 1;
    int yPos = 2;
    Alignment textAlignment = Alignment.Left;
    boolean overrideMinihud = false;

    public enum Alignment { Left, Center, Right }

    ConfigOptions normalized() {
        ConfigOptions copy = new ConfigOptions();
        copy.xPos = Math.clamp(xPos, 0, 100);
        copy.yPos = Math.clamp(yPos, 0, 100);
        copy.textAlignment = textAlignment == null ? Alignment.Left : textAlignment;
        copy.overrideMinihud = overrideMinihud;
        return copy;
    }
}