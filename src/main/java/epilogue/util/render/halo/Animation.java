package epilogue.util.render.halo;

public class Animation {

    private Easing easing;
    private long duration;
    private long nanos;
    private long startTime;

    private double startValue;
    private double destinationValue;
    private double value;
    private boolean finished;

    public Animation(final Easing easing, final long durationNanos) {
        this.easing = easing;
        this.startTime = System.nanoTime();
        this.duration = durationNanos;
    }

    public void setDuration(long durationNanos) {
        this.duration = durationNanos;
    }

    public double run(final double destinationValue) {
        this.nanos = System.nanoTime();
        if (this.destinationValue != destinationValue) {
            this.destinationValue = destinationValue;
            this.reset();
        } else {
            this.finished = this.nanos - this.duration > this.startTime;
            if (this.finished) {
                this.value = destinationValue;
                return this.value;
            }
        }

        final double result = this.easing.getFunction().apply(this.getProgress());
        this.value = this.startValue + (destinationValue - this.startValue) * result;

        return this.value;
    }

    public double getProgress() {
        return Math.min(1, (double) (System.nanoTime() - this.startTime) / (double) this.duration);
    }

    public boolean isFinished() {
        return this.getProgress() == 1;
    }

    public void reset() {
        this.startTime = System.nanoTime();
        this.startValue = value;
        this.finished = false;
    }

    public double getValue() {
        return value;
    }
}