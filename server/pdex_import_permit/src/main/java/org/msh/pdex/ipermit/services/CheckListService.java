package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.msh.pdex.dto.CheckListRow;
import org.msh.pdex.dto.CheckListsDTO;
import org.msh.pdex.dto.NavigatorDTO;
import org.msh.pdex.dto.QuestionDTO;
import org.msh.pdex.dto.QuestionTemplateDTO;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.model.User;
import org.msh.pdex.model.enums.TemplateType;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.PipStatus;
import org.msh.pdex.model.pip.QuestionInstancePIP;
import org.msh.pdex.model.pip.QuestionPIP;
import org.msh.pdex.model.pip.QuestionTemplatePIP;
import org.msh.pdex.model.rsecond.AnswerQuestion;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.repository.JdbcRepository;
import org.msh.pdex.repository.pip.Import_permitRepo;
import org.msh.pdex.repository.pip.PipStatusRepo;
import org.msh.pdex.repository.pip.QuestionInstancePIPRepo;
import org.msh.pdex.repository.pip.QuestionTemplatePIPRepo;
import org.msh.pdex.services.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Serve all types of check lists for all users in all states  
 * @author alexk
 *
 */
@Service
public class CheckListService implements CheckListRow {


	private static final Logger logger = LoggerFactory.getLogger(CheckListService.class);

	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	Messages messages;
	@Autowired
	QuestionTemplatePIPRepo questionTemplatePIPRepo;
	@Autowired
	QuestionInstancePIPRepo questionInstancePIPRepo;
	@Autowired
	Import_permitRepo applicationRepo;
	@Autowired
	EntityToDtoService entityToDTOServ;
	@Autowired
	ValidationService validator;
	@Autowired
	PipStatusRepo statusRepo;
	@Autowired
	JdbcRepository jdbcRepo;
	@Autowired
	DictionaryService dictionaryService;
	
	/**
	 * Create or load a checklist for the current PIP
	 * @param model
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public List<QuestionInstancePIP> questionsForPIP(Import_permit model) throws ObjectNotFoundException {
		List<QuestionInstancePIP> ret = new ArrayList<QuestionInstancePIP>();
		if(shouldLoadCheckList(model)) {
			ret = loadQuestionInstances(model,null);
		}else {
			ret = addQuestionInstances(model);
			model.getCheckLists().addAll(ret);
			applicationRepo.save(model);
			ret = loadQuestionInstances(model,null);
		}
		return ret;
	}

	/**
	 * Should load or create instatnces of questions
	 * @param model
	 * @return true load, false create
	 */
	private boolean shouldLoadCheckList(Import_permit model){
		boolean ret = model.getCheckLists() != null && model.getCheckLists().size()>0;
		if(ret){
			ret = loadQuestionInstances(model,null).size()>0;
		}
		return ret;
	}
	/**
	 * Return checklist only for the current PIP and sorted by order
	 * @param model - PIP
	 * @param pipStatus 
	 * @return sorted by order list only for the current instance
	 */
	public List<QuestionInstancePIP> loadQuestionInstances(Import_permit model, PipStatus pipStatus) {
		List<QuestionInstancePIP> ret = new ArrayList<QuestionInstancePIP>();
		long stateId = model.getPipStatus().getId();
		if(pipStatus != null) {
			stateId=pipStatus.getId();
		}
		for(QuestionInstancePIP q : model.getCheckLists()) {
			if(q.getStatus().getId()==stateId) {
				ret.add(q);
			}
		}
		sortByOrder(ret);
		return ret;
	}

	/**
	 * 
	 */
	@Transactional
	private List<QuestionTemplateDTO> loadQuestionTemplates(long pipStatusId) {
		List<QuestionTemplateDTO> ret = new ArrayList<QuestionTemplateDTO>();

		Iterable<QuestionTemplatePIP> questions = questionTemplatePIPRepo.findAll();
		if(questions != null) {
			for(QuestionTemplatePIP question : questions) {
				if(question.getStatus().getId() == pipStatusId) {
					ret.add(entityToDTOServ.questionTemplateToDto(question));
				}
			}
		}
		if(ret.size() == 0) {
			// create List with 1 empty item
			PipStatus status = statusRepo.findById(pipStatusId).get();
			QuestionTemplatePIP question = createEmpty(status, 1);

			ret.add(entityToDTOServ.questionTemplateToDto(question));
		}

		sortTemplByOrder(ret);
		return ret;
	}

