package fedorax.server.module.storage.lowlevel.irods;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.fcrepo.server.errors.LowlevelStorageException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fedorax.server.module.storage.lowlevel.irods.IrodsIFileSystem.PathReentrantLock;

public class IrodsIFileSystemLockTest extends Assert {
	private static final Logger log = LoggerFactory.getLogger(IrodsIFileSystemLockTest.class);

	@Test
	public void lockTest() throws LowlevelStorageException, InterruptedException{
		final IrodsIFileSystem fileSystem = new IrodsIFileSystem(0, null, null);
		final String path1 = "/test/file";
		final String path2 = "/test/object";
		final StringBuffer completionOrder = new StringBuffer();
		final AtomicInteger noDelayCount = new AtomicInteger(0);
		
		Runnable longRunnable = new Runnable(){
			@Override
			public void run() {
				PathReentrantLock lock = null;
				try {
					lock = fileSystem.lockPath(path1);
					log.debug("lr:" + lock.hashCode());
					Thread.sleep(500);
					completionOrder.append("lr");
				} catch (InterruptedException e) {
				} finally {
					lock.unlock();
					log.debug("Completed longRunnable");
				}
			}
		};
		
		Runnable noDelayRunnable = new Runnable(){
			@Override
			public void run() {
				PathReentrantLock lock = null;
				try {
					lock = fileSystem.lockPath(path1);
					log.debug("nd:" + lock.hashCode());
					completionOrder.append('n').append(noDelayCount.incrementAndGet());
				} finally {
					lock.unlock();
					log.debug("Completed noDelayRunnable");
				}
			}
		};
		
		Runnable path2Runnable = new Runnable(){
			@Override
			public void run() {
				PathReentrantLock lock = null;
				try {
					lock = fileSystem.lockPath(path2);
					log.debug("p2:" + lock.hashCode());
					completionOrder.append("p2");
				} finally {
					lock.unlock();
					log.debug("Completed path2Runnable");
				}
			}
		};
		
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
		//Execute two threads on same path at sufficient delay that they do not overlap
		scheduler.schedule(path2Runnable, 0, TimeUnit.MILLISECONDS);
		scheduler.schedule(path2Runnable, 50, TimeUnit.MILLISECONDS);
		
		scheduler.awaitTermination(80, TimeUnit.MILLISECONDS);
		
		//Execute long running thread with 2 other threads trying to lock the same path
		scheduler.schedule(longRunnable, 0, TimeUnit.MILLISECONDS);
		scheduler.schedule(noDelayRunnable, 80, TimeUnit.MILLISECONDS);
		//And two interweaved threads trying to lock another path
		scheduler.schedule(path2Runnable, 100, TimeUnit.MILLISECONDS);
		scheduler.schedule(noDelayRunnable, 90, TimeUnit.MILLISECONDS);
		scheduler.schedule(path2Runnable, 160, TimeUnit.MILLISECONDS);
		
		//Execute two additional threads on different paths to make sure all paths are unlocked now
		scheduler.schedule(noDelayRunnable, 520, TimeUnit.MILLISECONDS);
		scheduler.schedule(path2Runnable, 530, TimeUnit.MILLISECONDS);
		
		scheduler.awaitTermination(1000, TimeUnit.MILLISECONDS);
		
		scheduler.shutdown();
		
		log.debug("Completion Order: " + completionOrder.toString());
		
		//Verify execution order
		assertTrue("p2p2p2p2lrn1n2n3p2".equals(completionOrder.toString()));
		
		
	}
}
