package com.github.se_bastiaan.torrentstream;


import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.TorrentStatus;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.PieceFinishedAlert;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Torrent implements AlertListener {
    private final static Integer MAX_PREPARE_COUNT = 20;
    private final static Integer MIN_PREPARE_COUNT = 2;
    private final static Integer DEFAULT_PREPARE_COUNT = 5;
    private final static Integer SEQUENTIAL_CONCURRENT_PIECES_COUNT = 5;

    private List<TorrentFileInfo> fileList;

    private List<WeakReference<TorrentInputStream>> torrentStreamReferences;


    private final TorrentHandle torrentHandle;
    private final TorrentListener listener;

    /**
     * The constructor for a new Torrent
     * <p/>
     * First the largest file in the download is selected as the file for playback
     * <p/>
     * After setting this priority, the first and last index of the pieces that make up this file are determined.
     * And last: amount of pieces that are needed for playback are calculated (needed for playback means: make up 10 megabyte of the file)
     *
     * @param torrentHandle jlibtorrent TorrentHandle
     */
    public Torrent(TorrentHandle torrentHandle, TorrentListener listener, Long prepareSize) {
        this.torrentHandle = torrentHandle;
        this.listener = listener;


        torrentStreamReferences = new ArrayList<>();

        initFileInfos(torrentHandle,prepareSize);

        if (this.listener != null) {
        }
    }

    public Torrent(Integer selectedFileIndex,TorrentHandle torrentHandle, TorrentListener listener, Long prepareSize) {
        this.torrentHandle = torrentHandle;
        this.listener = listener;

        torrentStreamReferences = new ArrayList<>();

        initFileInfos(torrentHandle,prepareSize);

        if (selectedFileIndex==null||selectedFileIndex == -1) {
            setLargestFile();
        }else {
            setSelectedFileIndex(selectedFileIndex);
        }
        if (this.listener != null) {
            this.listener.onStreamPrepared(this, selectedFileIndex);
        }
    }

    public InputStream getVideoStream(int i) throws FileNotFoundException {
        TorrentFileInfo fileInfo=fileList.get(i);
        File file = fileInfo.getFile();
        TorrentInputStream inputStream = new TorrentInputStream(fileInfo,torrentHandle, new FileInputStream(file));
        torrentStreamReferences.add(new WeakReference<>(inputStream));

        return inputStream;
    }

    private void initFileInfos(TorrentHandle torrentHandle, Long prepareSize) {
        fileList=new ArrayList<>();
        TorrentInfo torrentInfo= torrentHandle.torrentFile();
        FileStorage fs= torrentInfo.files();
        for (int i = 0; i < fs.numFiles(); i++) {
            String name=fs.fileName(i);
            long size = fs.fileSize(i);
            int firstPiece = torrentInfo.mapFile(i, 0, 1).piece();
            int lastPiece = torrentInfo.mapFile(i, size - 1, 1).piece();

            Map<Integer,Boolean> downloadMap=new HashMap<>();
            for (int j = firstPiece; j <=lastPiece ; j++) {
                downloadMap.put(j,false);
            }
            TorrentFileInfo info=new TorrentFileInfo();
            info.setFileName(name);
            info.setIndex(i);
            info.setFirstPiece(firstPiece);
            info.setLastPiece(lastPiece);
            info.setSize(size);
            info.setDownloadMap(downloadMap);
            info.setPriority(Priority.IGNORE);
            String saveFolder= torrentHandle.savePath();
            info.setSaveFolder(saveFolder);


            int pieceCount = lastPiece - firstPiece + 1;
            int pieceLength = torrentHandle.torrentFile().pieceLength();
            int activePieceCount;
            if (pieceLength > 0) {
                activePieceCount = (int) (prepareSize / pieceLength);
                if (activePieceCount < MIN_PREPARE_COUNT) {
                    activePieceCount = MIN_PREPARE_COUNT;
                } else if (activePieceCount > MAX_PREPARE_COUNT) {
                    activePieceCount = MAX_PREPARE_COUNT;
                }
            } else {
                activePieceCount = DEFAULT_PREPARE_COUNT;
            }
            if (pieceCount < activePieceCount) {
                activePieceCount = pieceCount / 2;
            }
            info.setPrepareSize(activePieceCount);
            fileList.add(info);
        }
    }

    private void finishPiece(TorrentHandle torrentHandle, TorrentListener listener, int pieceIndex){
        for (TorrentFileInfo info : fileList) {
           boolean r= info.finishPiece(this,torrentHandle,listener,pieceIndex);
            if(r){
                sendStreamProgress(info.getIndex());
            }
        }
    }


    public TorrentFileInfo getFileInfo(int i){
        return fileList.get(i);
    }



    public float progress(){
        int total=0,finish=0;
        for (TorrentFileInfo info : fileList) {
            if(info.needDownload()){
                total+=info.pieceNum();
                finish+=info.getFinishNum();
            }
        }
        if(total==0){
            return 0f;
        }else {
            return 1.0f*finish/total;
        }
    }

    /**
     * Reset piece priorities of selected file to normal
     */
    private void resetPriorities() {
        for (TorrentFileInfo info : fileList) {
            if(info.needDownload()){
                info.setPriority(Priority.NORMAL);
            }
        }
    }


    /**
     * Pause the torrent download
     */
    public void pause() {
        torrentHandle.pause();
    }

    /**
     * Set the selected file index to the largest file in the torrent
     */
    public void setLargestFile() {
        TorrentFileInfo maxFile=null;
        long maxSize=0;
        for (TorrentFileInfo info : fileList) {
            info.setPriority(Priority.IGNORE);
            if(info.getSize()>maxSize){
                maxFile=info;
                maxSize=info.getSize();
            }
        }
        maxFile.setPriority(Priority.NORMAL);
    }

    /**
     * Set the index of the file that should be downloaded
     * If the given index is -1, then the largest file is chosen
     *
     * @param selectedFileIndex {@link Integer} Index of the file
     */
    public void setSelectedFileIndex(Integer selectedFileIndex) {
        TorrentFileInfo selected=null;
        for (TorrentFileInfo info : fileList) {
            if(info.getIndex()==selectedFileIndex){
                selected=info;

            }
            info.setPriority(Priority.IGNORE);
        }
        selected.setPriority(Priority.NORMAL);
        selected.start(this,torrentHandle,listener);
    }

    /**
     * Prepare torrent for playback. Prioritize the first {@code piecesToPrepare} pieces and the last {@code piecesToPrepare} pieces
     * from {@code firstPieceIndex} and {@code lastPieceIndex}. Ignore all other pieces.
     */
    public void startDownload() {
        for (TorrentFileInfo info : fileList) {
            if(info.needDownload()){
                info.start(this,torrentHandle,listener);
            }
        }
        torrentHandle.resume();
    }

    public void addDownloadFile(int i){
        for (TorrentFileInfo info : fileList) {
            if(!info.needDownload()&&info.getIndex()==i){
                info.setPriority(Priority.NORMAL);
                info.start(this, torrentHandle, listener);
                this.listener.onStreamPrepared(this, i);
                break;
            }
        }
    }






    /**
     * Piece finished
     *
     * @param alert
     */
    private void pieceFinished(PieceFinishedAlert alert) {
        int pIndex = alert.pieceIndex();
        finishPiece(torrentHandle,listener,pIndex);
    }

    private void blockFinished(BlockFinishedAlert alert) {
        int pIndex = alert.pieceIndex();
        for (TorrentFileInfo info : fileList) {
            if(info.getFirstPiece()<=pIndex&&info.getLastPiece()>=pIndex){
                sendStreamProgress(info.getIndex());
            }
        }

    }

    private void sendStreamProgress(int i) {
        TorrentStatus status = torrentHandle.status();
        float progress = status.progress() * 100;
        int seeds = status.numSeeds();
        int downloadSpeed = status.downloadPayloadRate();

        if (listener != null ) {
            listener.onStreamProgress(this, new StreamStatus(progress, 0, seeds, downloadSpeed),i);
        }
    }

    @Override
    public int[] types() {
        return new int[]{
                AlertType.PIECE_FINISHED.swig(),
                AlertType.BLOCK_FINISHED.swig(),
                AlertType.PEER_CONNECT.swig(),
                AlertType.PEER_DISCONNECTED.swig(),
                AlertType.SESSION_STATS.swig()
        };
    }

    @Override
    public void alert(Alert<?> alert) {
        switch (alert.type()) {
            case PIECE_FINISHED:
                pieceFinished((PieceFinishedAlert) alert);
                break;
            case BLOCK_FINISHED:
                blockFinished((BlockFinishedAlert) alert);
                break;
            default:
                if(listener!=null){
                    listener.fireAlert(alert);
                }
                break;
        }

        Iterator<WeakReference<TorrentInputStream>> i = torrentStreamReferences.iterator();

        while (i.hasNext()) {
            WeakReference<TorrentInputStream> reference = i.next();
            TorrentInputStream inputStream = reference.get();

            if (inputStream == null) {
                i.remove();
                continue;
            }

            inputStream.alert(alert);
        }
    }

    public TorrentHandle getTorrentHandle() {
        return torrentHandle;
    }

    public File getSaveLocation() {
        return new File(torrentHandle.savePath() + "/" + torrentHandle.name());
    }
}
