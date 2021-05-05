package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.msh.pdex.dto.NavigatorDTO;
import org.msh.pdex.dto.NotificationsDTO;
import org.msh.pdex.dto.QuestionDTO;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.model.User;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.repository.JdbcRepository;
import org.msh.pdex.repository.QueryRepository;
import org.msh.pdex.repository.UserRepository;
import org.msh.pdex.services.ContextServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Implementation of Review business logic
 * @author alexk
 *
 */
@Service
public class NotificationService {
	private static final String NOTES = "notes";
	private static final String CATEGORY = "category";
	private static final String NOTIFICATION_HEADERS = "NOTIFICATION_HEADERS";
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	    
	@Autowired
	ContextServices contextServices;
	@Autowired
	Messages messages;
	@Autowired
	JdbcRepository jdbcRepo;
	@Autowired
	QueryRepository queryRepo;
	@Autowired
	UserRepository userRepo;
	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	CheckListService checkListServ;
	@Autowired
	EntityToDtoService entityToDTOServ;
	@Autowired
	CommonService commonService;

	/**
	 * Get notification table data
	 * @param data
	 * @param fetchGroup
	 * @param fetchUserLogin
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public NotificationsDTO notifications(Context context, String userGroup, String userLogin,
			NotificationsDTO data) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		
		User user = boilerServ.userByUserLogin(userLogin);
		String select = createSelect(userGroup);
		CreateHeaders createHeaders = this::notificationsHeadersCreate;
		Headers headers = boilerServ.loadHeaders(context, NOTIFICATION_HEADERS, data.getTable(), createHeaders);
		data.getTable().setHeaders(headers);
		List<TableRow> rows= jdbcRepo.qtbGroupReport(select, "", 
				"notif.me in (0,"+user.getUserId()+")",
				headers);
		TableQtb.tablePage(rows, data.getTable());
		data.getTable().setSelectable(false);
		boilerServ.translateHeaders(data.getTable().getHeaders());
		TableHeader notes = data.getTable().getHeaders().getHeaderByKey(NOTES);
		notes.setColumnType(TableHeader.COLUMN_I18);
		boilerServ.translateRows(data.getTable());
		notes.setColumnType(TableHeader.COLUMN_LINK);
		contextServices.saveHeaders(context, headers,  NOTIFICATION_HEADERS);
		return data;
	}
	/**
	 * Create select as union of allowed notifications
	 * @param user
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	private String createSelect(String userRole) throws ObjectNotFoundException {
		String ret = createCommonSelect();

		String replStr = "opt.Code in ('RQST','VRFY','VALD')";
		String newStr = "";
		if(userRole.equalsIgnoreCase("secretary")) {
			newStr = "opt.Code in ('RQST', 'RJCT', 'ACCT','CNCL')";
		}
		if(userRole.equalsIgnoreCase("review")) {
			newStr = "opt.Code in ('VRFY')";
		}
		if(userRole.equalsIgnoreCase("moderator")) {
			newStr = "opt.Code not in ('ARCH','DRFT')";
		}
		if(newStr.length() > 0){
			ret =  ret + " union " + boilerServ.loadSelect("notif_apps_delayed").replace(replStr, newStr);
		}
		ret = "select * from (" + ret + ") notif";
		
		/* 25092020
		String ret = createCommonSelect();
		if(userRole.equalsIgnoreCase( "applicant")) {
			ret = ret + " union " + boilerServ.loadSelect("notif_justregistered");
			ret=ret+" union " + boilerServ.loadSelect("notif_invoiced");
			ret = ret + " union " + boilerServ.loadSelect("notif_justconsidered");
			ret = ret + " union " + boilerServ.loadSelect("notif_apps_more_five_days");
		}
		if(userRole.equalsIgnoreCase("secretary")) {
			ret =  ret + " union " + boilerServ.loadSelect("notif_apps_delayed").replace("opt.Code in ('RQST','VRFY','VALD')", "opt.Code in ('RQST', 'RJCT', 'ACCT','CNCL')");
		}
		if(userRole.equalsIgnoreCase("review")) {
			ret =  ret + " union " + boilerServ.loadSelect("notif_apps_delayed").replace("opt.Code in ('RQST','VRFY','VALD')", "opt.Code in ('VRFY')");
		}
		if(userRole.equalsIgnoreCase("moderator")) {
			ret = ret + " union " + boilerServ.loadSelect("notif_apps_delayed").replace("opt.Code in ('RQST','VRFY','VALD')", "opt.Code not in ('ARCH','DRFT')");
			ret = ret + " union " + boilerServ.loadSelect("notif_answer_delayed");
		}
		ret= "select * from ("+ret+")notif";
		*/
		return ret;
	}
	/**
	 * Notification for all - questions and just registered
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	private String createCommonSelect() throws ObjectNotFoundException {
		String ret = boilerServ.loadSelect("notif_myquestions");
		ret = ret + " union " + boilerServ.loadSelect("notif_questionstome");
		ret = ret +" union " + boilerServ.loadSelect("notif_answered");
		return ret;
	}
	/**
	 * Create new notifications headers
	 * @return
	 */
	private Headers notificationsHeadersCreate() {
		Headers ret = new Headers();

		ret.getHeaders().add(TableHeader.instanceOf(
				"issued",
				"issued",
				true,
				true,
				true,
				TableHeader.COLUMN_LOCALDATE,
				15));
		ret.getHeaders().add(TableHeader.instanceOf(
				"originatedby",
				"origin",
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				30));
		ret.getHeaders().add(TableHeader.instanceOf(
				"relatedto",
				"relation",
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				30));
		ret.getHeaders().add(TableHeader.instanceOf(
				CATEGORY,
				CATEGORY,
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				40));
		ret.getHeaders().add(TableHeader.instanceOf(
				NOTES,
				NOTES,
				true,
				true,
				true,
				TableHeader.COLUMN_LINK,
				30));
		ret.getHeaders().get(0).setSort(true);
		ret.getHeaders().get(0).setSortValue(TableHeader.SORT_DESC);
		return ret;
	}
	/**
	 * Where to go by the link
	 * @param context
	 * @param fetchGroup
	 * @param fetchUserLogin
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public NavigatorDTO notificationsLink(Context context, String fetchGroup, String userLogin,
			NavigatorDTO data) throws ObjectNotFoundException {
		String source = (String) data.getRow().getCellByKey(CATEGORY).getOriginalValue();
		data.setParams(data.getRow().getDbID()+"");
		data.setId(data.getRow().getDbID());
		//25092020
		//if(source.toUpperCase().contains("justregistered".toUpperCase())) {		//product
		//	data.setTab("notifications");
		//	data.setComponent("product");
		//}else {																								//application
			data=commonService.applicationOpen(userLogin, context, data);
		//}
		return data;
	}
	/**
	 * Prepare list of related to the checklist questions to which answers should be be needed
	 * Moderator can answer all
	 * @param context
	 * @param fetchGroup
	 * @param userLogin
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO questionsAnswers(Context context, String userRole, String userLogin,
			ApplicationDTO appDto) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(appDto.getId());
		appDto=entityToDTOServ.ApplicationToDto(context, model, userLogin,userRole,appDto);
		User user = boilerServ.userByUserLogin(userLogin);
		appDto=checkListServ.loadQuestionsAndAnswers(model, user, appDto);
		List<QuestionDTO> questions = new ArrayList<QuestionDTO>();
		for(QuestionDTO q : appDto.getQuestions()) {
			if(q.getAnswer().getExpert()!=null && q.getAnswer().isAsk()) {
				if(q.getAnswer().getExpert().getValue().getId()==user.getUserId() || userRole.equalsIgnoreCase( "moderator")) {
					questions.add(q);
				}
			}
		}
		appDto.getQuestions().clear();
		appDto.getQuestions().addAll(questions);
		return appDto;
		
	}

}