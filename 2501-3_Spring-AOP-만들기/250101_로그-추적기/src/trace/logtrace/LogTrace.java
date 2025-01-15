package yeim.aop.trace.logtrace;

import yeim.aop.trace.TraceStatus;

public interface LogTrace {
	TraceStatus begin(String message);

	void end(TraceStatus status);

	void exception(TraceStatus status, Exception e);
}
