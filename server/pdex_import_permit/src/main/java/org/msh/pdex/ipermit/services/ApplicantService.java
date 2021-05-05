package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.dto.ApplicationDetailDTO;
import org.msh.pdex.ipermit.dto.ApplicationsButtonsDTO;
import org.msh.pdex.ipermit.dto.ApplicationsDTO;
import org.msh.pdex.ipermit.dto.ProductDTO;
import org.msh.pdex.ipermit.dto.QuotasDTO;
import org.msh.pdex.ipermit.dto.QuoteDTO;
import org.msh.pdex.model.Applicant;
import org.msh.pdex.model.ProdCompany;
import org.msh.pdex.model.Product;
import org.msh.pdex.model.User;
import org.msh.pdex.model.enums.CompanyType;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.Import_permit_detail;
import org.msh.pdex.model.pip.ItemType;
import org.msh.pdex.model.pip.OrderType;
import org.msh.pdex.model.pip.PipStatus;
import org.msh.pdex.model.pip.QuestionInstancePIP;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.repository.JdbcRepository;
import org.msh.pdex.repository.ProductRepo;
import org.msh.pdex.repository.pip.Import_permitRepo;
import org.msh.pdex.repository.pip.Import_permit_detailRepo;
import org.msh.pdex.services.ContextServices;
import org.msh.pdex.services.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Implementation of Applicant - related business logic
 * @author alexk
 *
 */
@Service
public class ApplicantService {

	private static final String APPLICATIONS_HEADERS = "APPLICATIONS_HEADERS";
	private static final String QUOTAS_HEADERS = "QUOTAS_HEADERS";

	private static final Logger logger = LoggerFactory.getLogger(ApplicantService.class);
	@Autowired
	ContextServices contextServices;
	@Autowired
	Messages messages;
	@Autowired
	JdbcRepository jdbcRepo;
	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	EntityToDtoService entityToDTOServ;
	@Autowired
	DtoToEntityService dtoToEntityServ;
	@Autowired
	DictionaryService dictServ;
	@Autowired
	DocumentService docService;

	@Autowired
	CommonService commonService;
	@Autowired
	NotificationService notifServ;
	@Autowired
	ValidationService validator;
	@Autowired
	Import_permitRepo appRepo; 
	@Autowired
	Import_permitRepo pipRepo;
	@Autowired
	ProductRepo prodRepo;

	@Autowired
	CheckListService checkListService;

	@Autowired
	Import_permit_detailRepo iPermDetailRepo;


	/**
	 * Get quotes for an applicant.
	 * The applicant should recognized by the current user
	 * @param data
	 * @param fetchUserLogin
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public QuotasDTO quotas(QuotasDTO data, String userLogin, Context context) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		User user = boilerServ.userByUserLogin(userLogin);
		data.setTable(quotasTable(data.getTable(), user, context));
		contextServices.saveHeaders(context, data.getTable().getHeaders(), QUOTAS_HEADERS);
		data.setApplicantName(user.getApplicant().getAppName());
		return data;
	}
	/**
	 * Create main (not expanded) Quotas table
	 * @param table
	 * @param user
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private TableQtb quotasTable(TableQtb table, User user, Context context)
			throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		String select = boilerServ.loadSelect( "productsApplicant");
		CreateHeaders createHeaders=entityToDTOServ::quotasHeaders;
		Headers headers = boilerServ.loadHeaders(context,QUOTAS_HEADERS, table,createHeaders);
		table.setHeaders(headers);
		List<TableRow> rows= jdbcRepo.qtbGroupReport(select, "", "applcntId="+user.getApplicant().getApplcntId(), headers);
		TableQtb.tablePage(rows, table);
		table.setSelectable(false);
		return table;
	}


	/**
	 * Open an application by ID for edit
	 * Editable linked tables are implemented by the separate services like applicationDetails
	 * @param loginName
	 * @param userGroup 
	 * @param context
	 * @param appDTO
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@Transactional
	public ApplicationDTO applicationOpen(String loginName, String userGroup, Context context, ApplicationDTO appDTO) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		Import_permit model = boilerServ.loadApplication(appDTO.getId());
		User currentUser = boilerServ.userByUserLogin(loginName);
		appDTO =  entityToDTOServ.ApplicationToDto(context, model, loginName, userGroup, appDTO);
		appDTO.getDetails().setTotalAmount(FormFieldDTO.of(calcTotalAmount(model),2));
		appDTO=checkListService.questionsForPIP(appDTO,currentUser);
		appDTO = validator.validateDTO(appDTO, false, false);
		return appDTO;
	}


	/**
	 * Calc the total amount of products
	 * @param model
	 * @return
	 */
	private BigDecimal calcTotalAmount(Import_permit model) {
		BigDecimal ret = BigDecimal.ZERO;
		for(Import_permit_detail detail : model.getImport_permit_detail()) {
			BigDecimal units = detail.getUnits();
			BigDecimal price = detail.getPrice();
			if(units != null && price != null) {
				ret = ret.add(price.multiply(units).setScale(2)).setScale(2);
			}
		}
		return ret;
	}



