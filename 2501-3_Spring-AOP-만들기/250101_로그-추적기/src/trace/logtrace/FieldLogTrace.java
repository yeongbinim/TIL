package yeim.aop.trace.logtrace;

import lombok.extern.slf4j.Slf4j;
import yeim.aop.trace.TraceId;
import yeim.aop.trace.TraceStatus;

@Slf4j
public class FieldLogTrace implements LogTrace {

	private static final String START_PREFIX = "-->";
	private static final String COMPLETE_PREFIX = "<--";
	private static final String EX_PREFIX = "<X-";

	private TraceId traceIdHolder; // traceId 동기화, 동시성 이슈 발생

	private static String addSpace(String prefix, int level) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append((i == level - 1) ? "|" + prefix : "|   ");
		}
		return sb.toString();
	}

	@Override
	public TraceStatus begin(String message) {
		syncTraceId();
		TraceId traceId = traceIdHolder;
		Long startTimeMs = System.currentTimeMillis();

		log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);

		return new TraceStatus(traceId, startTimeMs, message);
	}

	@Override
	public void end(TraceStatus status) {
		Long stopTimeMs = System.currentTimeMillis();
		Long resultTimeMs = stopTimeMs - status.getStartTimeMs();
		TraceId traceId = status.getTraceId();

		log.info("[{}] {}{} time={}ms", traceId.getId(),
			addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(),
			resultTimeMs);
		releaseTraceId();
	}

	@Override
	public void exception(TraceStatus status, Exception e) {
		Long stopTimeMs = System.currentTimeMillis();
		Long resultTimeMs = stopTimeMs - status.getStartTimeMs();
		TraceId traceId = status.getTraceId();

		log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
			addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs,
			e.toString());
		releaseTraceId();
	}

	private void syncTraceId() {
		if (traceIdHolder == null) {
			traceIdHolder = new TraceId();
			return;
		}
		traceIdHolder = traceIdHolder.createNextId();
	}

	private void releaseTraceId() {
		if (traceIdHolder.isFirstLevel()) {
			traceIdHolder = null;
		} else {
			traceIdHolder = traceIdHolder.createPreviousId();
		}
	}
}
