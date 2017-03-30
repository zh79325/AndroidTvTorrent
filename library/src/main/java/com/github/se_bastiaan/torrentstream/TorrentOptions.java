/*
 * Copyright (C) 2015-2016 Sébastiaan (github.com/se-bastiaan)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.se_bastiaan.torrentstream;

import java.io.File;

public final class TorrentOptions {

    protected String saveLocation = "/";
    protected String proxyHost;
    protected String proxyUsername;
    protected String proxyPassword;
    protected String peerFingerprint;
    protected Integer maxDownloadSpeed = 0;
    protected Integer maxUploadSpeed = 0;
    protected Integer maxConnections = 200;
    protected Integer maxDht = 88;
    protected Integer listeningPort = -1;
    protected Boolean removeFiles = false;
    protected Boolean anonymousMode = false;
    protected Long prepareSize = 15 * 1024L * 1024L;

    private TorrentOptions() {
        // Unused
    }

    private TorrentOptions(TorrentOptions torrentOptions) {
        this.saveLocation = torrentOptions.saveLocation;
        this.proxyHost = torrentOptions.proxyHost;
        this.proxyUsername = torrentOptions.proxyUsername;
        this.proxyPassword = torrentOptions.proxyPassword;
        this.peerFingerprint = torrentOptions.peerFingerprint;
        this.maxDownloadSpeed = torrentOptions.maxDownloadSpeed;
        this.maxUploadSpeed = torrentOptions.maxUploadSpeed;
        this.maxConnections = torrentOptions.maxConnections;
        this.maxDht = torrentOptions.maxDht;
        this.listeningPort = torrentOptions.listeningPort;
        this.removeFiles = torrentOptions.removeFiles;
        this.anonymousMode = torrentOptions.anonymousMode;
        this.prepareSize = torrentOptions.prepareSize;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {

        private TorrentOptions torrentOptions;

        public Builder() {
            torrentOptions = new TorrentOptions();
        }

        private Builder(TorrentOptions torrentOptions) {
            torrentOptions = new TorrentOptions(torrentOptions);
        }

        public Builder saveLocation(String saveLocation) {
            torrentOptions.saveLocation = saveLocation;
            return this;
        }

        public Builder saveLocation(File saveLocation) {
            torrentOptions.saveLocation = saveLocation.getAbsolutePath();
            return this;
        }

        public Builder maxUploadSpeed(Integer maxUploadSpeed) {
            torrentOptions.maxUploadSpeed = maxUploadSpeed;
            return this;
        }

        public Builder maxDownloadSpeed(Integer maxDownloadSpeed) {
            torrentOptions.maxDownloadSpeed = maxDownloadSpeed;
            return this;
        }

        public Builder maxConnections(Integer maxConnections) {
            torrentOptions.maxConnections = maxConnections;
            return this;
        }

        public Builder maxActiveDHT(Integer maxActiveDHT) {
            torrentOptions.maxDht = maxActiveDHT;
            return this;
        }

        public Builder removeFilesAfterStop(Boolean b) {
            torrentOptions.removeFiles = b;
            return this;
        }

        public Builder prepareSize(Long prepareSize) {
            torrentOptions.prepareSize = prepareSize;
            return this;
        }

        public Builder listeningPort(Integer port) {
            torrentOptions.listeningPort = port;
            return this;
        }

        public Builder proxy(String host, String username, String password) {
            torrentOptions.proxyHost = host;
            torrentOptions.proxyUsername = username;
            torrentOptions.proxyPassword = password;
            return this;
        }

        public Builder peerFingerprint(String peerId) {
            torrentOptions.peerFingerprint = peerId;
            torrentOptions.anonymousMode = false;
            return this;
        }

        public Builder anonymousMode(Boolean enable) {
            torrentOptions.anonymousMode = enable;
            if (torrentOptions.anonymousMode)
                torrentOptions.peerFingerprint = null;
            return this;
        }

        public TorrentOptions build() {
            return torrentOptions;
        }

    }

}
