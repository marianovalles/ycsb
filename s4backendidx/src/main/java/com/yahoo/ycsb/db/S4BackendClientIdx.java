/**
 * S4 client binder for YCSB - 1st try
 * 
 * */

package com.yahoo.ycsb.db;

import org.apache.s4.ft.*;
import org.apache.s4.ft.SafeKeeper.StorageResultCode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.StringByteIterator;

public class S4BackendClientIdx extends com.yahoo.ycsb.DB {
    public IndexedFileSystemStateStorage storage = new IndexedFileSystemStateStorage();
    public static final String N_PES = "s4.npes" ;
	private static final String PAYLOAD = "throughput";
	private SyncThread st;
    public static int numPE;

    
    public void init() throws DBException{
        Properties props = getProperties();
        //int port;
        storage.setBufferSize(10*1024*1024);
        storage.setSyncRate(1000);
        storage.checkFileExists();
        st = new SyncThread(storage);
        st.start();
      	File root = new File(storage.getStorageRootPath());
      	storage.setIndex(new ConcurrentHashMap<SafeKeeperId,IndexedFileSystemStateStorage.CheckpointState>()); 
    	if(root.isDirectory() && root.listFiles().length > 0){
    		for (File f: root.listFiles()){
    			f.delete();
    		}
    	}
      
        
        numPE = Integer.parseInt(props.getProperty(N_PES));
        if ( numPE <= 0){
            numPE = 100;
        }
    }
    

    @Override
    public int delete(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    /**
     * TODO: A naive insert for now will only use the key parameter to send
     * to the S4 cluster and has "Word" harcoded and will need a word application
     */
    public int insert(String table, String key, HashMap<String, ByteIterator> values) {
    	
    	final CountDownLatch signalAllSaved = new CountDownLatch(1);
		storage.saveState(new SafeKeeperId("prototype", "key" + new Random().nextInt(numPE)), PAYLOAD.getBytes(), new StorageCallback() {
			@Override
			public void storageOperationResult(StorageResultCode resultCode, Object message, byte[] state) {
				signalAllSaved.countDown();
			}
		});
		try {
			signalAllSaved.await(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			return 1;
		}
		return 0;	
    }

    @Override
    public int read(String table, String key, Set<String> fields, HashMap<String, ByteIterator> result) {
    	
        return 0;
    }

    @Override
    public int scan(String arg0, String arg1, int arg2, Set<String> arg3, Vector<HashMap<String, ByteIterator>> arg4) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int update(String arg0, String arg1, HashMap<String, ByteIterator> arg2) {
        // TODO Auto-generated method stub
        return 0;
    }
}
