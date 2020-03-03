/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.jobrun;

class TestingJobEvent {

  public static final TestingJobEvent create(String jobId, EventType eventType) {
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
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TestingJobEvent other = (TestingJobEvent) obj;
    if (eventType != other.eventType) return false;
    if (jobId == null) {
      if (other.jobId != null) return false;
    } else if (!jobId.equals(other.jobId)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "TestingJobEvent [jobId=" + jobId + ", eventType=" + eventType + "]";
  }
}
