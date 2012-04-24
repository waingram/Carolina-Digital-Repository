/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.unc.lib.dl.cdr.services.processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.unc.lib.dl.cdr.services.model.CDREventMessage;
import edu.unc.lib.dl.cdr.services.model.FedoraEventMessage;
import edu.unc.lib.dl.cdr.services.util.JMSMessageUtil;
import edu.unc.lib.dl.data.ingest.solr.SolrUpdateAction;
import edu.unc.lib.dl.data.ingest.solr.SolrUpdateRequest;
import edu.unc.lib.dl.data.ingest.solr.SolrUpdateRunnable;
import edu.unc.lib.dl.data.ingest.solr.SolrUpdateService;
import edu.unc.lib.dl.message.ActionMessage;

public class SolrUpdateConductor extends SolrUpdateService implements MessageConductor, ServiceConductor {
	private long beforeExecuteDelay = 50;

	@SuppressWarnings("unchecked")
	@Override
	protected void initializeExecutor(){
		LOG.debug("Initializing services thread pool executor with " + this.maxThreads + " threads.");
		this.executor = new ServicesThreadPoolExecutor<SolrUpdateRunnable>(this.maxThreads, this.getIdentifier());
		this.executor.setKeepAliveTime(0, TimeUnit.DAYS);
		((ServicesThreadPoolExecutor<SolrUpdateRunnable>)this.executor).setBeforeExecuteDelay(beforeExecuteDelay);
	}

	@Override
	public void add(ActionMessage message) {
		if (message instanceof SolrUpdateRequest){
			this.offer((SolrUpdateRequest)message);
			return;
		}
		String action = message.getQualifiedAction();
		if (action == null)
			return;
		
		if (message instanceof FedoraEventMessage){
			if (JMSMessageUtil.FedoraActions.PURGE_OBJECT.equals(action)){
				this.offer(message.getTargetID(), SolrUpdateAction.DELETE_SOLR_TREE);
			} else {
				this.offer(message.getTargetID());
			}
		} else if (message instanceof CDREventMessage){
			CDREventMessage cdrMessage = (CDREventMessage)message;
			if (JMSMessageUtil.CDRActions.MOVE.equals(action) || JMSMessageUtil.CDRActions.ADD.equals(action)
					|| JMSMessageUtil.CDRActions.REORDER.equals(action)){
				if (JMSMessageUtil.CDRActions.MOVE.equals(action) || JMSMessageUtil.CDRActions.ADD.equals(action)){
					//Move and add are both recursive adds of all subjects, plus a nonrecursive update for reordered children.
					for (String pidString: cdrMessage.getSubjects()){
						this.offer(new SolrUpdateRequest(pidString, SolrUpdateAction.RECURSIVE_ADD));
					}
				}
				// Reorder is a non-recursive add.
				for (String pidString: cdrMessage.getReordered()){
					this.offer(new SolrUpdateRequest(pidString, SolrUpdateAction.ADD));
				}
			} else if (JMSMessageUtil.CDRActions.REINDEX.equals(action)){
				//Determine which kind of reindex to perform based on the mode
				if (cdrMessage.getMode().equals("inplace")){
					this.offer(new SolrUpdateRequest(cdrMessage.getParent(), SolrUpdateAction.RECURSIVE_REINDEX));
				} else {
					this.offer(new SolrUpdateRequest(cdrMessage.getParent(), SolrUpdateAction.CLEAN_REINDEX));
				}
			}
		} else {
			//For all other message types, do a single record update
			this.offer(message.getTargetID());
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void pause() {
		((ServicesThreadPoolExecutor)this.executor).pause();

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void resume() {
		((ServicesThreadPoolExecutor)this.executor).resume();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isPaused(){
		return ((ServicesThreadPoolExecutor)this.executor).isPaused();
	}

	public Map<String, Object> getInfo() {
		// TODO put values in separate keys
		Map<String, Object> result = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		sb.append("Solr Update Conductor Status:\n")
			.append("Paused: " + isPaused() + "\n")
			.append("PID Queue: " + this.pidQueue.size() + "\n")
			.append("Collision List: " + this.collisionList.size() + "\n")
			.append("Locked pids: " + this.lockedPids.size() + "\n")
			.append("Executor: " + executor.getActiveCount() + " active workers, " + executor.getQueue().size() + " queued");
		result.put("message", sb.toString());
		return result;
	}

	@Override
	public String queuesToString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Solr Update Conductor Queues:\n")
			.append("PID Queue: " + this.pidQueue + "\n")
			.append("Collision List: " + this.collisionList + "\n")
			.append("Locked pids: " + this.lockedPids + "\n");
		return sb.toString();
	}

	@Override
	public int getQueueSize() {
		return this.pidQueue.size() + this.collisionList.size();
	}

	@Override
	public synchronized void clearQueue(){
		this.pidQueue.clear();
		this.collisionList.clear();
	}

	@Override
	public synchronized void clearState(){
		this.pidQueue.clear();
		this.collisionList.clear();
		this.lockedPids.clear();
		executor.getQueue().clear();
	}

	@Override
	public boolean isEmpty() {
		return this.pidQueue.size() == 0 && this.collisionList.size() == 0 && this.lockedPids.size() == 0;
	}

	@Override
	public boolean isIdle(){
		return isPaused() || this.lockedPids.size() == 0;
	}

	@Override
	public boolean isReady(){
		return !this.executor.isShutdown() && !this.executor.isTerminated() && !this.executor.isTerminating();
	}

	@Override
	public void shutdown() {
		this.executor.shutdown();
		this.clearQueue();
		this.lockedPids.clear();
		LOG.warn("Solr Update conductor is shutting down, no further objects will be received");
	}

	@Override
	public void shutdownNow() {
		this.executor.shutdownNow();
		this.clearQueue();
		this.lockedPids.clear();
		LOG.warn("Solr Update conductor is shutting down now, interrupting future processing");
	}

	@Override
	public synchronized void abort() {
		this.lockedPids.clear();
		//Perform hard shutdown and wait for it to finish
		List<Runnable> runnables = this.executor.shutdownNow();
		while (this.executor.isTerminating() && !this.executor.isShutdown());
		//restart and pause the executor
		initializeExecutor();
		pause();
		//Pass the old runnables on to the new executor.
		if (runnables != null){
			for (Runnable runnable: runnables){
				this.executor.submit(runnable);
			}
		}
	}

	@Override
	public void restart() {
		if (this.executor == null || this.executor.isShutdown() || this.executor.isTerminated())
			initializeExecutor();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	
	@SuppressWarnings("unchecked")
	public ServicesThreadPoolExecutor<SolrUpdateRunnable> getThreadPoolExecutor(){
		return (ServicesThreadPoolExecutor<SolrUpdateRunnable>)this.executor;
	}

	public long getBeforeExecuteDelay() {
		return beforeExecuteDelay;
	}

	public void setBeforeExecuteDelay(long beforeExecuteDelay) {
		this.beforeExecuteDelay = beforeExecuteDelay;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.dl.cdr.services.processing.ServiceConductor#getActiveThreadCount()
	 */
	@Override
	public int getActiveThreadCount() {
		return this.executor.getActiveCount();
	}
}
