package br.com.threadstech.stockfy;

import io.github.bucket4j.TimeMeter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class MutableTimeMeter implements TimeMeter {

	private final AtomicLong currentTimeNanos = new AtomicLong();

	@Override
	public long currentTimeNanos() {
		return currentTimeNanos.get();
	}

	@Override
	public boolean isWallClockBased() {
		return false;
	}

	public void advanceBy(Duration duration) {
		currentTimeNanos.addAndGet(duration.toNanos());
	}

	public void reset() {
		currentTimeNanos.set(0);
	}

}
