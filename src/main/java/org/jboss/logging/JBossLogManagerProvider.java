/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging;

import java.util.Map;
import org.jboss.logmanager.MDC;
import org.jboss.logmanager.NDC;

import static org.jboss.logmanager.Logger.AttachmentKey;

final class JBossLogManagerProvider implements LoggerProvider {

    private static final AttachmentKey<Logger> KEY = new AttachmentKey<Logger>();

    public Logger getLogger(final String name, final String resourceBundleName, final String prefix) {
        final org.jboss.logmanager.Logger logger;
        if (resourceBundleName != null) {
            logger = org.jboss.logmanager.Logger.getLogger(name, resourceBundleName);
        } else {
            logger = org.jboss.logmanager.Logger.getLogger(name);
        }
        Logger l = logger.getAttachment(KEY);
        for (;;) {
            if (l != null) {
                if (prefix == null) {
                    if (l.getPrefix() == null) {
                        return l;
                    }
                } else if (prefix.equals(l.getPrefix())) {
                    return l;
                }
                return new JBossLogManagerLogger(name, resourceBundleName, prefix, logger);
            } else {
                l = new JBossLogManagerLogger(name, resourceBundleName, prefix, logger);
                Logger a = logger.attachIfAbsent(KEY, l);
                if (a == null) {
                    return l;
                }
                l = a;
                // try again...
            }
        }
    }

    public void putMdc(final String key, final Object value) {
        MDC.put(key, String.valueOf(value));
    }

    public Object getMdc(final String key) {
        return MDC.get(key);
    }

    public void removeMdc(final String key) {
        MDC.remove(key);
    }

    @SuppressWarnings({ "unchecked" })
    public Map<String, Object> getMdcMap() {
        // we can re-define the erasure of this map because MDC does not make further use of the copy
        return (Map)MDC.copy();
    }

    public void clearNdc() {
        NDC.clear();
    }

    public String getNdc() {
        return NDC.get();
    }

    public int getNdcDepth() {
        return NDC.getDepth();
    }

    public String popNdc() {
        return NDC.pop();
    }

    public String peekNdc() {
        return NDC.get();
    }

    public void pushNdc(final String message) {
        NDC.push(message);
    }

    public void setNdcMaxDepth(final int maxDepth) {
        NDC.trimTo(maxDepth);
    }
}