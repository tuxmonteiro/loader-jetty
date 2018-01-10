//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.load.generator.listeners;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoadResult
{

    private ServerInfo serverInfo;

    private CollectorInformations collectorInformations;

    private List<LoadConfig> loadConfigs = new ArrayList<>();

    private String uuid;

    private String comment;

    /**
     * so we can search prefix-*
     */
    private String uuidPrefix;

    /**
     * timestamp using format
     */
    private String timestamp = ZonedDateTime.now().format( DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm.ssZ" ) );

    public LoadResult()
    {
        // no op
    }

    public LoadResult( ServerInfo serverInfo, CollectorInformations collectorInformations, LoadConfig loadConfig )
    {
        this.serverInfo = serverInfo;
        this.collectorInformations = collectorInformations;
        this.loadConfigs.add( loadConfig );
    }

    public ServerInfo getServerInfo()
    {
        return serverInfo == null ? new ServerInfo() : serverInfo;
    }

    public CollectorInformations getCollectorInformations()
    {
        return collectorInformations == null ? new CollectorInformations() : collectorInformations;
    }

    public void setServerInfo( ServerInfo serverInfo )
    {
        this.serverInfo = serverInfo;
    }

    public void setCollectorInformations( CollectorInformations collectorInformations )
    {
        this.collectorInformations = collectorInformations;
    }

    public List<LoadConfig> getLoadConfigs()
    {
        return loadConfigs;
    }

    public void setLoadConfigs( List<LoadConfig> loadConfigs )
    {
        this.loadConfigs = loadConfigs;
    }

    public void addLoadConfig( LoadConfig loadConfig )
    {
        this.loadConfigs.add( loadConfig );
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid( String uuid )
    {
        this.uuid = uuid;
    }

    public LoadResult uuid( String uuid )
    {
        this.uuid = uuid;
        return this;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    public LoadResult comment( String comment )
    {
        this.comment = comment;
        return this;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp( String timestamp )
    {
        this.timestamp = timestamp;
    }

    public LoadResult timestamp( String timestamp )
    {
        this.timestamp = timestamp;
        return this;
    }

    public String getUuidPrefix()
    {
        return uuidPrefix;
    }

    public void setUuidPrefix( String uuidPrefix )
    {
        this.uuidPrefix = uuidPrefix;
    }

    public LoadResult uuidPrefix( String uuidPrefix )
    {
        this.uuidPrefix = uuidPrefix;
        return this;
    }

    @Override
    public String toString()
    {
        return "LoadResult{" + "serverInfo=" + serverInfo + ", collectorInformations=" + collectorInformations
            + ", loadConfigs=" + loadConfigs + ", uuid='" + uuid + '\'' + ", comment='" + comment + '\''
            + ", timestamp='" + timestamp + '\'' + '}';
    }
}
