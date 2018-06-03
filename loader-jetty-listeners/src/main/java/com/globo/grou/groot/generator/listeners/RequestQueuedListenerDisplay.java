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

package com.globo.grou.groot.generator.listeners;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.globo.grou.groot.generator.LoadGenerator;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 *
 */
public class RequestQueuedListenerDisplay
    extends Request.Listener.Adapter
    implements LoadGenerator.EndListener
{

    private static final Logger LOGGER = Log.getLogger( RequestQueuedListenerDisplay.class );

    private AtomicLong requestsQueued = new AtomicLong( 0 );

    private ScheduledExecutorService scheduledExecutorService;

    public RequestQueuedListenerDisplay()
    {
        this( 10, 30, TimeUnit.SECONDS );
    }

    public RequestQueuedListenerDisplay( long initial, long delay, TimeUnit timeUnit )
    {
        scheduledExecutorService = Executors.newScheduledThreadPool( 1 );
        scheduledExecutorService.scheduleWithFixedDelay( () ->
            {
                LOGGER.info( "----------------------------------------" );
                LOGGER.info( "  Requests in queue: " + requestsQueued.get() );
                LOGGER.info( "----------------------------------------" );
            },//
            initial, delay, timeUnit );
    }

    @Override
    public void onQueued( Request request )
    {
        requestsQueued.incrementAndGet();
    }

    @Override
    public void onBegin( Request request )
    {
        requestsQueued.decrementAndGet();
    }

    @Override
    public void onEnd( LoadGenerator generator )
    {
        this.scheduledExecutorService.shutdownNow();
    }

}
