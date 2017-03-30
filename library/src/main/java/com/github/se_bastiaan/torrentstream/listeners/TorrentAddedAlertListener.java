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

package com.github.se_bastiaan.torrentstream.listeners;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.TorrentAddedAlert;

public abstract class TorrentAddedAlertListener implements AlertListener {
    @Override
    public int[] types() {
        return new int[]{AlertType.TORRENT_ADDED.swig()};
    }

    @Override
    public void alert(Alert<?> alert) {
        switch (alert.type()) {
            case TORRENT_ADDED:
                torrentAdded((TorrentAddedAlert) alert);
                break;
            default:
                break;
        }
    }

    public abstract void torrentAdded(TorrentAddedAlert alert);
}
