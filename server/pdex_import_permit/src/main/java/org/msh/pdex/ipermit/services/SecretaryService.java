package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.dto.ApplicationsDTO;
import org.msh.pdex.model.User;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.PIPTrackDone;
import org.msh.pdex.model.pip.PipStatus;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.repository.JdbcRepository;
import org.msh.pdex.services.ContextServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Secretary related business logic implementation
 * @author alexk
 *
 */
@Service
public class SecretaryService {
	private static final String APPLICATIONS_HEADERS = "APPLICATIONS_HEADERS";
	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	Messages messages;
	@Autowired
	JdbcRepository jdbcRepo;
	@Autowired
	ValidationService validator;
	@Autowired
	CommonService commonServ;
	@Autowired
	CheckListService checkListService;
	@Autowired
	EntityToDtoService entityToDTOServ;
	@Autowired
	ContextServices contextServices;
	/**
	 * Load list of applications for the current user's company and, possible, for companies for which the user's company is agent
	 * @param userLogin
	 * @param appsDTO
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public ApplicationsDTO applications(String userLogin, ApplicationsDTO appsDTO, Context context) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		User user = boilerServ.userByUserLogin(userLogin);
		appsDTO.setApplicantName("");
		String select=boilerServ.loadSelect("applications");
		String where = createApplicationsWhere(appsDTO);
		CreateHeaders createHeaders=this::applicationsHeaders;
		Headers headers = boilerServ.loadHeaders(context,APPLICATIONS_HEADERS, appsDTO.getTable(),createHeaders);
		appsDTO.getTable().setHeaders(headers);
		List<TableRow> rows= jdbcRepo.qtbGroupReport(select, 
				"",
				where, headers);
		TableQtb.tablePage(rows, appsDTO.getTable());
		appsDTO.setTable(boilerServ.translateRows(appsDTO.getTable()));
		appsDTO.getTable().setSelectable(false);
		contextServices.saveHeaders(context, headers,  APPLICATIONS_HEADERS);
		return appsDTO;
	}
	/**
	 * Phrase Where depends on a tab name
	 * @param appsDTO
	 * @return
	 */
	private String createApplicationsWhere(ApplicationsDTO appsDTO) {
		String ret = "statCode in ('RQST','ACCT', 'RJCT', 'CNCL')";
		if(appsDTO.getTabName().equalsIgnoreCase("ARCHIVE")) {
			ret = "statCode in ('ARCH')";
		}
		return ret;
	}

	/**
	 * Create the headers
	 * @return
	 */
	private Headers applicationsHeaders() {
		Headers ret = ApplicantService.ApplicationHeadersCommon();
		ret.getHeaders().add(TableHeader.instanceOf(
				"appName",
				"applicant_name",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				15));
		ret.getHeaders().add(TableHeader.instanceOf(
				"address",
				"address",
				false,
				true,
				true,
				TableHeader.COLUMN_STRING,
				15));
		return ret;
	}
	/**
	 * Pass this application to the Import Responsible (verification stage)
	 * @param userLogin
	 * @param context
	 * @param appDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO passToVerification(String userLogin, String userRole, Context context, ApplicationDTO appDTO) throws ObjectNotFoundException, IOException {
		appDTO.setValid(true);
		Import_permit model = boilerServ.loadApplication(appDTO.getId());
		appDTO = validator.validatePIPChecklist(model, appDTO);
		if(appDTO.isValid()) {
			appDTO = commonServ.doneJob(context, userLogin, userRole, "passtoverification","VRFY",appDTO);
			if(appDTO.isValid()) {
				appDTO.setAlertMessage(messages.get("passtoverification"));
			}
		}
		return appDTO;
	}

	/**
	 * Open an application depends on the state
	 * @param userLogin
	 * @param context
	 * @param appDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	@Transactional
	public ApplicationDTO applicationOpen(String userLogin, String userRole, Context context, ApplicationDTO appDTO) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(appDTO.getId());
		appDTO=entityToDTOServ.ApplicationToDto(context, model, userLogin,userRole,appDTO);
		if(model.getPipStatus().getCode().equalsIgnoreCase("VALD")) {
			long maxId=0;
			String statString = "";
			for(PIPTrackDone track: model.getJobsDone()) {
				if(track.getId()>maxId) {
					maxId=track.getId();
					statString = track.getJobCode();
				}
			}
			if(statString.length()>0) {
				appDTO.setAlertMessage(messages.get(statString));
			}
		}
		appDTO.setValid(true);
		return appDTO;
	}
	/**
	 * Archive this application
	 * @param userLogin
	 * @param context
	 * @param appDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO applicationArchive(String userLogin, String userRole, Context context, ApplicationDTO appDTO) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(appDTO.getId());
		appDTO = entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, appDTO);
		appDTO=validator.validatePIPChecklist(model, appDTO);
		if(appDTO.isValid()) {
			appDTO=commonServ.doneJob(context, userLogin, userRole,"descr_ARCH" , "ARCH", appDTO);
			model = checkListService.removeQuestions(model);
		}
		return appDTO;
	}
}
