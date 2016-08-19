//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.load.collector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.load.generator.CollectorInformations;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    public CollectorClient( List<String> addresses, long scheduleDelayInMillis )
    {
        this.addresses = addresses;
        this.scheduleDelayInMillis = scheduleDelayInMillis;
        this.httpClients = new ArrayList<>( this.addresses.size() );
    }


    public CollectorClient start()
        throws Exception
    {

        this.scheduledExecutorService = Executors.newScheduledThreadPool( addresses.size() );

        for ( String address : this.addresses )
        {
            this.scheduledExecutorService.scheduleWithFixedDelay( new Runnable()
            {
                @Override
                public void run()
                {

                    try
                    {
                        HttpClient httpClient = new HttpClient();
                        httpClient.start();
                        httpClients.add( httpClient );

                        // client latency collect
                        ContentResponse contentResponse = httpClient //
                            .newRequest( "http://" + address + "/collector/client-latency" ) //
                            .send();

                        LOGGER.debug( "latence response status: {}, response: {}", //
                                      contentResponse.getStatus(), //
                                      contentResponse.getContentAsString() );

                        ObjectMapper objectMapper = new ObjectMapper();
                        CollectorInformations infos = objectMapper //
                            .readValue( contentResponse.getContentAsString(), //
                                        CollectorInformations.class );

                        LOGGER.debug( "infos: {}", infos );

                        // response time per path informations
                        contentResponse = httpClient //
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

                        LOGGER.debug( "responseTimePerPath: {}", responseTimePerPath );

                    }
                    catch ( Throwable e )
                    {
                        LOGGER.warn( e );
                    }
                }
            }, 10, this.scheduleDelayInMillis, TimeUnit.MILLISECONDS );
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

        public static Builder builder()
        {
            return new Builder();
        }

        private Builder()
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
            return new CollectorClient( this.addresses, this.scheduleDelayInMillis );
        }


    }

}