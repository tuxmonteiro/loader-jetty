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

import com.globo.grou.groot.generator.LoadGenerator;
import com.globo.grou.groot.generator.Resource;
import com.globo.grou.groot.generator.listeners.QpsListenerDisplay;
import com.globo.grou.groot.generator.listeners.RequestQueuedListenerDisplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;



/**
 * Class to start remote process for Jenkins
 */
public class JenkinsRemoteStarter {
    private static List<Resource.NodeListener> nodeListeners;
    private static List<LoadGenerator.Listener> loadGeneratorListeners;

    public static List<Resource.NodeListener> getNodeListeners() {
        return nodeListeners;
    }

    public static void setNodeListeners(List<Resource.NodeListener> nodeListeners) {
        JenkinsRemoteStarter.nodeListeners = nodeListeners;
    }

    public static List<LoadGenerator.Listener> getLoadGeneratorListeners() {
        return loadGeneratorListeners;
    }

    public static void setLoadGeneratorListeners(List<LoadGenerator.Listener> loadGeneratorListeners) {
        JenkinsRemoteStarter.loadGeneratorListeners = loadGeneratorListeners;
    }

    public static void main(String... args) throws Exception {
        String slaveAgentSocket = args[0];
        int i = slaveAgentSocket.indexOf(':');
        if (i > 0) {
            main(slaveAgentSocket.substring(0, i), Integer.parseInt(slaveAgentSocket.substring(i + 1)));
        } else {
            main(null, Integer.parseInt(slaveAgentSocket));
        }
    }

    public static void main(String agentIp, int tcpPort) throws Exception {
        final Socket s = new Socket(agentIp, tcpPort);

        ClassLoader classLoader = JenkinsRemoteStarter.class.getClassLoader();

        Class<?> remotingLauncher = classLoader.loadClass("hudson.remoting.Launcher");

        remotingLauncher.getMethod("main", 
                new Class[]{InputStream.class, OutputStream.class}).invoke( 
                null,
                // do partial close, since socket.getInputStream and
                // getOutputStream doesn't do it by
                new BufferedInputStream(
                        new FilterInputStream(s.getInputStream()) {
                            public void close() throws IOException {
                                s.shutdownInput();
                            }
                        }),
                new BufferedOutputStream(
                        new RealFilterOutputStream(s.getOutputStream()) {
                            public void close() throws IOException {
                                s.shutdownOutput();
                            }
                        })
        );
        System.exit(0);
    }

    static class RealFilterOutputStream extends FilterOutputStream {
        public RealFilterOutputStream(OutputStream core) {
            super(core);
        }

        public void write(byte[] b) throws IOException {
            out.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        public void close() throws IOException {
            out.close();
        }
    }

    public static void launch(List<String> argsList) throws Exception {
        final String[] args = argsList.toArray(new String[argsList.size()]);
        LoadGeneratorStarterArgs starterArgs = LoadGeneratorStarter.parse(args);
        LoadGenerator.Builder builder = LoadGeneratorStarter.prepare(starterArgs);
        if (nodeListeners != null) {
            nodeListeners.forEach(builder::resourceListener);
        }
        if (loadGeneratorListeners != null) {
            loadGeneratorListeners.forEach(builder::listener);
        }
        QpsListenerDisplay qpsListenerDisplay = new QpsListenerDisplay(10, 30, TimeUnit.SECONDS);
        RequestQueuedListenerDisplay requestQueuedListenerDisplay = new RequestQueuedListenerDisplay(10, 30, TimeUnit.SECONDS);
        builder.listener(qpsListenerDisplay).listener(requestQueuedListenerDisplay)
                .requestListener(qpsListenerDisplay).requestListener(requestQueuedListenerDisplay);
        LoadGeneratorStarter.run(builder);
    }
}
