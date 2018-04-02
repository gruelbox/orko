import { put } from './fetchUtil';

class JobService {
  
  async submitJob(job, token) {
    return await put('jobs', token, JSON.stringify(job))
  }

}

export default new JobService();