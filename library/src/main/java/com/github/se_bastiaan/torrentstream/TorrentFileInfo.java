package com.github.se_bastiaan.torrentstream;

import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zh_zhou on 2017/3/30.
 */

public class TorrentFileInfo {

    public boolean hasBytes(TorrentHandle torrentHandle, long offset) {
        int pieceIndex = (int) (offset / torrentHandle.torrentFile().pieceLength());
        pieceIndex+=firstPiece;
        if(downloadMap.containsKey(pieceIndex)){
            Boolean r=  downloadMap.get(pieceIndex);
            return Boolean.TRUE.equals(r);
        }else{
            return false;
        }
    }

    public enum State {UNKNOWN,WAITING, STARTING, STREAMING}
    private State state = State.WAITING;

    String fileName;
    int index;
    long size;
    int firstPiece;
    int lastPiece;
    Map<Integer,Boolean> downloadMap;
    int finishNum;
    Priority priority;

    int prepareSize;


    private Set<Integer> preparePieces;


    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void prepare(){
        preparePieces=new HashSet<>();
        if(prepareSize==0){
            preparePieces.add(firstPiece);
            return;
        }
        for (int i = 0; i < prepareSize; i++) {
            preparePieces.add(lastPiece - i);
        }
        for (int i = 0; i < prepareSize; i++) {
            preparePieces.add(firstPiece + i);
        }
    }

    public void start(Torrent torrent, TorrentHandle torrentHandle, TorrentListener listener){
        prepare();
        for (int i = firstPiece; i <= lastPiece; i++) {
            torrentHandle.piecePriority(i,Priority.NORMAL);
        }
        for (Integer piece : preparePieces) {
            torrentHandle.piecePriority(piece,Priority.SEVEN);
            torrentHandle.setPieceDeadline(piece, 1000);
        }
        state=State.STARTING;
        if(listener!=null){
            listener.onStreamStarted(torrent,index);
        }
    }

    public Set<Integer> getPreparePieces() {
        return preparePieces;
    }

    public void setPreparePieces(Set<Integer> preparePieces) {
        this.preparePieces = preparePieces;
    }

    public int getPrepareSize() {
        return prepareSize;
    }

    public void setPrepareSize(int prepareSize) {
        this.prepareSize = prepareSize;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getFinishNum() {
        return finishNum;
    }

    public void setFinishNum(int finishNum) {
        this.finishNum = finishNum;
    }

    public boolean needDownload() {
        return priority!=Priority.IGNORE;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getFirstPiece() {
        return firstPiece;
    }

    public void setFirstPiece(int firstPiece) {
        this.firstPiece = firstPiece;
    }

    public int getLastPiece() {
        return lastPiece;
    }

    public void setLastPiece(int lastPiece) {
        this.lastPiece = lastPiece;
    }

    public Map<Integer, Boolean> getDownloadMap() {
        return downloadMap;
    }

    public void setDownloadMap(Map<Integer, Boolean> downloadMap) {
        this.downloadMap = downloadMap;
    }

    public void finishPiece(Torrent torrentExtend, TorrentHandle torrentHandle, TorrentListener listener, int pieceIndex){
        Iterator<Integer> iterator= preparePieces.iterator();
        while (iterator.hasNext()){
            Integer piece =iterator.next();
            if(pieceIndex==piece){
                iterator.remove();
            }
        }
        if(downloadMap.containsKey(pieceIndex)){
            downloadMap.put(pieceIndex,true);
            finishNum++;
        }
        if(preparePieces.size()==0&&state==State.STARTING){
            state=State.STREAMING;
            listener.onStreamReady(torrentExtend,index);
        }else{
            int next=nextPiece(pieceIndex);
            if(next!=-1){
                torrentHandle.piecePriority(next,Priority.SEVEN);
                torrentHandle.setPieceDeadline(next, 1000);
            }
        }
        if(finishNum==downloadMap.size()){
            listener.onDownloadFinish(torrentExtend,index);
        }
    }

    boolean isStreamReady(){
        return  state==State.STREAMING;
    }
    public int nextPiece(int pieceIndex){
        if(pieceIndex<firstPiece||pieceIndex>lastPiece){
            return -1;
        }
        for (int i = firstPiece; i <= lastPiece; i++) {
            if(!Boolean.TRUE.equals(downloadMap.get(i))){
                return i;
            }
        }
        return -1;
    }

    public double progress(){
        if(downloadMap.size()==0){
            return 0d;
        }
        return 1.0*finishNum/downloadMap.size();
    }

    public int pieceNum(){
        return downloadMap.size();
    }

}
