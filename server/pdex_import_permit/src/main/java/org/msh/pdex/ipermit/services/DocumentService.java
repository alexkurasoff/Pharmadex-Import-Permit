package org.msh.pdex.ipermit.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableCell;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.model.FileTemplate;
import org.msh.pdex.model.ProdApplications;
import org.msh.pdex.model.Product;
import org.msh.pdex.model.User;
import org.msh.pdex.model.enums.TemplateType;
import org.msh.pdex.model.enums.YesNoNA;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.Import_permit_detail;
import org.msh.pdex.model.pip.PipStatus;
import org.msh.pdex.model.pip.QuestionInstancePIP;
import org.msh.pdex.model.rsecond.AnswerQuestion;
import org.msh.pdex.repository.FileTemplateRepo;
import org.msh.pdex.services.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generate documents from MS Word templates
 * @author alexk
 *
 */
@Service
public class DocumentService {
	private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
	@Autowired
	FileTemplateRepo fileTemplRepo;
	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	EntityToDtoService entityToDTOServ;
	@Autowired
	Messages messages;
	@Autowired
	CheckListService checkListService;
	@Autowired
	DictionaryService dictionaryService;

	/**
	 * Find a template by a type given
	 * @param name
	 * @return byte input stream with the template found
	 */
	public Optional<InputStream> findTemplate(TemplateType templType) {
		Optional<InputStream> ret = Optional.empty();
		for(FileTemplate templ : fileTemplRepo.findAll()) {
			if(templ.getTemplateType().equals(templType)) {
				byte[] file = templ.getFile();
				if(file != null && file.length>1024) {
					ret = Optional.of( new ByteArrayInputStream(file));
				}
			}
		}
		return ret;
	}
	/**
	 * Load bief template from the database
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public InputStream biefTemplate() throws ObjectNotFoundException {
		Optional<InputStream> streamo = findTemplate(TemplateType.BIEF);
		if(streamo.isPresent()) {
			return streamo.get();
		}else {
			throw new ObjectNotFoundException("BIEF template not found or wrong", logger);
		}
	}
	
	public InputStream checklistTemplate(TemplateType type) throws ObjectNotFoundException {
		Optional<InputStream> streamo = findTemplate(type);
		if(streamo.isPresent()) {
			return streamo.get();
		}else {
			throw new ObjectNotFoundException("checklist template not found or wrong", logger);
		}
	}
	
	/**
	 * Parameters for BIEF - table and applicant data
	 * @param appId application ID
	 * @param userLogin 
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public Map<String, Object> biefModel(long appId, String userLogin) throws ObjectNotFoundException {
		Map<String, Object> ret = new HashMap<String, Object>();
		Import_permit appl = boilerServ.loadApplication(appId);
		User user = boilerServ.userByUserLogin(userLogin);
		ret.put("name", appl.getApplicant().getAppName());
		ret.put("address",entityToDTOServ.addrToString(appl.getApplicant().getAddress()));
		ret.put("phones", entityToDTOServ.applicantElinkToString(appl.getApplicant()));
		ret.put("executor",entityToDTOServ.userData(user));
		ret.put("table", biefSpecifications(appl));
		return ret;
	}
	
	@Transactional
	public Map<String, Object> checkListModel(TemplateType type, ApplicationDTO appDTO, String userLogin) throws ObjectNotFoundException {
		Map<String, Object> ret = new HashMap<String, Object>();
		Import_permit appl = boilerServ.loadApplication(appDTO.getId());
		User user = boilerServ.userByUserLogin(userLogin);
		
		List<QuestionInstancePIP> questions = null;
		if(appDTO.getPrevStatusId() > 0) {
			PipStatus st = dictionaryService.loadPipStatus(appDTO.getPrevStatusId());
			if(st != null) {
				questions = checkListService.loadQuestionInstances(appl, st);
			}
		}else {
			questions = checkListService.questionsForPIP(appl);
		}
		
		if(type.name().contentEquals(TemplateType.CHECKLIST_VRFY.name())) {
			ret.put("name", appl.getApplicant().getAppName());
			ret.put("address",entityToDTOServ.addrToString(appl.getApplicant().getAddress()));
			ret.put("phones", entityToDTOServ.applicantElinkToString(appl.getApplicant()));
		}
		
		ret.put("table", checkListSpecifications(type, questions));
		
		return ret;
	}
	/**
	 * Create product specifications for BIEF
	 * @param appl
	 * @return
	 */
	private TableQtb biefSpecifications(Import_permit appl) {
		TableQtb table = new TableQtb();
		Headers headers = table.getHeaders();
		headers.getHeaders().add(TableHeader.instanceOf(
				"numberinorder",
				"numberinorder",
				false,
				false,
				false,
				TableHeader.COLUMN_LONG,
				5));
		headers.getHeaders().add(TableHeader.instanceOf(
				"producttoimport",
				"producttoimport",
				false,
				false,
				false,
				TableHeader.COLUMN_STRING,
				0));
		headers.getHeaders().add(TableHeader.instanceOf(
				"productquantity",
				"productquantity",
				false,
				false,
				false,
				TableHeader.COLUMN_LONG,
				0));
		headers.getHeaders().add(TableHeader.instanceOf(
				"registernumber",
				"registernumber",
				false,
				false,
				false,
				TableHeader.COLUMN_LONG,
				20));
		boilerServ.translateHeaders(table.getHeaders());
		long i=1;
		for(Import_permit_detail detail :appl.getImport_permit_detail()) {
			TableRow row=TableRow.instanceOf(i);
			row.getRow().add(TableCell.instanceOf(
					"numberinorder",
					i,
					LocaleContextHolder.getLocale()));
			row.getRow().add(TableCell.instanceOf(
					"producttoimport",
					entityToDTOServ.productToString(detail.getProduct())
					));
			row.getRow().add(TableCell.instanceOf(
					"productquantity",
					detail.getUnits().longValue(),
					LocaleContextHolder.getLocale()));
			row.getRow().add(TableCell.instanceOf(
					"registernumber",
					findProdRegNumber(detail.getProduct())
					));
			i++;
			table.getRows().add(row);
		}
		
		return table;
	}
	// notes
	private TableQtb checkListSpecifications(TemplateType type, List<QuestionInstancePIP> list) {
		TableQtb table = new TableQtb();
		Headers headers = table.getHeaders();
		headers.getHeaders().addAll(buildTableHeader(type));
		boilerServ.translateHeaders(table.getHeaders());
		
		for(int i = 0; i < list.size(); i++) {
			TableRow row = TableRow.instanceOf(i);
			
			QuestionInstancePIP quesPIP = list.get(i);
			
			row.getRow().add(TableCell.instanceOf(
					"name", messages.get(quesPIP.getQuestion())
					));
			
			String value_yes = "";
			String value_no = "";
			String note = "";
			
			AnswerQuestion answer = quesPIP.getAnswer();
			if(answer != null) {
				if(answer.getYes()) 
					value_yes = "X";

				if(answer.getNo()) 
					value_no = "X";
				
				if(answer.getNote() != null)
					note = answer.getNote();
			}
			row.getRow().add(TableCell.instanceOf(
					"answer_y", value_yes));
			row.getRow().add(TableCell.instanceOf(
					"answer_n", value_no));
			if(type.equals(TemplateType.CHECKLIST_VRFY)) {
				row.getRow().add(TableCell.instanceOf(
						"note", note));
			}
			
			table.getRows().add(row);
		}
		
		return table;
	}
	
