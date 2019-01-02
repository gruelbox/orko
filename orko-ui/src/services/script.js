import { put, get, del } from "./fetchUtil"

class ScriptService {
  async fetchScripts() {
    return await get("scripts")
  }

  async saveScript(script) {
    // console.log("Script saved", script)
    return await put("scripts/" + script.id, JSON.stringify(script))
  }

  async deleteScript(id) {
    return await del("scripts/" + id)
  }
}

export default new ScriptService()