	/**
	 * Save an application
	 * @param fetchUserLogin
	 * @param appDTO
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO applicationSave(String userLogin, String userRole, ApplicationDTO appDTO, Context context) throws ObjectNotFoundException, IOException {
		appDTO=save(appDTO,false,context,userLogin, userRole);
		appDTO=validator.validateDTO(appDTO, true, false);
		if(appDTO.isValid()) {
			appDTO=validator.validatePipDetails(appDTO);
		}
		return appDTO;
	}


	/**
	 * Save  application to the database, calculate the total
	 * @param appDTO
	 * @param strict - do not save in case of validation errors, otherwise save anyway
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	private ApplicationDTO save(ApplicationDTO appDTO, boolean strict, Context context, String userLogin, String userRole) throws IOException, ObjectNotFoundException {
		if(strict) {
			if(appDTO.isValid()) {
				appDTO.setLastSaved(null);
				return appDTO;
			}
		}
		//determine application model
		Import_permit model = new Import_permit();
		long id = appDTO.getId();
		if(id>0) {
			model = boilerServ.loadApplication(id);
		}
		model = dtoToEntityServ.applicationFromDTO(model, appDTO);
		if(entityToDTOServ.checkUserAccess(model, userLogin)) {
			model = appRepo.save(model);
			if(model.getPipNumber() == null) {
				model.setPipNumber(model.getPipStatus().getCode()+"/"+model.getId()); //assign temporary number
				model=appRepo.save(model);
				appDTO.setId(model.getId());
				appDTO = commonService.doneJob(context, userLogin, userRole, "draftcreated",model.getPipStatus().getCode(),appDTO);
			}
			appDTO = entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, appDTO);
		}
		appDTO.getDetails().setTotalAmount(new FormFieldDTO<BigDecimal>(calcTotalAmount(model)));
		return appDTO;
	}


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
		appsDTO.setApplicantName(user.getApplicant().getAppName());
		String select=boilerServ.loadSelect("applications");
		String where = createWhere(appsDTO, user);
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
	 * Create a where clause depends on the tab
	 * @param appsDTO
	 * @param user
	 * @return
	 */
	private String createWhere(ApplicationsDTO appsDTO, User user) {
		//restrict by applicant
		String ret = "applcntId="+user.getApplicant().getApplcntId();
		//restrict by statuses
		switch(appsDTO.getTabName()) {
		case "applications":
			ret=ret+ " and statCode not in ('RJCT','CNCL','ARCH')";
			break;
		case "invoiced":
			ret= ret+" and statCode='FACT'"; 
			break;
		case "permits":
			ret= ret+" and statCode='ARCH' and (Expiry_date > CURDATE() and ApprovalDate is not null)"; 
			break;
		case "archive":
			ret= ret+" and statCode in ('RJCT','CNCL') or (statCode='ARCH' and(ApprovalDate is not null and Expiry_date <= CURDATE()))"; 
			break;			
		default:
		}
		ret = restrictByButtons(appsDTO, ret);
		return ret;
	}

