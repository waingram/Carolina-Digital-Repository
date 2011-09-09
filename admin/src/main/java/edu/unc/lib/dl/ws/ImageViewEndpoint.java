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
package edu.unc.lib.dl.ws;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

import edu.unc.lib.dl.schema.ImageViewRequest;
import edu.unc.lib.dl.schema.ImageViewResponseList;
import edu.unc.lib.dl.schema.ItemInfoRequest;
import edu.unc.lib.dl.schema.ItemInfoResponse;
import edu.unc.lib.dl.service.DataService;
import edu.unc.lib.dl.service.ItemInfoService;
import edu.unc.lib.dl.util.Constants;

@Endpoint
public class ImageViewEndpoint extends WebServiceGatewaySupport {
    private final Logger logger = Logger.getLogger(getClass());
    private DataService dataService;

    @PayloadRoot(localPart = Constants.IMAGE_VIEW_REQUEST, namespace = Constants.NAMESPACE)
    public ImageViewResponseList getData(ImageViewRequest imageViewRequest) {

    logger.info("in ImageViewEndpoint");	
    logger.info(imageViewRequest.getIrUrlInfo().getDecodedUrl());
    
	return dataService.getImageViewList(imageViewRequest);
    }

    public void setDataService(DataService dataService) {
	this.dataService = dataService;
    }

}
