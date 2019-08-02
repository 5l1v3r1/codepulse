/* Code Pulse: a real-time code coverage tool, for more information, see <http://code-pulse.com/>
 *
 * Copyright (C) 2014-2017 Code Dx, Inc. <https://codedx.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codedx.codepulse.agent.init;

import java.io.IOException;

import com.codedx.codepulse.agent.common.config.RuntimeAgentConfigurationV1;
import com.codedx.codepulse.agent.common.connect.Connection;

public interface ControlConnectionHandshake
{
	RuntimeAgentConfigurationV1 performHandshake(Connection connection, int projectId) throws IOException;
}
