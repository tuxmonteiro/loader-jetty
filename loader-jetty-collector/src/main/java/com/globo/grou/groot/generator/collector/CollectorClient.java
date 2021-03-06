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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.grou.groot.generator.listeners.CollectorInformations;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 *
 */
public class CollectorClient
{

    private static final Logger LOGGER = Log.getLogger( CollectorClient.class );

    /**
     * addresses to collect informations must in the form of 127.0.0.1:8080 (ip:port)
     */
    private List<String> addresses;

    /**
     * will collect informations on remote server with this schedule
     */
    private long scheduleDelayInMillis = 1;

    private ScheduledExecutorService scheduledExecutorService;

    private List<HttpClient> httpClients;

    private List<CollectorResultHandler> collectorResultHandlers;

    public CollectorClient( List<String> addresses, long scheduleDelayInMillis,
                            List<CollectorResultHandler> collectorResultHandlers )
    {
        this.addresses = addresses;
        this.scheduleDelayInMillis = scheduleDelayInMillis;
        this.httpClients = new CopyOnWriteArrayList<>( );
        this.collectorResultHandlers = collectorResultHandlers == null ? Collections.emptyList() : collectorResultHandlers;
    }


    public CollectorClient start()
        throws Exception
    {

        // at least a default one
        if (this.collectorResultHandlers.isEmpty())
        {
            this.collectorResultHandlers = Arrays.asList( new LoggerCollectorResultHandler() );
        }

        this.scheduledExecutorService = Executors.newScheduledThreadPool( addresses.size() );

        for ( String address : this.addresses )
        {
            this.scheduledExecutorService.scheduleWithFixedDelay( () ->
                {

                    try
                    {
                        HttpClient httpClient = new HttpClient();
                        httpClient.start();
                        httpClients.add( httpClient );

                        ObjectMapper objectMapper = new ObjectMapper();


                        // response time per path informations
                        ContentResponse contentResponse = httpClient //
                            .newRequest( "http://" + address + "/collector/response-times" ) //
                            .send();

                        LOGGER.debug( "response time per path status: {}, response: {}", //
                                      contentResponse.getStatus(), //
                                      contentResponse.getContentAsString() );

                        TypeReference<Map<String, CollectorInformations>> typeRef =
                            new TypeReference<Map<String, CollectorInformations>>()
                            {
                            };

                        Map<String, CollectorInformations> responseTimePerPath =
                            objectMapper.readValue( contentResponse.getContentAsString(), typeRef );


                        for (CollectorResultHandler collectorResultHandler: collectorResultHandlers)
                        {
                            collectorResultHandler.handleResponseTime( responseTimePerPath );
                        }

                    }
                    catch ( Throwable e )
                    {
                        LOGGER.warn( e );
                    }

                }, 1, this.scheduleDelayInMillis, TimeUnit.MILLISECONDS );
        }

        return this;
    }

    public CollectorClient stop()
        throws Exception
    {
        for ( HttpClient httpClient : httpClients )
        {
            httpClient.stop();
        }
        this.scheduledExecutorService.shutdown();
        return this;
    }

    //--------------------------------------------------------------
    //  Builder
    //--------------------------------------------------------------

    public static class Builder
    {
        private List<String> addresses = new ArrayList<>();

        private long scheduleDelayInMillis = 5000;

        private List<CollectorResultHandler> collectorResultHandlers;

        public Builder()
        {
            // no op
        }

        public Builder addAddress( String address )
        {
            this.addresses.add( address );
            return this;
        }

        public Builder addAddresses( String... addresses )
        {
            this.addresses.addAll( Arrays.asList( addresses ) );
            return this;
        }

        public Builder addAddresses( List<String> addresses )
        {
            this.addresses.addAll( addresses );
            return this;
        }


        public Builder scheduleDelayInMillis( long scheduleDelayInMillis )
        {
            this.scheduleDelayInMillis = scheduleDelayInMillis;
            return this;
        }

        public Builder collectorResultHandlers( List<CollectorResultHandler> collectorResultHandlers )
        {
            this.collectorResultHandlers = collectorResultHandlers;
            return this;
        }


        public CollectorClient build()
        {
            if ( this.addresses.isEmpty() )
            {
                throw new IllegalArgumentException( "addresses are mandatory" );
            }
            if ( this.scheduleDelayInMillis < 1 )
            {
                throw new IllegalArgumentException( "scheduleDelayInMillis must be higher than 0" );
            }
            return new CollectorClient( this.addresses, this.scheduleDelayInMillis, this.collectorResultHandlers );
        }


    }

}
