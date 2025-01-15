package yeim.aop.trace.hellotrace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yeim.aop.trace.TraceId;
import yeim.aop.trace.TraceStatus;

@Slf4j
@Component
public class HelloTraceV2 {

	private static final String START_PREFIX = "-->";
	private static final String COMPLETE_PREFIX = "<--";
	private static final String EX_PREFIX = "<X-";

	private static String addSpace(String prefix, int level) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append((i == level - 1) ? "|" + prefix : "|   ");
		}
		return sb.toString();
	}

	public TraceStatus begin(String message) {
		TraceId traceId = new TraceId();
		Long startTimeMs = System.currentTimeMillis();

		log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);

		return new TraceStatus(traceId, startTimeMs, message);
	}

	public TraceStatus beginSync(TraceId beforeTraceId, String message) {
		TraceId nextId = beforeTraceId.createNextId();
		Long startTimeMs = System.currentTimeMillis();
		log.info("[{}] {}{}", nextId.getId(), addSpace(START_PREFIX, nextId.getLevel()), message);
		return new TraceStatus(nextId, startTimeMs, message);
	}

	public void end(TraceStatus status) {
		Long stopTimeMs = System.currentTimeMillis();
		Long resultTimeMs = stopTimeMs - status.getStartTimeMs();
		TraceId traceId = status.getTraceId();

		log.info("[{}] {}{} time={}ms", traceId.getId(),
			addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs);
	}

	public void exception(TraceStatus status, Exception e) {
		Long stopTimeMs = System.currentTimeMillis();
		Long resultTimeMs = stopTimeMs - status.getStartTimeMs();
		TraceId traceId = status.getTraceId();

		log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
			addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs, e.toString());
	}
}
