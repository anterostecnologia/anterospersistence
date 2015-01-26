/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.metadata.configuration;

import br.com.anteros.persistence.metadata.descriptor.type.ConnectivityType;
import br.com.anteros.synchronism.annotation.Remote;
import br.com.anteros.synchronism.annotation.RemoteParam;

public class RemoteConfiguration {

	private String mobileActionExport = "";
	private String mobileActionImport = "";
	private String displayLabel = "";
	private RemoteParamConfiguration[] importParams = {};
	private RemoteParamConfiguration[] exportParams = {};
	private int exportOrderToSendData = 0;
	private String[] exportColumns;
	private ConnectivityType importConnectivityType;
	private ConnectivityType exportConnectivityType;

	public RemoteConfiguration() {
	}

	public RemoteConfiguration(String displayLabel, String mobileActionExport, String mobileActionImport, RemoteParamConfiguration[] importParams,
			RemoteParamConfiguration[] exportParams, int exportOrderToSendData, String[] exportColumns, ConnectivityType importConnectivityType, ConnectivityType exportConnectivityType) {
		this.mobileActionExport = mobileActionExport;
		this.mobileActionImport = mobileActionImport;
		this.displayLabel = displayLabel;
		this.importParams = importParams;
		this.exportParams = exportParams;
		this.exportOrderToSendData = exportOrderToSendData;
		this.exportColumns = exportColumns;
		this.importConnectivityType = importConnectivityType;
		this.exportConnectivityType = exportConnectivityType;
	}

	public RemoteConfiguration(Remote remote) {
		this.mobileActionImport = remote.mobileActionImport();
		this.displayLabel = remote.displayLabel();
		RemoteParam[] impParams = remote.importParams();
		if (impParams != null) {
			importParams = new RemoteParamConfiguration[impParams.length];
			for (int i = 0; i < impParams.length; i++)
				importParams[i] = new RemoteParamConfiguration(impParams[i].paramName(), impParams[i].paramValue(), impParams[i].paramOrder());
		}

		this.mobileActionExport = remote.mobileActionExport();
		RemoteParam[] expParams = remote.exportParams();
		if (expParams != null) {
			exportParams = new RemoteParamConfiguration[expParams.length];
			for (int i = 0; i < expParams.length; i++)
				exportParams[i] = new RemoteParamConfiguration(expParams[i].paramName(), expParams[i].paramValue(), expParams[i].paramOrder());
		}
		
		this.exportColumns = remote.exportColumns();
		this.exportOrderToSendData = remote.exportOrderToSendData();
		this.importConnectivityType = remote.importConnectivityType();
		this.exportConnectivityType = remote.exportConnectivityType();

	}

	public String getMobileActionExport() {
		return mobileActionExport;
	}

	public RemoteConfiguration mobileActionExport(String mobileActionExport) {
		this.mobileActionExport = mobileActionExport;
		return this;
	}

	public String getMobileActionImport() {
		return mobileActionImport;
	}

	public RemoteConfiguration mobileActionImport(String mobileActionImport) {
		this.mobileActionImport = mobileActionImport;
		return this;
	}

	public RemoteParamConfiguration[] getImportParams() {
		return importParams;
	}

	public RemoteConfiguration importParams(RemoteParamConfiguration[] importParams) {
		this.importParams = importParams;
		return this;
	}

	public RemoteConfiguration importParams(RemoteParamConfiguration param) {
		this.importParams = new RemoteParamConfiguration[] { param };
		return this;
	}

	public RemoteParamConfiguration[] getExportParams() {
		return exportParams;
	}

	public RemoteConfiguration exportParams(RemoteParamConfiguration[] exportParams) {
		this.exportParams = exportParams;
		return this;
	}

	public RemoteConfiguration exportParams(RemoteParamConfiguration param) {
		this.exportParams = new RemoteParamConfiguration[] { param };
		return this;
	}

	public int getExportOrderToSendData() {
		return exportOrderToSendData;
	}

	public RemoteConfiguration exportOrderToSendData(int exportOrderToSendData) {
		this.exportOrderToSendData = exportOrderToSendData;
		return this;
	}

	public String[] getExportColumns() {
		return exportColumns;
	}

	public RemoteConfiguration exportColumns(String[] exportColumns) {
		this.exportColumns = exportColumns;
		return this;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public RemoteConfiguration displayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
		return this;
	}

	public ConnectivityType getImportConnectivityType() {
		return importConnectivityType;
	}

	public void importConnectivityType(ConnectivityType importConnectivityType) {
		this.importConnectivityType = importConnectivityType;
	}

	public ConnectivityType getExportConnectivityType() {
		return exportConnectivityType;
	}

	public void exportConnectivityType(ConnectivityType exportConnectivityType) {
		this.exportConnectivityType = exportConnectivityType;
	}
}