	/**
	 * where by buttons
	 * @param appsDTO 
	 * @param ret
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	private String restrictByButtons(ApplicationsDTO appsDTO, String ret)  {
		//restrict by application type
		OrderType order;
		try {
			if(appsDTO.getButtons().getAppType()>0) {
				order = dictServ.loadOrderType(appsDTO.getButtons().getAppType());
				ret = ret + "and ordertype='"+order.getCode()+"'";
			}
		} catch (ObjectNotFoundException e) {
			//nothing to do
		}
		//restrict by product type
		ItemType itype;
		try {
			if(appsDTO.getButtons().getProductType()>0) {
				itype = dictServ.loadItemType(appsDTO.getButtons().getProductType());
				ret = ret+ " and itemtype='"+itype.getCode()+"'";
			}
		} catch (ObjectNotFoundException e) {
			//nothing to do
		}
		return ret;
	}
	/**
	 * Create the headers
	 * @return
	 */
	private Headers applicationsHeaders() {
		Headers ret = ApplicationHeadersCommon();
		return ret;
	}

	/**
	 * Common part for all application headers in any application list
	 * @return
	 */
	public static Headers ApplicationHeadersCommon() {
		Headers ret = new Headers();
		ret.getHeaders().add(TableHeader.instanceOf(
				"Requested_date",
				"requested_date",
				true,
				true,
				true,
				TableHeader.COLUMN_LOCALDATE,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"apptype",
				"prod_app_type",
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"itemtype",
				"product_drug_type",
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"PipNumber",
				"permitNumber",
				true,
				true,
				true,
				TableHeader.COLUMN_LINK,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"statCode",
				"state",
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"ProformaNumber",
				"proformanumber",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"destination",
				"destination",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				0));	
		ret.getHeaders().add(TableHeader.instanceOf(
				"custom",
				"custom",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				0));	
		ret.getHeaders().add(TableHeader.instanceOf(
				"amount",
				"amount",
				true,
				true,
				true,
				TableHeader.COLUMN_LONG,
				0));	
		ret.getHeaders().add(TableHeader.instanceOf(
				"currency",
				"currency",
				false,
				false,
				false,
				TableHeader.COLUMN_STRING,
				0));
		ret.getHeaders().get(0).setSort(true);
		ret.getHeaders().get(0).setSortValue(TableHeader.SORT_DESC);
		return ret;
	}
	/**
	 * Expand a product quota
	 * @param userLogin
	 * @param appsDTO
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@Transactional
	public QuoteDTO quotasExpand(String userLogin, QuoteDTO qDTO, Context context) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		if(qDTO.getId()>0) {
			Optional<Product> prodo = prodRepo.findById(qDTO.getId());
			if(prodo.isPresent()) {
				User user = boilerServ.userByUserLogin(userLogin);
				//table for the common data
				qDTO.setTable(boilerServ.subTable(quotasTable(qDTO.getTable(),user,context),2));
				//table for the expanded data
				String select = boilerServ.loadSelect("productApplications");
				Headers headers = quotasExpandHeaders();
				boilerServ.translateHeaders(headers);
				List<TableRow> rows = jdbcRepo.qtbGroupReport(select, "","prod.id="+qDTO.getId(), headers);
				TableQtb.tablePage(rows, qDTO.getTableExpand());
				qDTO.getTable().getHeaders().getHeaders().get(0).setColumnType(TableHeader.COLUMN_LINK);
				qDTO.getTableExpand().setHeaders(headers);
				qDTO.setProductName(prodo.get().getProdName());
				qDTO.setManufacturer(companyByRole(prodo.get(), CompanyType.FIN_PROD_MANUF));
				qDTO.getTableExpand().setSelectable(false);
			}else {
				throw new ObjectNotFoundException("Product not found. ID is "+qDTO.getId());
			}
		}
		return qDTO;
	}

	private String companyByRole(Product product, CompanyType companyType) {
		String ret="";
		if(product.getProdCompanies() != null) {
			for(ProdCompany pc : product.getProdCompanies()) {
				if(pc.getCompanyType().equals(companyType)) {
					return pc.getCompany().getCompanyName();
				}
			}
		}
		return ret;
	}
	/**
	 * Headers for Quote Expanded table
	 * @return
	 */
	private Headers quotasExpandHeaders() {
		Headers ret = new Headers();
		ret.getHeaders().add(TableHeader.instanceOf(
				"perm.PipNumber",
				"permitNumber",
				true,
				false,
				false,
				TableHeader.COLUMN_STRING,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"perm.ProformaNumber",
				"proformanumber",
				true,
				false,
				false,
				TableHeader.COLUMN_STRING,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"supplier.Code",
				"supplier",
				false,
				false,
				false,
				TableHeader.COLUMN_STRING,
				20));
		ret.getHeaders().add(TableHeader.instanceOf(
				"perm.Requested_date",
				"requested_date",
				false,
				false,
				false,
				TableHeader.COLUMN_LOCALDATE,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"perm.Expiry_date",
				"valid_to",
				true,
				false,
				false,
				TableHeader.COLUMN_LOCALDATE,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"ipd.Price*ipd.Units",
				"global_quantity",
				true,
				false,
				false,
				TableHeader.COLUMN_DECIMAL,
				0));

		return ret;
	}
	/**
	 * Strict verify an application and save it anyway
	 * @param fetchUserLogin
	 * @param appDTO
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO applicationVerify(String userLogin, String userRole, ApplicationDTO appDTO, Context context) throws ObjectNotFoundException, IOException {
		appDTO=save(appDTO,false,context,userLogin, userRole);
		appDTO = validator.validateDTO(appDTO, true, true);
		if(appDTO.isValid()) {
			appDTO = validator.validatePipDetails(appDTO);
		}
		return appDTO;
	}

	/**
	 * Submit an application - verify and change the state
	 * @param userLogin
	 * @param appDTO
	 * @param context
	 * @return
	 * @throws IOException 
	 * @throws ObjectNotFoundException 
	 */
	public ApplicationDTO applicationSubmit(String userLogin, String userRole, ApplicationDTO appDTO, Context context) throws ObjectNotFoundException, IOException {
		appDTO.setValid(true);		
		Optional<Import_permit> modelo = pipRepo.findById(appDTO.getId());
		if(modelo.isPresent()) {
			ApplicationDTO modelDTO =  entityToDTOServ.ApplicationToDto(context, modelo.get(), userLogin,userRole, appDTO);
			modelDTO = validator.validateDTO(modelDTO, true, true);
			if(modelDTO.isValid()) {
				appDTO = validator.validatePipDetails(appDTO);
				appDTO = validator.validatePIPChecklist(modelo.get(), appDTO);
			}else {
				appDTO.setValid(modelDTO.isValid());
				appDTO.setAlertMessage(messages.get("editapplicationform"));
			}
			if(appDTO.isValid()) {
				appDTO = commonService.doneJob(context, userLogin, userRole, "submitregistersuccess","RQST",appDTO);
				if(appDTO.isValid()) {
					appDTO.setAlertMessage(messages.get("submitregistersuccess"));
				}
			}
			return appDTO;
		}else {
			throw new ObjectNotFoundException("Application not found. ID="+appDTO.getId(),logger);
		}
	}

