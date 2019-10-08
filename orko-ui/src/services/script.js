/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import { put, get, del } from "modules/common/util/fetchUtil"

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