	/**
	 * Sort Question by order asc
	 * @param ret
	 */
	private void sortByOrder(List<QuestionInstancePIP> ret) {
		Collections.sort(ret, new Comparator<QuestionInstancePIP>() {
			@Override
			public int compare(QuestionInstancePIP me, QuestionInstancePIP it) {
				if(me.getOrder()>it.getOrder()) {
					return 1;
				}
				if(me.getOrder()<it.getOrder()) {
					return -1;
				}
				return 0;
			}
		});
	}

	/**
	 * Sort QuestionTemplate by order asc
	 * @param ret
	 */
	private void sortTemplByOrder(List<QuestionTemplateDTO> ret) {
		Collections.sort(ret, new Comparator<QuestionTemplateDTO>() {
			@Override
			public int compare(QuestionTemplateDTO me, QuestionTemplateDTO it) {
				if(me.getOrder().getValue() > it.getOrder().getValue()) {
					return 1;
				}
				if(me.getOrder().getValue() < it.getOrder().getValue()) {
					return -1;
				}
				return 0;
			}
		});
	}

	/**
	 * Create question instance
	 * @param model 
	 * @return
	 */
	private List<QuestionInstancePIP> addQuestionInstances(Import_permit model) {
		List<QuestionInstancePIP> ret = new ArrayList<QuestionInstancePIP>();
		Iterable<QuestionTemplatePIP> questions = questionTemplatePIPRepo.findAll();
		if(questions != null) {
			for(QuestionPIP question : questions) {
				if(question.getStatus().getId()==model.getPipStatus().getId()) {
					ret.add(instanceFromQuestion(question));
				}
			}
		}
		sortByOrder(ret);
		return ret;
	}

	/**
	 * Create an instance of PIP question from a question
	 * @param question
	 * @return
	 */
	private QuestionInstancePIP instanceFromQuestion(QuestionPIP question) {
		QuestionInstancePIP ret = new QuestionInstancePIP();
		ret.setAnswer(new AnswerQuestion());
		ret.setHeader(question.getHeader());
		ret.setOrder(question.getOrder());
		ret.setQuestion(question.getQuestion());
		ret.setStatus(question.getStatus());
		return ret;
	}

