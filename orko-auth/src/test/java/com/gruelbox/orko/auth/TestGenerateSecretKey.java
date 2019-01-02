package com.gruelbox.orko.auth;

/*-
 * ===============================================================================L
 * Orko Auth
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.warrenstrange.googleauth.GoogleAuthenticator;

public class TestGenerateSecretKey {

  GenerateSecretKey generateSecretKey;

  @Before
  public void setup() {
    generateSecretKey = new GenerateSecretKey(new GoogleAuthenticator());
  }

  @Test
  public void testGenerateSecretKeyTrue() {
    String key = generateSecretKey.createNewKey();
    assertTrue(generateSecretKey.checkKey(key, generateSecretKey.generateValidInput(key)));
  }
}
