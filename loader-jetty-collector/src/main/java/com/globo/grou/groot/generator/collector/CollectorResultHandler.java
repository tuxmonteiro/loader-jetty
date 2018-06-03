/*
 * Copyright (c) 2017-2018 Globo.com
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globo.grou.groot.generator.collector;

import java.util.Map;

import com.globo.grou.groot.generator.listeners.CollectorInformations;

/**
 *
 */
public interface CollectorResultHandler
{

    /**
     *
     * @param responseTimePerPath  key is the http path, value is the reponse time information
     */
    void handleResponseTime( Map<String, CollectorInformations> responseTimePerPath );

}