	/**
	 * create DTO for the current checklist
	 * @param model
	 * @param appDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public ApplicationDTO questionsForPIP(ApplicationDTO appDTO, User currentUser) throws ObjectNotFoundException {
		Import_permit model = boilerServ.loadApplication(appDTO.getId());
		List<QuestionInstancePIP> questions = null;
		if(appDTO.getPrevStatusId() > 0) {
			PipStatus st = dictionaryService.loadPipStatus(appDTO.getPrevStatusId());
			if(st != null) {
				questions = loadQuestionInstances(model, st);
			}
		}else {
			questions = questionsForPIP(model);
		}
		
		TemplateType templType = null;
		if(questions != null && questions.size() > 0) {
			QuestionInstancePIP q = questions.get(0);
			String code = q.getStatus().getCode().toUpperCase();
			for(TemplateType tt:TemplateType.values()) {
				if(tt.name().endsWith("_" + code)) {
					templType = tt;
					break;
				}
			}
		}
		if(templType != null)
			appDTO.setTypeTmplCheckList(templType.name());
		else
			appDTO.setTypeTmplCheckList("");
		List<QuestionDTO> cards = entityToDTOServ.questionInstancesToDTO(questions, model, currentUser);
		appDTO.setQuestions(cards);
		return appDTO;
	}



	/**
	 * Save an answer to the question
	 * @param userLogin
	 * @param context
	 * @param question
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public QuestionDTO answerPIPSave(String userLogin, Context context, QuestionDTO questionDTO) throws ObjectNotFoundException {
		if(questionDTO.getAnswer().isAsk()) {
			questionDTO.setAnswer(validator.validateDTO(questionDTO.getAnswer(), true, true));
		}else {
			questionDTO.getAnswer().setValid(true);
		}
		if(questionDTO.getAnswer().isValid()) {
			Optional<QuestionInstancePIP> questiono = questionInstancePIPRepo.findById(questionDTO.getId());
			User author = boilerServ.userByUserLogin(userLogin);
			if(questiono.isPresent()) {
				QuestionInstancePIP question = questiono.get();
				question.setAnswer(entityToDTOServ.answerDtoToAnswerQuestion(questionDTO.getAnswer(), question.getAnswer(), author));
				question=questionInstancePIPRepo.save(question);
				questionDTO = entityToDTOServ.questionInstanceToDto(question,questionDTO);
			}else {
				throw new ObjectNotFoundException("Question PIP not found. Id is " + questionDTO.getId(),logger);
			}
		}
		return questionDTO;
	}

	/**
	 * Expert answered this question
	 * @param context
	 * @param fetchGroup
	 * @param fetchUserLogin
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public QuestionDTO questionAnswered(Context context, String fetchGroup, String fetchUserLogin, QuestionDTO data) throws ObjectNotFoundException {
		QuestionInstancePIP model = boilerServ.question(data.getId());
		model = answerIt(model);
		data = entityToDTOServ.questionInstanceToDto(model, data);
		return data;
	}

	public QuestionInstancePIP answerIt(QuestionInstancePIP model) {
		model.getAnswer().setAnswered(true);
		model.getAnswer().setAsk(false);
		model.getAnswer().setNo(false);
		model.getAnswer().setNotApplicable(false);
		model.getAnswer().setYes(false);
		model.getAnswer().setAnsweredAt(new Date());
		model = questionInstancePIPRepo.save(model);
		return model;
	}
	/**
	 * load all the questions from all checklists in the model
	 * Supposed that all checklist are created
	 * @param model
	 * @param user
	 * @param appDto
	 * @return
	 */
	public ApplicationDTO loadQuestionsAndAnswers(Import_permit model, User user, ApplicationDTO appDto) {
		appDto.setQuestions(entityToDTOServ.questionInstancesToDTO(model.getCheckLists(), model,user));
		return appDto;
	}
	/**
	 * Remove all unanswered questions
	 * @param model
	 * @return
	 */
	@Transactional
	public Import_permit removeQuestions(Import_permit model) {
		for(QuestionInstancePIP q : model.getCheckLists()){
			if(q.getAnswer().getAsk()){
				q=answerIt(q);
			}
		}
		return model;
	}
	/**
	 * Get results of verification in format of checklist
	 * @param appDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public ApplicationDTO verificationResult(ApplicationDTO appDTO, User currentUser) throws ObjectNotFoundException {
		Import_permit model = boilerServ.loadApplication(appDTO.getId());
		Optional<PipStatus> statuso = statusRepo.findByCode("VRFY");
		if(statuso.isPresent()) {
			List<QuestionDTO> cards = entityToDTOServ.questionInstancesToDTO(loadQuestionInstances(model,statuso.get()),model, currentUser);
			appDTO.setQuestions(cards);
			return appDTO;
		}else {
			throw new ObjectNotFoundException("Permit status VRFY not found or inactive", logger);
		}
	}

	@Transactional
	public NavigatorDTO checklistOpen(String userLogin, Context context, NavigatorDTO navigator) throws ObjectNotFoundException {
		navigator.setTab("administration");
		navigator.setComponent("checkList");
		navigator.setParams(navigator.getId() + "");

		return navigator;
	}

	/**
	 * user by id
	 * @param id
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public PipStatus pipStatusById(long id) throws ObjectNotFoundException {
		Optional<PipStatus> st = statusRepo.findById(id);
		if(st.isPresent()) {
			return st.get();
		}else {
			throw new ObjectNotFoundException("pipStatusById(). PipStatus not found. Id is "+id, logger);
		}
	}

	@Transactional
	public CheckListsDTO checkListOpenEdit(User currentUser, Context context, CheckListsDTO checkDTO) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		Optional<PipStatus> statuso = statusRepo.findById(checkDTO.getId());
		if(statuso.isPresent()) {
			checkDTO.setState(statuso.get().getCode());
			checkDTO.setStateDescr(messages.get(statuso.get().getDescription()));
			checkDTO.setQuestions(loadQuestionTemplates(checkDTO.getId()));
			return checkDTO;
		}else {
			throw new ObjectNotFoundException("checkListOpenByEdit PipSatus not found ID is "+checkDTO.getId(),logger);
		}
	}

	public CheckListsDTO checklists(String userLogin, CheckListsDTO checkDTO, Context context) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		String select = boilerServ.loadSelect("checklists");
		CreateHeaders createHeaders = this::checklistsHeaders;
		Headers headers = boilerServ.loadHeaders(context, "", checkDTO.getTable(), createHeaders);
		checkDTO.getTable().setHeaders(headers);
		List<TableRow> rows= jdbcRepo.qtbGroupReport(select, 
				"",
				"", headers);
		TableQtb.tablePage(rows, checkDTO.getTable());
		checkDTO.setTable(boilerServ.translateRows(checkDTO.getTable()));
		checkDTO.getTable().setSelectable(false);
		return checkDTO;
	}
	@Transactional
	public CheckListsDTO questionTemplateSave(String userLogin, Context context, CheckListsDTO checkListDTO) throws ObjectNotFoundException {
		if(checkListDTO.getQuestions() != null) {
			for(int i = 0; i < checkListDTO.getQuestions().size(); i++) {
				QuestionTemplateDTO dto = checkListDTO.getQuestions().get(i);

				QuestionTemplatePIP current = questionTemplatePIPRepo.findById(dto.getId()).get();
				long pipStatusId = current.getStatus().getId();
				if(dto.isUp() || dto.isDown()) {
					int newOrder = current.getOrder();

					if(dto.isUp()) newOrder = newOrder - 1;
					if(dto.isDown()) newOrder = newOrder + 1;

					QuestionTemplatePIP item = questionTemplatePIPRepo.findByOrderAndStatusId(newOrder, pipStatusId);
					if(item != null) {
						item.setOrder(current.getOrder());
						current.setOrder(newOrder);

						item = questionTemplatePIPRepo.save(item);
						current = questionTemplatePIPRepo.save(current);
					}
				}else if(dto.isDelete()) {
					questionTemplatePIPRepo.delete(current);
				}else if(dto.isEdit()) {
					current.setHeader(dto.getHead().getValue());

					if(current.getQuestion().equals(QuestionTemplateDTO.QUESTION_KEY)) {
						// create/update ResourceMessage
						String key = QuestionTemplateDTO.QUESTION_KEY + current.getId();
						boilerServ.updateResourceMessage(key, dto.getQuest().getValue(), dto.getQuestportu().getValue());
						current.setQuestion(key);
						current = questionTemplatePIPRepo.save(current);
					}else {
						current = questionTemplatePIPRepo.save(current);
						// update text value question
						boilerServ.updateResourceMessage(dto.getQuestionKey(), dto.getQuest().getValue(), dto.getQuestportu().getValue());
					}
					// reload all Messages from DB
					messages.getMessages().clear();
					messages.reload(messages.getWorkspace().getDefaultLocale());
				}else if(dto.isAdd()){
					// inc all orders after current
					Iterable<QuestionTemplatePIP> questions = questionTemplatePIPRepo.findAll();
					if(questions != null) {
						for(QuestionTemplatePIP question : questions) {
							if(question.getStatus().getId() == pipStatusId) {
								if(question.getOrder() > current.getOrder()) {
									question.setOrder(question.getOrder() + 1);
									questionTemplatePIPRepo.save(question);
								}
							}
						}
					}

					// create empty QuestionTemplate after current
					createEmpty(current.getStatus(), current.getOrder() + 1);
				}
			}
			// update list questions template
			checkListDTO.setQuestions(loadQuestionTemplates(checkListDTO.getId()));
		}
		return checkListDTO;
	}

	/**
	 * Create empty QuestionTemplatePIP
	 * questionkey=Question_
	 * After edit QuestionTemplatePIP questionkey=Question_ID, where ID= id QuestionTemplatePIP
	 * @param status
	 * @param order
	 * @return
	 */
	private QuestionTemplatePIP createEmpty(PipStatus status, int order) {
		QuestionTemplatePIP empty = new QuestionTemplatePIP();
		empty.setStatus(status);
		empty.setOrder(order);
		// create questionkey=Question_
		empty.setQuestion(QuestionTemplateDTO.QUESTION_KEY);
		empty = questionTemplatePIPRepo.save(empty);

		return empty;
	}
	/**
	 * Create the headers
	 * @return
	 */
	private Headers checklistsHeaders() {
		Headers ret = CheckListHeadersCommon();
		return ret;
	}

	public static Headers CheckListHeadersCommon() {
		Headers ret = new Headers();
		ret.getHeaders().add(TableHeader.instanceOf(
				"Code",
				"state",
				true,
				true,
				true,
				TableHeader.COLUMN_LINK,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"Description",
				"global_description",
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				0));
		return ret;
	}
}
