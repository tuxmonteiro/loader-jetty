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

package com.globo.grou.groot.generator.starter;

import com.beust.jcommander.JCommander;
import com.globo.grou.groot.generator.LoadGenerator;
import com.globo.grou.groot.generator.listeners.CollectorInformations;
import com.globo.grou.groot.generator.listeners.report.GlobalSummaryListener;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class LoadGeneratorStarter {
    private static final Logger LOGGER = Log.getLogger(LoadGeneratorStarter.class);

    public static void main(String[] args) {
        LoadGeneratorStarterArgs starterArgs = parse(args);
        if (starterArgs == null) {
            return;
        }
        LoadGenerator.Builder builder = prepare(starterArgs);
        GlobalSummaryListener globalSummaryListener = new GlobalSummaryListener();
        builder = builder.resourceListener(globalSummaryListener).requestListener(globalSummaryListener);
        run(builder);
        if (starterArgs.isDisplayStats()) {
            displayGlobalSummaryListener(globalSummaryListener);
        }
    }

    public static LoadGeneratorStarterArgs parse(String[] args) {
        return parse(args, LoadGeneratorStarterArgs::new);
    }

    public static <T extends LoadGeneratorStarterArgs> T parse(String[] args, Supplier<T> argsSupplier) {
        T starterArgs = argsSupplier.get();
        JCommander jCommander = new JCommander(starterArgs);
        jCommander.setAcceptUnknownOptions(true);
        jCommander.parse(args);
        if (starterArgs.isHelp()) {
            jCommander.usage();
            return null;
        }
        return starterArgs;
    }

    public static LoadGenerator.Builder prepare(LoadGeneratorStarterArgs starterArgs) {
        try {
            LoadGenerator.Builder builder = new LoadGenerator.Builder();
            return builder.threads(starterArgs.getThreads())
                    .warmupIterationsPerThread(starterArgs.getWarmupIterations())
                    .iterationsPerThread(starterArgs.getIterations())
                    .runFor(starterArgs.getRunningTime(), starterArgs.getRunningTimeUnit())
                    .usersPerThread(starterArgs.getUsersPerThread())
                    .channelsPerUser(starterArgs.getChannelsPerUser())
                    .resource(starterArgs.getResource(builder))
                    .resourceRate(starterArgs.getResourceRate())
                    .rateRampUpPeriod(starterArgs.getRateRampUpPeriod())
                    .httpClientTransportBuilder(starterArgs.getHttpClientTransportBuilder())
                    .sslContextFactory(new SslContextFactory())
                    .scheme(starterArgs.getScheme())
                    .host(starterArgs.getHost())
                    .port(starterArgs.getPort())
                    .maxRequestsQueued(starterArgs.getMaxRequestsQueued())
                    .connectBlocking(starterArgs.isConnectBlocking())
                    .connectTimeout(starterArgs.getConnectTimeout())
                    .idleTimeout(starterArgs.getIdleTimeout());
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    public static void run(LoadGenerator.Builder builder) {
        run(builder.build());
    }

    public static void run(LoadGenerator loadGenerator) {
        LOGGER.info("load generator config: {}", loadGenerator.getConfig());
        LOGGER.info("load generation begin");
        CompletableFuture<Void> cf = loadGenerator.begin();
        cf.whenComplete((x, f) -> {
            if (f == null) {
                LOGGER.info("load generation complete");
            } else {
                LOGGER.info("load generation failure", f);
            }
        }).join();
    }

    private static void displayGlobalSummaryListener(GlobalSummaryListener globalSummaryListener) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z");
        CollectorInformations latencyTimeSummary =
                new CollectorInformations(globalSummaryListener.getLatencyTimeHistogram() //
                        .getIntervalHistogram());

        long totalRequestCommitted = globalSummaryListener.getRequestCommitTotal();
        long start = latencyTimeSummary.getStartTimeStamp();
        long end = latencyTimeSummary.getEndTimeStamp();

        LOGGER.info("");
        LOGGER.info("");
        LOGGER.info("----------------------------------------------------");
        LOGGER.info("--------    Latency Time Summary     ---------------");
        LOGGER.info("----------------------------------------------------");
        LOGGER.info("total count:" + latencyTimeSummary.getTotalCount());
        LOGGER.info("maxLatency:" //
                + nanosToMillis(latencyTimeSummary.getMaxValue()));
        LOGGER.info("minLatency:" //
                + nanosToMillis(latencyTimeSummary.getMinValue()));
        LOGGER.info("aveLatency:" //
                + nanosToMillis(Math.round(latencyTimeSummary.getMean())));
        LOGGER.info("50Latency:" //
                + nanosToMillis(latencyTimeSummary.getValue50()));
        LOGGER.info("90Latency:" //
                + nanosToMillis(latencyTimeSummary.getValue90()));
        LOGGER.info("stdDeviation:" //
                + nanosToMillis(Math.round(latencyTimeSummary.getStdDeviation())));
        LOGGER.info("start: {}, end: {}", //
                simpleDateFormat.format(latencyTimeSummary.getStartTimeStamp()), //
                simpleDateFormat.format(latencyTimeSummary.getEndTimeStamp()));
        LOGGER.info("----------------------------------------------------");
        LOGGER.info("-----------     Estimated QPS     ------------------");
        LOGGER.info("----------------------------------------------------");
        long timeInSeconds = TimeUnit.SECONDS.convert(end - start, TimeUnit.MILLISECONDS);
        long qps = totalRequestCommitted / timeInSeconds;
        LOGGER.info("estimated QPS : " + qps);
        LOGGER.info("----------------------------------------------------");
        LOGGER.info("response 1xx family: " + globalSummaryListener.getResponses1xx().longValue());
        LOGGER.info("response 2xx family: " + globalSummaryListener.getResponses2xx().longValue());
        LOGGER.info("response 3xx family: " + globalSummaryListener.getResponses3xx().longValue());
        LOGGER.info("response 4xx family: " + globalSummaryListener.getResponses4xx().longValue());
        LOGGER.info("response 5xx family: " + globalSummaryListener.getResponses5xx().longValue());
        LOGGER.info("");
    }

    private static long nanosToMillis(long nanosValue) {
        return TimeUnit.NANOSECONDS.toMillis(nanosValue);
    }
}
