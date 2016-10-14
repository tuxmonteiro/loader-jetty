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

package org.eclipse.jetty.load.generator.report;

import org.eclipse.jetty.load.generator.responsetime.ResponseTimeListener;

import java.io.Serializable;

/**
 * Use this one to collect all values
 */
public class DetailledResponseTimeReportListener
    implements ResponseTimeListener, Serializable
{
    private DetailledResponseTimeReport detailledResponseTimeReport = new DetailledResponseTimeReport();

    @Override
    public void onLoadGeneratorStop()
    {
        // no op
    }

    @Override
    public void onResponseTimeValue( Values values )
    {
        this.detailledResponseTimeReport.addEntry(
            new DetailledResponseTimeReport.Entry( values.getEventTimestamp(), //
                                                   values.getPath(), //
                                                   values.getStatus(), //
                                                   values.getTime() ) );
    }

    public DetailledResponseTimeReport getDetailledResponseTimeReport()
    {
        return detailledResponseTimeReport;
    }
}
