package org.example.hyperledgerfabric.app.javademo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.hyperledger.fabric.client.ChaincodeEvent;
import org.hyperledger.fabric.client.CloseableIterator;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;


@Slf4j
public class ChaincodeEventListener implements Runnable {

    @Autowired
    private Gateway Org1Gateway;

    public ChaincodeEventListener() {
        //ScheduledThreadPoolExecutor定时任务线程池
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName(this.getClass() + "Chaincode_Event_Listener");
                return thread;
            }
        });

        executor.execute(this);
    }

    @Override
    public void run() {
        CloseableIterator<ChaincodeEvent> events = Org1Gateway.getNetwork("businesschannel").getChaincodeEvents("hyperledger-fabric-contract-java-demo");
        log.info("chaincodeEvents {} " , events);

        // events.hasNext() 会无限期阻塞等待，直到获取到一个事件
        while (events.hasNext()) {
            ChaincodeEvent event = events.next();

            log.info("receive chaincode event {} , transaction id {} ,  block number {} , payload {} "
                    , event.getEventName() , event.getTransactionId() , event.getBlockNumber() , StringUtils.newStringUtf8(event.getPayload()));

        }
    }
}
