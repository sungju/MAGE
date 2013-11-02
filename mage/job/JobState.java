package mage.job;

public class JobState {
	public static final int JOB_INIT = 0;
	public static final int JOB_START = 1;
	public static final int JOB_PROGRESS = 2;
	public static final int JOB_FINISHED = 3;
	public static final int JOB_FAILED = 4;
	
	private static final String[] jobStateStr = {
		"Job Init",
		"Job Start",
		"Job Progress",
		"Job Finished",
		"Job Failed",
	};
	
	public static final String getJobStateStr(int code) {
		return jobStateStr[code];
	}
}
