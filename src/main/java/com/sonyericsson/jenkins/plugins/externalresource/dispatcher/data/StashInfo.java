/*
 *  The MIT License
 *
 *  Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonyericsson.jenkins.plugins.externalresource.dispatcher.data;

import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.Constants;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Information about the "stashed" status of an {@link ExternalResource}. I.e. The lock and reservation status.
 *
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class StashInfo implements Serializable, Cloneable {
    private String stashedBy;
    private StashType type;
    private Lease lease;
    private String key;

    /**
     * Standard Constructor.
     *
     * @param type      the general type of what stashed the resource.
     * @param stashedBy exactly what stashed it.
     * @param lease     when will it end.
     * @param key       the key to use when releasing.
     */
    public StashInfo(StashType type, String stashedBy, Lease lease, String key) {
        this.stashedBy = stashedBy;
        this.type = type;
        this.lease = lease;
        this.key = key;
    }

    /**
     * Creates a new object with info from the StashResult. The type will be defaulted to {@link
     * StashInfo.StashType#INTERNAL}.
     *
     * @param result    the result from a {@link
     *  com.sonyericsson.jenkins.plugins.externalresource.dispatcher.utils.resourcemanagers.ExternalResourceManager}
     *                  operation.
     * @param stashedBy the build that it belongs to.
     */
    public StashInfo(StashResult result, String stashedBy) {
        this.stashedBy = stashedBy;
        this.type = StashType.INTERNAL;
        this.key = result.getKey();
        this.lease = result.getLease();
    }

    @Override
    public StashInfo clone() throws CloneNotSupportedException {
        StashInfo other = (StashInfo)super.clone();
        if (this.lease != null) {
            other.lease = this.lease.clone();
        }
        return other;
    }

    /**
     * Exactly what stashed it. If {@link #getType()} is {@link StashType#INTERNAL} then this points to a build on the
     * local Jenkins server.
     *
     * @return the name or something else.
     */
    public String getStashedBy() {
        return stashedBy;
    }

    /**
     * General type info about what/who stashed the resource.
     *
     * @return the type.
     */
    public StashType getType() {
        return type;
    }

    /**
     * Information about when the lease expires.
     *
     * @return the lease info.
     */
    public Lease getLease() {
        return lease;
    }

    /**
     * The key to release or lock the resource.
     *
     * @return the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * If {@link #getType()} is internal or not. Added for simplified usage in jelly.
     *
     * @return true if so.
     */
    public boolean isInternal() {
        return getType() == StashType.INTERNAL;
    }

    /**
     * Returns true if the stashedBy String is a valid URL.
     *
     * @return true if URL, false if not.
     */
    public boolean stashedByIsURL() {
        try {
            URL url = new URL(getStashedBy());
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Returns a JSON representation of this object.
     *
     * @return the object in JSON format.
     *
     * @see com.sonyericsson.jenkins.plugins.externalresource.dispatcher.data.ExternalResource#toJson()
     * @see com.sonyericsson.jenkins.plugins.externalresource.dispatcher.data.Lease#toJson()
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put(Constants.JSON_ATTR_STASHED_BY, stashedBy);
        json.put(Constants.JSON_ATTR_TYPE, type.name());
        if (lease != null) {
            json.put(Constants.JSON_ATTR_LEASE, lease.toJson());
        } else {
            json.put(Constants.JSON_ATTR_LEASE, new JSONObject(true));
        }
        json.put(Constants.JSON_ATTR_KEY, key);
        return json;
    }


    /**
     * The general type of what stashed the resource.
     */
    public static enum StashType {
        /**
         * The resource is stashed by Jenkins, so stashedBy should point to a build.
         */
        INTERNAL,
        /**
         * The resource is stashed by something external, so stashed by points to something else.
         */
        EXTERNAL
    }
}