	private List<TableHeader> buildTableHeader(TemplateType type){
		List<TableHeader> list = new ArrayList<TableHeader>();
		list.add(TableHeader.instanceOf(
				"name",
				"quest",
				false,
				false,
				false,
				TableHeader.COLUMN_STRING,
				0));
		
		String mess_yes = YesNoNA.YES.getKey();
		String mess_no = YesNoNA.NO.getKey();
		String mess_note = "notes";
		boolean isAddNote = false;
		
		if(type.equals(TemplateType.CHECKLIST_VRFY)) {
			mess_yes = "lbl_conforme";
			mess_no = "lbl_notconforme";
			isAddNote = true;
		}
		
		list.add(TableHeader.instanceOf(
				"answer_y",
				mess_yes,
				false,
				false,
				false,
				TableHeader.COLUMN_STRING,
				0));
		list.add(TableHeader.instanceOf(
				"answer_n",
				mess_no,
				false,
				false,
				false,
				TableHeader.COLUMN_STRING,
				0));
		
		if(isAddNote) {
			list.add(TableHeader.instanceOf(
					"note",
					mess_note,
					false,
					false,
					false,
					TableHeader.COLUMN_STRING,
					0));
		}
		
		return list;
	}
	/**
	 * Find product registration number
	 * @param product
	 * @return
	 */
	private String findProdRegNumber(Product product) {
		ProdApplications latest = findTheLatestApplication(product);
		if(latest != null) {
			if(latest.getRegExpiryDate()!= null) {
				LocalDate expired = new LocalDate(latest.getRegExpiryDate());
				if(expired.isAfter(LocalDate.now())) {
					return latest.getProdRegNo();
				}else {
					return messages.get("notauthorized");
				}
			}else {
				if(latest.getProdRegNo()!= null && latest.getProdRegNo().length()>0) {
					return latest.getProdRegNo();
				}else {
					return messages.get("notauthorized");
				}
			}
		}else {
			return messages.get("notauthorized");
		}
	}
	/**
	 * find the latest product application
	 * @param product
	 * @return
	 */
	private ProdApplications findTheLatestApplication(Product product) {
		LocalDate regDate = LocalDate.now().minusYears(100);
		ProdApplications ret = null;
		if(product.getProdApplicationses() != null) {
			for(ProdApplications pa : product.getProdApplicationses()) {
				if(pa.getProdRegNo() != null && pa.getProdRegNo().length()>0) {
					LocalDate paReg = new LocalDate(pa.getRegistrationDate());
					if(paReg.isAfter(regDate)) {
						ret=pa;
						regDate=paReg;
					}
				}
				
			}
		}
		return ret ;
	}
	

}
