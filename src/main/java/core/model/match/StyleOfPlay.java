package core.model.match;


import core.util.HOLogger;

public class StyleOfPlay {

    /**
     * Possible values for style of play
     * null unknown (not downloaded yet)
     * -10	100% defensive
     * -9	90% defensive
     * -8	80% defensive
     * -7	70% defensive
     * -6	60% defensive
     * -5	50% defensive
     * -4	40% defensive
     * -3	30% defensive
     * -2	20% defensive
     * -1	10% defensive
     * 0	Neutral
     * 1	10% offensive
     * 2	20% offensive
     * 3	30% offensive
     * 4	40% offensive
     * 5	50% offensive
     * 6	60% offensive
     * 7	70% offensive
     * 8	80% offensive
     * 9	90% offensive
     * 10	100% offensive
     */
    private final Integer val;

    private StyleOfPlay(Integer styleOfPlay) {
        this.val = styleOfPlay;
    }

    public static StyleOfPlay fromInt(Integer styleOfPlay) {
        if (styleOfPlay == null || styleOfPlay >= -10 && styleOfPlay <= 10) {
            return new StyleOfPlay(styleOfPlay);
        }
        if (styleOfPlay == -1000) { // old matches has -1000
            return StyleOfPlay.Neutral();
        }
        HOLogger.instance().warning(StyleOfPlay.class, "Unknown style of play: " + styleOfPlay);
        return new StyleOfPlay(null);
    }

    public static StyleOfPlay Neutral() {
        return new StyleOfPlay(0);
    }

    public static Integer toInt(StyleOfPlay in) {
        if (in != null) return in.val;
        return null;
    }

    public static StyleOfPlay Defensive() {
        return new StyleOfPlay(-10);
    }

    public static StyleOfPlay Offensive() {
        return new StyleOfPlay(10);
    }
}
