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
package edu.unc.lib.dl.cdr.services;

import java.io.InputStream;
import java.net.SocketException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * @author Gregory Jansen
 *
 */
public abstract class AbstractIrodsObjectEnhancementService extends AbstractFedoraEnhancementService {
	private IRODSAccount irodsAccount = null;

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public void setIrodsAccount(IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	public InputStream remoteExecuteWithPhysicalLocation(String command, String path) throws Exception {
		RemoteExecutionOfCommandsAO rexecAO = null;
		rexecAO = IRODSFileSystem.instance().getIRODSAccessObjectFactory()
				.getRemoteExecutionOfCommandsAO(this.getIrodsAccount());
		try {
			return rexecAO.executeARemoteCommandAndGetStreamAddingPhysicalPathAsFirstArgumentToRemoteScript(command, "",
					path);
		} catch (JargonException e) {
			recycleConnection();
			return rexecAO.executeARemoteCommandAndGetStreamAddingPhysicalPathAsFirstArgumentToRemoteScript(command, "",
					path);
		}
	}

	public InputStream remoteExecuteWithPhysicalLocation(String command, String arguments, String path) throws Exception {
		LOG.debug("iexecmd -P " + path + " \"" + command + " " + arguments + "\"");
		RemoteExecutionOfCommandsAO rexecAO = null;
		rexecAO = IRODSFileSystem.instance().getIRODSAccessObjectFactory()
				.getRemoteExecutionOfCommandsAO(this.getIrodsAccount());
		try {
			return rexecAO.executeARemoteCommandAndGetStreamAddingPhysicalPathAsFirstArgumentToRemoteScript(command,
					arguments, path);
		} catch (JargonException e) {
			recycleConnection();
			return rexecAO.executeARemoteCommandAndGetStreamAddingPhysicalPathAsFirstArgumentToRemoteScript(command, "",
					path);
		}
	}

	/**
	 *
	 */
	private void recycleConnection() {
			try {
				IRODSFileSystem.instance().closeAndEatExceptions(this.getIrodsAccount());
			} catch (JargonException e) {
				LOG.error("Trouble recycling iRODS connection "+e.getLocalizedMessage(), e);
				e.printStackTrace();
			}
	}

	// TODO Implement iRods API call to delete a file from the staging area.
	public void deleteIRODSFile(String path) throws JargonException {
		IRODSFileSystem irodsFileSystem;
		try {
			irodsFileSystem = IRODSFileSystem.instance();
			IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(path);
			irodsFileSystem.getIRODSAccessObjectFactory().getIRODSFileSystemAO(irodsAccount).fileDeleteNoForce(irodsFile);
		} catch (JargonException e) {
			recycleConnection();
			irodsFileSystem = IRODSFileSystem.instance();
			IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(path);
			irodsFileSystem.getIRODSAccessObjectFactory().getIRODSFileSystemAO(irodsAccount).fileDeleteNoForce(irodsFile);
		}
	}

	public String makeIrodsURIFromPath(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append("irods://").append(this.getIrodsAccount().getUserName()).append("@")
				.append(this.getIrodsAccount().getHost()).append(":").append(this.getIrodsAccount().getPort()).append(path);
		return sb.toString();
	}

}
