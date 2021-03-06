/**
 * This file is part of huborcid.
 *
 * huborcid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * huborcid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with huborcid.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cineca.pst.huborcid.web.rest;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;

import it.cineca.pst.huborcid.domain.Application;
import it.cineca.pst.huborcid.domain.ResultUploadOrcidEntity;
import it.cineca.pst.huborcid.repository.ApplicationRepository;
import it.cineca.pst.huborcid.repository.ResultUploadOrcidEntityRepository;
import it.cineca.pst.huborcid.security.SecurityUtils;
import it.cineca.pst.huborcid.service.OrcidFundingFileService;
import it.cineca.pst.huborcid.service.OrcidWorksFileService;
import it.cineca.pst.huborcid.web.rest.dto.UploadResponseDTO;
import it.cineca.pst.huborcid.web.rest.util.ResultCode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api")
public class UploadOrcidEntityFileResource {
	
    @Inject
    private OrcidWorksFileService orcidWorksFileService;
    
    @Inject
    private OrcidFundingFileService orcidFundingFileService;
    
    @Inject
	private ApplicationRepository applicationRepository;
    
    @Inject
    private ResultUploadOrcidEntityRepository resultUploadOrcidEntityRepository;

	@RequestMapping(value="/uploadOrcidEntity/fileUpload/{typeEntity}", method=RequestMethod.POST)
    public @ResponseBody UploadResponseDTO handleFileUpload(@RequestParam("file") MultipartFile multipartFile, @PathVariable String typeEntity){
        if (!multipartFile.isEmpty()) {
            try {
            	String currentLogin = SecurityUtils.getCurrentLogin();
        		Application application = applicationRepository.findOneByApplicationID(currentLogin);

        	    if("WORKS".equals(typeEntity)){    
        	    	orcidWorksFileService.uploadFileOrcid(multipartFile, application, typeEntity);
        	    } else if("FUNDING".equals(typeEntity)){    
        	    	orcidFundingFileService.uploadFileOrcid(multipartFile, application, typeEntity);
        	    }

                return new UploadResponseDTO(ResultCode.SUCCESS, "huborcidApp.uploadOrcidEntity.uploadFileStatus.UPLOAD_SUCCESS");
            } catch (Exception e) {
                return new UploadResponseDTO(ResultCode.ERROR_FILE_UPLOAD, "huborcidApp.uploadOrcidEntity.uploadFileStatus.UPLOAD_FAILURE");
            }
        } else {
        	 return new UploadResponseDTO(ResultCode.ERROR_FILE_UPLOAD, "huborcidApp.uploadOrcidEntity.uploadFileStatus.UPLOAD_FAILURE_EMPTY");
        }
    }
	

    @RequestMapping(value = "/uploadOrcidEntity/downloadExcelResult/{id}",
            method = RequestMethod.GET)
    @Timed
    public void getExcelResultUploadOrcidEntity(@PathVariable Long id, HttpServletResponse response) {
    	try {
    		ResultUploadOrcidEntity resultUploadOrcid = resultUploadOrcidEntityRepository.findOne(id);
		    byte[] fileBytesResult = resultUploadOrcid.getFileResult();
		    response.setContentType("application/data");
		    OutputStream outs = response.getOutputStream();
		    outs.write(fileBytesResult);
		    outs.flush();
		    outs.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    
    
   
}
