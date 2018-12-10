import { put, get, del } from "./fetchUtil"

class JobService {
  async submitJob(job) {
    return await put("jobs/" + job.id, JSON.stringify(job))
  }

  async deleteJob(job) {
    return await del("jobs/" + job.id)
  }

  async fetchJobs() {
    return await get("jobs")
  }

  async fetchJob(jobId) {
    return await get("jobs/" + jobId)
  }
}

export default new JobService()
