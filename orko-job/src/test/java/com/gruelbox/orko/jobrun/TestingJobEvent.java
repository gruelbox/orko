package com.gruelbox.orko.jobrun;

class TestingJobEvent {

  public static final TestingJobEvent create( String jobId, EventType eventType) {
    return new TestingJobEvent(jobId, eventType);
  }

  private final String jobId;
  private final EventType eventType;

  public TestingJobEvent(String jobId, EventType eventType) {
    this.jobId = jobId;
    this.eventType = eventType;
  }

  public String jobId() {
    return jobId;
  }

  public EventType eventType() {
    return eventType;
  }

  public enum EventType {
    START,
    FINISH
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
    result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TestingJobEvent other = (TestingJobEvent) obj;
    if (eventType != other.eventType)
      return false;
    if (jobId == null) {
      if (other.jobId != null)
        return false;
    } else if (!jobId.equals(other.jobId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TestingJobEvent [jobId=" + jobId + ", eventType=" + eventType + "]";
  }
}