//
//  ========================================================================
//  Copyright (c) 1995-2017 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.load.generator.responsetime;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public interface HistogramConstants
{

    /**
     * lowest value
     */
    long LOWEST_DISCERNIBLE_VALUE = TimeUnit.MILLISECONDS.toNanos( 1 );

    /**
     * max 10 minutes otherwise we get out of range in histogram
     */
    long HIGHEST_TRACKABLE_VALUE = TimeUnit.MINUTES.toNanos( 10 );

    int NUMBER_OF_SIGNIFICANT_VALUE_DIGITS = 3;

}