	/**
	 * Load or create price and units for a product, i.e. Import_permit_detail
	 * @param userLogin
	 * @param userGroup
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException
	 */
	@Transactional
	public ApplicationDetailDTO applicationDetail(String userLogin, String userGroup, ApplicationDetailDTO data,
			Context context) throws ObjectNotFoundException {
		if(data.getId()>0) {
			//load an existing Import_permit_detail
			Optional<Import_permit_detail> detailo = iPermDetailRepo.findById(data.getId());
			if(detailo.isPresent()) {
				data = entityToDTOServ.ipDetailToDTO(data,detailo.get());
			}else {
				throw new ObjectNotFoundException(" Import permit detail not found. Id is "+data.getId(),logger);
			}
		}else {
			//create a new Import_permit_detail
			Product product = new Product();
			if(data.getProduct().getId()   >0) {
				product = boilerServ.loadProduct(data.getProduct().getId());
			}else {
				//we need to create a new product for Special Import and Medical Products
				product = dtoToEntityServ.productFromDTO(product, data.getProduct());
				product = prodRepo.save(product);
			}
			Import_permit ip = boilerServ.loadApplication(data.getIpId());
			Import_permit_detail detail = new Import_permit_detail();
			detail.setPrice(BigDecimal.ZERO);
			detail.setUnits(BigDecimal.ZERO);
			detail.setProduct(product);
			data.setProduct(entityToDTOServ.productToDto(product, data.getProduct()));
			data.setIpId(ip.getId());
		}
		return data;
	}
	/**
	 * Save existing Application Detail
	 * @param userLogin
	 * @param userGroup
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public ApplicationDetailDTO applicationDetailSave(String userLogin, String userGroup,
			ApplicationDetailDTO data, Context context) throws ObjectNotFoundException {
		data = validator.validateDTO(data, true, true);
		if(data.isValid()) {
			Import_permit_detail detail = new Import_permit_detail();
			Product product = new Product();
			Optional<Import_permit_detail> detailo = iPermDetailRepo.findById(data.getId());
			if(detailo.isPresent()) {
				//existing detail
				detail=detailo.get();
				product = detail.getProduct();
			}else {
				//new detail
				if(data.getProduct().getId()>0) {
					//product is existing
					product = boilerServ.loadProduct(data.getProduct().getId());
				}else {
					//new product
					product=dtoToEntityServ.productFromDTO(product, data.getProduct());
				}

			}
			if(product.getProdApplicationses() != null && product.getProdApplicationses().size()==0) {
				//Special Import or Medical Product
				product = dtoToEntityServ.productFromDTO(product, data.getProduct());
				product = prodRepo.save(product);
			}
			detail = dtoToEntityServ.detailFromDTO(data, detail);
			detail.setProduct(product);
			Import_permit ip = boilerServ.loadApplication(data.getIpId());
			ip.getImport_permit_detail().add(detail);
			ip=pipRepo.save(ip);
			for(Import_permit_detail ipd : ip.getImport_permit_detail()) {	//return new created ID doesn't trivial task :)
				if(ipd.getProduct().getId()==product.getId()) {
					data.setId(ipd.getId());
					break;
				}
			}
			detail=iPermDetailRepo.save(detail);
			data.setId(detail.getId());
		}
		return data;
	}
	/**
	 * Create a new empty draft application based on "buttons" property
	 * @param userLogin
	 * @param fetchGroup
	 * @param context
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public ApplicationsButtonsDTO applicationCreate(String userLogin, String fetchGroup, Context context,
			ApplicationsButtonsDTO data) throws ObjectNotFoundException {
		User user = boilerServ.userByUserLogin(userLogin);
		OrderType ordType = dictServ.loadOrderType(data.getAppType());
		ItemType itemTYpe= dictServ.loadItemType(data.getProductType());
		Applicant applicant = user.getApplicant();
		PipStatus status = dictServ.loadPipStatus("DRFT");
		if(applicant != null) {
			Import_permit ip = new Import_permit();
			ip.setApplicant(applicant);
			ip.setPipStatus(status);
			ip.setOrderType(ordType);
			ip.setItemType(itemTYpe);
			ip = pipRepo.save(ip);
			ip.setPipNumber(status.getCode()+"/"+ip.getId());
			ip.setRemark("");				//to avoid "unrecognized data type
			ip = pipRepo.save(ip);
			data.setNewApplicationId(ip.getId());
		}else {	
			data.setNewApplicationId(0l);
		}
		return data;
	}
	/**
	 * Delete a detail record
	 * A detail record points to a product. The product should be deleted only if it has not link to prodapplication 
	 * @param fetchUserLogin
	 * @param fetchGroup
	 * @param data
	 * @param context
	 * @return empty data
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public ApplicationDetailDTO applicationDetailDelete(String fetchUserLogin, String fetchGroup,
			ApplicationDetailDTO data, Context context) throws ObjectNotFoundException {
		Import_permit_detail ipd = boilerServ.loadProductDetail(data.getId());
		Product product = ipd.getProduct();
		iPermDetailRepo.delete(ipd);
		if(product.getProdApplicationses().size()==0) {
			prodRepo.delete(ipd.getProduct());
		}
		data.setId(0);
		data.setIpId(0);
		data.setProduct(new ProductDTO());
		return data;
	}
	/***
	 * Delete application draft
	 * @param userLogin
	 * @param userGroup
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO applicationDelete(String userLogin, String userRole, ApplicationDTO data,
			Context context) throws ObjectNotFoundException, IOException {
		Import_permit application = boilerServ.loadApplication(data.getId());
		if(application.getPipStatus().getCode().equalsIgnoreCase("DRFT")) {
			data = commonService.doneJob(context, userLogin, userRole, "is_deleted","ARCH",data);
		}
		data.setValid(true);
		return data;
	}


}
