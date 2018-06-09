import * as jobTypes from "../services/jobTypes"

export const isAlert = job =>
  job.jobType === jobTypes.OCO &&
  ((job.high && job.high.job.jobType === jobTypes.ALERT) ||
    (job.low && job.low.job.jobType === jobTypes.ALERT))

export const isStop = job =>
    job.jobType === jobTypes.OCO &&
    ((!job.low && job.high && job.high.job.jobType === jobTypes.LIMIT_ORDER) ||
      (!job.high && job.low && job.low.job.jobType === jobTypes.LIMIT_ORDER))