package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.msh.pdex.dto.AnswerDTO;
import org.msh.pdex.dto.ApplicantDTO;
import org.msh.pdex.dto.AtcDTO;
import org.msh.pdex.dto.AttachmentDTO;
import org.msh.pdex.dto.DocTypesDTO;
import org.msh.pdex.dto.ManufacturerDTO;
import org.msh.pdex.dto.ProdExcipientDTO;
import org.msh.pdex.dto.ProdInnDTO;
import org.msh.pdex.dto.QuestionDTO;
import org.msh.pdex.dto.QuestionTemplateDTO;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableCell;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.dto.ApplicationDetailDTO;
import org.msh.pdex.ipermit.dto.ProductDTO;
import org.msh.pdex.model.Address;
import org.msh.pdex.model.AdminRoute;
import org.msh.pdex.model.Applicant;
import org.msh.pdex.model.Atc;
import org.msh.pdex.model.Company;
import org.msh.pdex.model.Country;
import org.msh.pdex.model.DosUom;
import org.msh.pdex.model.DosageForm;
import org.msh.pdex.model.Excipient;
import org.msh.pdex.model.Inn;
import org.msh.pdex.model.ProdApplications;
import org.msh.pdex.model.ProdCompany;
import org.msh.pdex.model.ProdExcipient;
import org.msh.pdex.model.ProdInn;
import org.msh.pdex.model.Product;
import org.msh.pdex.model.ResourceMessage;
import org.msh.pdex.model.Role;
import org.msh.pdex.model.User;
import org.msh.pdex.model.enums.AgeGroup;
import org.msh.pdex.model.enums.CompanyType;
import org.msh.pdex.model.enums.ProdCategory;
import org.msh.pdex.model.enums.UseCategory;
import org.msh.pdex.model.enums.YesNoType;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.Import_permit_detail;
import org.msh.pdex.model.pip.PIPDoc;
import org.msh.pdex.model.pip.PIPTrackDone;
import org.msh.pdex.model.pip.QuestionInstancePIP;
import org.msh.pdex.model.pip.QuestionTemplatePIP;
import org.msh.pdex.model.rsecond.AnswerQuestion;
import org.msh.pdex.model.rsecond.ApplicantDoc;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.model.rsecond.DocType;
import org.msh.pdex.model.rsecond.Document;
import org.msh.pdex.repository.AdminRouteRepo;
import org.msh.pdex.repository.ApplicantRepository;
import org.msh.pdex.repository.AtcRepo;
import org.msh.pdex.repository.CompanyRepo;
import org.msh.pdex.repository.CountryRepository;
import org.msh.pdex.repository.DosageFormRepo;
import org.msh.pdex.repository.DosageUnitRepo;
import org.msh.pdex.repository.ExcipientRepository;
import org.msh.pdex.repository.InnRepository;
import org.msh.pdex.repository.JdbcRepository;
import org.msh.pdex.repository.ProdCompanyRepo;
import org.msh.pdex.repository.ProdExcipientRepository;
import org.msh.pdex.repository.ProdInnRepository;
import org.msh.pdex.repository.ProductRepo;
import org.msh.pdex.repository.UserRepository;
import org.msh.pdex.repository.pip.PipDocRepo;
import org.msh.pdex.repository.rsecond.ApplicantDocRepo;
import org.msh.pdex.repository.rsecond.DocTypeRepo;
import org.msh.pdex.services.DictionaryService;
import org.msh.pdex.services.MagicEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This service is responsible for Entity to DTO conversion
 * This process is complex and, unlike DTO to Entity, cannot be automated
 * @author alexk
 *
 */
@Service
public class EntityToDtoService {
	public static final String MANUF_NAME_COLUMN = "manufname";
	//table headers
	public static final String PROD_NAME_COLUMN = "prodname";
	public static final String AMOUNT_COLUMN = "Amount";
	public static final String PACKS_COLUMN = "Packs";
	public static final String PRICE_COLUMN = "Price";
	private static final String DETAILS_HEADERS = null;

	@Autowired
	DictionaryService dict;
	@Autowired
	ApplicantRepository applRepo;
	@Autowired
	ApplicantDocRepo applDocRepo;
	@Autowired
	PipDocRepo pipDocRepo;
	@Autowired
	Messages messages;
	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	UserRepository userRepo;
	@Autowired
	DosageFormRepo dosageFormRepo;
	@Autowired
	DosageUnitRepo dosageUnitRepo;
	@Autowired
	MagicEntityService enttService;
	@Autowired
	AtcRepo atcRepo;
	@Autowired
	AdminRouteRepo adminRouteRepo;
	@Autowired
	ProdInnRepository prodInnRepository;
	@Autowired
	ProdExcipientRepository prodExcipientRepository;
	@Autowired
	InnRepository innRepository;
	@Autowired
	ExcipientRepository excipientRepository;
	@Autowired
	ProductRepo productRepo;
	@Autowired
	ProdCompanyRepo prodCompanyRepo;
	@Autowired
	CompanyRepo companyRepo;
	@Autowired
	CountryRepository countryRepository;
	@Autowired
	DocTypeRepo docTypeRepo;
	@Autowired
	JdbcRepository jdbcRepo;


	/**
	 * Create ApplicationDTO from Import_permit model object
	 * Check can the user access this application
	 * @param model
	 * @param userLogin
	 * @param appDTO 
	 * @return
	 * @throws IOException 
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public ApplicationDTO ApplicationToDto(Context context, Import_permit model, String userLogin, String userRole, ApplicationDTO appDTO) throws IOException, ObjectNotFoundException {
		ApplicationDTO ret = appDTO;
		if(checkUserAccess(model, userLogin)) {
			ret.setId(model.getId());
			ret.setOrderType(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getOrderType(),dict.allOrderTypes(),messages)));
			ret.setItemType(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getItemType(),dict.allItemTypes(),messages)));
			ret.setApplicant(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getApplicant())));
			
			ret.setCurrency(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getCurrency(),dict.allActiveCurrencies(),messages)));
			ret.setValidation_date(new FormFieldDTO<LocalDate>(createLocalDate(model.getValidation_date())));
			ret.setAuth_date(createOrGetAuthDate(model));
			ret.setInspector(createOrGetInspector(model));
			ret.setApprover(createOrGetApprover(model));
			ret.setExpiry_date(new FormFieldDTO<LocalDate>(createLocalDate(model.getExpiry_date())));
			
			ret.setIncoterms(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getIncoterms(), dict.allActiveIncoterms(),messages)));
			ret.setPipNumber(new FormFieldDTO<String>(model.getPipNumber()));
			ret.setPipStatus(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getPipStatus(), dict.allActivePipStats(),messages)));
			ret.setPort(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getPort(), dict.allActivePorts(),messages)));
			ret.setProformaNumber(new FormFieldDTO<String>(model.getProformaNumber()));
			ret.setRemark(new FormFieldDTO<String>(model.getRemark()));
			ret.setRequested_date(new FormFieldDTO<LocalDate>(new LocalDate(model.getRequested_date())));
			ret.setTransport(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getTransport(), dict.allActiveTransports(),messages)));
			ret.setShowCancel(applicationCancelCondition(model,userLogin, userRole));
			ret.setCustom(new FormFieldDTO<OptionDTO>(OptionDTO.of(model.getCustom(),dict.allActiveCustoms())));
			ret.setApprovalDate(new FormFieldDTO<LocalDate>(createLocalDate(model.getApprovalDate())));
				
			ret=loadDetailsTables(context, ret, model);

		}
		return ret;
	}
	/**
	 * Load import permit details both full and short
	 * @param model
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@Transactional
	private ApplicationDTO loadDetailsTables(Context context, ApplicationDTO data, Import_permit model) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		String key = "product_permit";
		String where= "importPermitID="+model.getId();
		if(!data.getDetails().isShowDetails()) {
			key="productsApplicant";
			List<String> ids = new ArrayList<String>();
			for(Import_permit_detail ipd : model.getImport_permit_detail()) {
				ids.add(ipd.getProduct().getId()+"");
			}
			where = "applcntid="+model.getApplicant().getApplcntId();
			if(ids.size()>0) {
				where = where + " and ID not in ("+ String.join(",", ids) +")";
			}
		}
		String select = boilerServ.loadSelect(key);
		CreateHeaders createHeaders=this::detailsPIPHeaders;
		Headers headers = boilerServ.loadHeaders(context, DETAILS_HEADERS, data.getDetails().getDetails(), createHeaders);
		List<TableRow> rows = jdbcRepo.qtbGroupReport(select,"" ,where, headers);
		data.getDetails().getDetails().setHeaders(headers);
		TableQtb.tablePage(rows, data.getDetails().getDetails());
		data.getDetails().setDetailsShort(boilerServ.subTable(data.getDetails().getDetails(), 1));
		data.getDetails().getDetails().setSelectable(false);
		data.getDetails().getDetailsShort().setSelectable(false);
		return data;
	}
	
	/**
	 * Create headers for list of Import_permin_detail in PIP application
	 * @return
	 */
	public Headers detailsPIPHeaders() {
		Headers ret = quotasHeaders();
		ret.getHeaders().add(TableHeader.instanceOf(
				EntityToDtoService.PRICE_COLUMN,
				"price",
				true,
				true,
				true,
				TableHeader.COLUMN_DECIMAL,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				EntityToDtoService.PACKS_COLUMN,
				"packs",
				true,
				true,
				true,
				TableHeader.COLUMN_DECIMAL,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				EntityToDtoService.AMOUNT_COLUMN,
				"quotas",
				false,
				true,
				true,
				TableHeader.COLUMN_DECIMAL,
				0));

		ret.setSelectedOnly(true);
		return ret;
	}
	/**
	 * Headers for products 
	 * @return
	 */
	public Headers quotasHeaders() {
		Headers ret = new Headers();
		ret.getHeaders().add(TableHeader.instanceOf(
				EntityToDtoService.PROD_NAME_COLUMN,
				"prod_name",
				true,
				true,
				true,
				TableHeader.COLUMN_LINK,
				40));
		ret.getHeaders().add(TableHeader.instanceOf(
				"description",
				"global_description",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				0));	
		ret.getHeaders().add(TableHeader.instanceOf(
				"countryName",
				"applicant_country",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				40));
		ret.getHeaders().add(TableHeader.instanceOf(
				EntityToDtoService.MANUF_NAME_COLUMN,
				"manuf_name",
				false,
				true,
				true,
				TableHeader.COLUMN_STRING,
				40));
		ret.getHeaders().add(TableHeader.instanceOf(
				"regno",
				"reg_number",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				0));
		return ret;

	}
	/**
	 * Calc total amount from model
	 * @param import_permit_detail
	 * @return
	 */
	public FormFieldDTO<BigDecimal> calcPipTotalAmount(Set<Import_permit_detail> import_permit_detail) {
		BigDecimal total = BigDecimal.ZERO;
		for(Import_permit_detail ipd :import_permit_detail) {
			BigDecimal amount = ipd.getPrice().multiply(ipd.getUnits());
			total=total.add(amount);
		}
		return FormFieldDTO.of(total,2);
	}

	/**
	 * Get validation date directly or assign todays date
	 * @param model 
	 * @return
	 */
	private LocalDate createLocalDate(Date date) {
		if(date!=null) {
			return new LocalDate(date);
		}else {
			return null;
		}
	}

	/**
	 * Propose an inspector
	 * @param model
	 * @return
	 */
	private FormFieldDTO<OptionDTO> createOrGetInspector(Import_permit model) {
		User inspector=findInspector(model);
		List<User> inspectors = new ArrayList<User>();
		Iterable<User> allUSers = userRepo.findAll();
		for(User u : allUSers) {
			if( boilerServ.hasRole(u, "responsible")) {
				inspectors.add(u);
			}
		}
		OptionDTO options = OptionDTO.of(inspector, inspectors);
		return new FormFieldDTO<OptionDTO>(options);
	}

	/**
	 * Propose an approver
	 * @param model
	 * @return
	 */
	@Transactional
	private FormFieldDTO<OptionDTO> createOrGetApprover(Import_permit model) {
		User approver=model.getApprover();
		List<User> approvers = new ArrayList<User>();
		Iterable<User> allUSers = userRepo.findAll();
		for(User u : allUSers) {
			if( boilerServ.hasRole(u, "responsible") || boilerServ.hasRole(u, "moderator") || boilerServ.hasRole(u, "import_expert") ) {
				approvers.add(u);
			}
		}
		OptionDTO options = OptionDTO.of(approver, approvers);
		return new FormFieldDTO<OptionDTO>(options);
	}

	/**
	 * Find the latest import responsible
	 * @param model
	 * @return
	 */
	private User findInspector(Import_permit model) {
		User ret = null;
		for(PIPTrackDone done :model.getJobsDone()) {
			if(done.getJobCode().equals("submitforvalidation")) {
				ret=done.getUserExecutor();
			}
		}
		return ret;
	}

	/**
	 * Get auth date directly from model or propose auth date from "done" list 
	 * @param model
	 * @return
	 */
	private FormFieldDTO<LocalDate> createOrGetAuthDate(Import_permit model) {
		LocalDate authDate= null;
		if(model.getAuth_date() == null) {
			for(PIPTrackDone done :model.getJobsDone()) {
				if(done.getJobCode().equals("submitforvalidation")) {
					authDate=new LocalDate(done.getCompleted());
				}
			}
		}else {
			authDate=new LocalDate(model.getAuth_date());
		}
		return new FormFieldDTO<LocalDate>(authDate);
	}

	/**
	 * who can cancel it
	 * @param model
	 * @param userLogin
	 * @param fetchGroup 
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public boolean applicationCancelCondition(Import_permit model, String userLogin, String fetchGroup) throws ObjectNotFoundException {
		if(model.getPipStatus().getCode().equalsIgnoreCase("CNCL")) {
			return false;
		}
		User user = boilerServ.userByUserLogin(userLogin);
		if(user.getApplicant()!=null) {
			return model.getApplicant().getApplcntId()==user.getApplicant().getApplcntId();
		}else {
			return false;
			//return fetchGroup.equalsIgnoreCase( "moderator");
		}
	}

	/**
	 * Company user may has access only to applications of the company
	 * @param model
	 * @param userLogin
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public boolean checkUserAccess(Import_permit model, String userLogin) throws ObjectNotFoundException {
		User user = boilerServ.userByUserLogin(userLogin);
		if(user.getApplicant()!=null) {
			return model.getApplicant().getApplcntId()==user.getApplicant().getApplcntId();
		}else {
			return true;		//any DNF user may has access to all applications
		}
	}


	/**
	 * Create DTO object from Document (common ancestor of all attachments) model
	 * @param doc
	 * @param att
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public AttachmentDTO documentToDto(AttachmentDTO att) throws ObjectNotFoundException {
		Document doc = boilerServ.document(att.getId());
		if(doc instanceof ApplicantDoc){
			att.setApplicantId(((ApplicantDoc) doc).getApplicant().getApplcntId());
		}
		if(doc instanceof PIPDoc) {
			att.setPipId(((PIPDoc) doc).getImport_permit().getId());
		}
		if(doc != null) {
			att.setDocType(new FormFieldDTO<OptionDTO>(OptionDTO.of(doc.getDocType(), dict.allActiveDocTypes(),messages)));
			att.setAnnotation(new FormFieldDTO<String>(doc.getAnnotation()));
			att.setSignDate(new FormFieldDTO<LocalDate>(new LocalDate(doc.getSignDate())));
			att.setSignNo(new FormFieldDTO<String>(doc.getSignNo()));
			att.setSignPerson(new FormFieldDTO<String>(doc.getSingPerson()));
			att.setAttachedby(new FormFieldDTO<String>(doc.getAuthor().getName()));
			att.setModifiedwhen(new FormFieldDTO<LocalDateTime>(new LocalDateTime(doc.getModified())));
			att.getFileName().setValue(doc.getFileName());
			att.getFileSize().setValue(doc.getFileSize());
			att.getFileContentType().setValue(doc.getContentEncoding());
			att.setId(doc.getId());
			return att;
		}else {
			throw new ObjectNotFoundException("Attached document not found. Id is " + att.getId());
		}
	}


	/**
	 * Convert product model to DTO
	 * @param product
	 * @param data
	 * @return
	 */
	@Transactional
	public ProductDTO productToDto(Product product, ProductDTO data) {
		data.setId(product.getId());
		data.setProd_name(new FormFieldDTO<String>(product.getProdName()));
		data.setProdgenname(new FormFieldDTO<String>(product.getGenName()));

		data.setAdmin_route(adminRouteToDTO(product.getAdminRoute()));
		data.setProd_cat(prodCatToDTO(product.getProdCategory()));
		data.setAge_group(ageGroupToDTO(product.getAgeGroup()));
		data.setProd_desc(new FormFieldDTO<String>(product.getProdDesc()));

		data.setDos_form(dosFormToDTO(product.getDosForm()));
		data.setDos_unit(dosUnitToDTO(product.getDosUnit()));
		data.setDos_strength(new FormFieldDTO<String>(product.getDosStrength()));

		data.setAtc(atcListToDTO(product.getAtcs()));
		data.setInns(productInns(product));
		data.setExcipients(productExcipients(product));
		data.setFinProdManuf(productManufFinish(product));
		data.setManufacturers(productManuf(product));
		data.setProd_indications(new FormFieldDTO<String>(product.getIndications()));

		data.setProd_posology(new FormFieldDTO<String>(product.getPosology()));
		data.setProduct_conttype(new FormFieldDTO<String>(product.getContType()));
		data.setProduct_packsize(new FormFieldDTO<String>(product.getPackSize()));
		data.setProduct_shelflife(new FormFieldDTO<String>(product.getShelfLife()));
		data.setProduct_storcndtn(new FormFieldDTO<String>(product.getStorageCndtn()));

		data.setNarc(isNarcCategory(product.getUseCategories()));
		data.setPrescr(isPrescriptionCategory(product.getUseCategories()));
		data.setHospital(isHospitalCategory(product.getUseCategories()));
		data.setOtc(isOtcCategory(product.getUseCategories()));

		if(product.getProdApplicationses() != null && product.getProdApplicationses().size()>0) {
			int latestProdApp = product.getProdApplicationses().size()-1;
			ProdApplications application = product.getProdApplicationses().get(latestProdApp);
			data.getLicense_holder().setValue(application.getApplicant().getAppName());
			data.getAddress().setValue(
					(application.getApplicant().getAddress().getAddress1()+" "+
							application.getApplicant().getAddress().getAddress2()+" "+
							application.getApplicant().getAddress().getCountry()
							).replaceAll("  ", " ")
					);
			data.getReg_number().setValue(application.getProdRegNo());
			data.getRegistration_date().setValue(new LocalDate(application.getRegistrationDate()));
			data.getValid_to().setValue(new LocalDate(application.getRegExpiryDate()));
		}
		if(product.getFnm()!= null) {
			data.getProd_fnm().setValue(product.getFnm());
		}
		if(product.getMedical_product() != null) {
			data.setMedical_product(FormFieldDTO.of(product.getMedical_product()));
		}
		return data;
	}
	/**
	 * A product may contain list of ATC
	 * @param atcs
	 * @return
	 */
	@Transactional
	private List<AtcDTO> atcListToDTO(List<Atc> atcs) {
		List<AtcDTO> ret = new ArrayList<AtcDTO>();
		if(atcs != null && atcs.size() > 0) {
			ret = fillAtcList(atcs, ret);
		}else {
			ret = initAtcList(ret);
		}
		return ret;
	}
	/**
	 * Fill a list of ATCs (typically it is only one ATC for a product)
	 * @param atcs 
	 * @param ret
	 * @return
	 */
	@Transactional
	private List<AtcDTO> fillAtcList(List<Atc> atcs, List<AtcDTO> ret) {
		ret.clear();
		for(Atc atc:atcs)
			ret.add(atcToDTO(atc));

		return ret;
	}
	/**
	 * ATC record to DTO
	 * @param atc
	 * @return
	 */
	@Transactional
	private AtcDTO atcToDTO(Atc atc) {
		AtcDTO ret = new AtcDTO();

		List<OptionDTO> list = new ArrayList<OptionDTO>();

		int level = atc.getLevel();
		switch (level) {
		case 1:
			list.add(ret.getAtc_1().getValue());
			break;
		case 2:
			list.add(ret.getAtc_2().getValue());
			list.add(ret.getAtc_1().getValue());
			break;
		case 3:
			list.add(ret.getAtc_3().getValue());
			list.add(ret.getAtc_2().getValue());
			list.add(ret.getAtc_1().getValue());
			break;

		case 4:
			list.add(ret.getAtc_4().getValue());
			list.add(ret.getAtc_3().getValue());
			list.add(ret.getAtc_2().getValue());
			list.add(ret.getAtc_1().getValue());
			break;

		case 5:
			list.add(ret.getAtc_5().getValue());
			list.add(ret.getAtc_4().getValue());
			list.add(ret.getAtc_3().getValue());
			list.add(ret.getAtc_2().getValue());
			list.add(ret.getAtc_1().getValue());
			break;

		default:
			break;
		} 

		setAtcInfo(list, atc, 0);

		return ret;
	}

	@Transactional
	private List<AtcDTO> initAtcList(List<AtcDTO> ret) {
		ret.clear();

		AtcDTO dto = new AtcDTO();
		dto.getAtc_1().getValue().setOptions(buildListAtcDTO(null, dto.getAtc_1().getValue()));
		ret.add(dto);

		return ret;
	}

	public AtcDTO reloadAtc(AtcDTO atcDto) {
		String code = "";
		if(atcDto.getAtc_1().getValue().getId() > 0) {
			atcDto.getAtc_1().getValue().setOptions(buildListAtcDTO(null, atcDto.getAtc_1().getValue()));
			code = atcDto.getAtc_1().getValue().getCode();

			if(code.length() > 0) {//atcDto.getAtc_2().getValue().getId() > 0 && 
				atcDto.getAtc_2().getValue().setOptions(buildListAtcDTO(code, atcDto.getAtc_2().getValue()));
				code = atcDto.getAtc_2().getValue().getCode();

				if(code.length() > 0) {
					atcDto.getAtc_3().getValue().setOptions(buildListAtcDTO(code, atcDto.getAtc_3().getValue()));
					code = atcDto.getAtc_3().getValue().getCode();

					if(code.length() > 0) {
						atcDto.getAtc_4().getValue().setOptions(buildListAtcDTO(code, atcDto.getAtc_4().getValue()));
						code = atcDto.getAtc_4().getValue().getCode();

						if(code.length() > 0) {
							atcDto.getAtc_5().getValue().setOptions(buildListAtcDTO(code, atcDto.getAtc_5().getValue()));
						}else {
							atcDto.setAtc_5(new FormFieldDTO<OptionDTO>(new OptionDTO()));
						}
					}else {
						atcDto.setAtc_4(new FormFieldDTO<OptionDTO>(new OptionDTO()));
						atcDto.setAtc_5(new FormFieldDTO<OptionDTO>(new OptionDTO()));
					}
				}else {
					atcDto.setAtc_3(new FormFieldDTO<OptionDTO>(new OptionDTO()));
					atcDto.setAtc_4(new FormFieldDTO<OptionDTO>(new OptionDTO()));
					atcDto.setAtc_5(new FormFieldDTO<OptionDTO>(new OptionDTO()));
				}
			}else {
				atcDto.setAtc_2(new FormFieldDTO<OptionDTO>(new OptionDTO()));
				atcDto.setAtc_3(new FormFieldDTO<OptionDTO>(new OptionDTO()));
				atcDto.setAtc_4(new FormFieldDTO<OptionDTO>(new OptionDTO()));
				atcDto.setAtc_5(new FormFieldDTO<OptionDTO>(new OptionDTO()));
			}
		}else {
			atcDto.setAtc_2(new FormFieldDTO<OptionDTO>(new OptionDTO()));
			atcDto.setAtc_3(new FormFieldDTO<OptionDTO>(new OptionDTO()));
			atcDto.setAtc_4(new FormFieldDTO<OptionDTO>(new OptionDTO()));
			atcDto.setAtc_5(new FormFieldDTO<OptionDTO>(new OptionDTO()));
		}

		return atcDto;
	}

	private void setAtcInfo(List<OptionDTO> lst, Atc atc, int index) {
		if(index >= lst.size())
			return;
		OptionDTO dto = lst.get(index);

		dto.setCode(atc.getAtcCode());
		dto.setDescription(atc.getAtcName());
		Atc parent = atc.getParent();
		if(parent != null) {
			dto.setOptions(buildListAtcDTO(parent.getAtcCode(), dto));

			int n = index + 1;
			setAtcInfo(lst, parent, n);
		}else
			dto.setOptions(buildListAtcDTO(null, dto));
	}

	private List<OptionDTO> buildListAtcDTO(String parentCode, OptionDTO option){
		List<OptionDTO> list = new ArrayList<OptionDTO>();
		if(parentCode == null) {
			long curId = -1;
			List<Atc> atcs = atcRepo.findByParentIsNull();
			if(atcs != null && atcs.size() > 0) {
				for(int i = 0; i < atcs.size(); i++) {
					Atc a = atcs.get(i);
					OptionDTO dto = new OptionDTO();
					dto.setId(i + 1);
					dto.setCode(a.getAtcCode());
					dto.setDescription(a.getAtcName());
					list.add(dto);

					if(a.getAtcCode().equals(option.getCode()))
						curId = dto.getId();
				}
			}
			if(curId > 0)
				option.setId(curId);
			else {
				option.setId(0);
				option.setCode("");
				option.setDescription("");
			}
		}else if(parentCode.length() > 0) {
			long curId = -1;
			List<Atc> atcs = atcRepo.findByParentLikeByAtcCode(parentCode);
			if(atcs != null && atcs.size() > 0) {
				for(int i = 0; i < atcs.size(); i++) {
					Atc a = atcs.get(i);
					OptionDTO dto = new OptionDTO();
					dto.setId(i + 1);
					dto.setCode(a.getAtcCode());
					dto.setDescription(a.getAtcName());
					list.add(dto);

					if(a.getAtcCode().equals(option.getCode()))
						curId = dto.getId();
				}
			}
			if(curId > 0)
				option.setId(curId);
			else {
				option.setId(0);
				option.setCode("");
				option.setDescription("");
			}
		}
		return list;
	}

	/**
	 * Dosage unit to field
	 * @param dosUnit
	 * @return
	 */
	public FormFieldDTO<OptionDTO> dosUnitToDTO(DosUom dosUnit) {
		OptionDTO opt = dosUnitToOptionDTO(dosUnit);
		Iterable<DosUom> dus = dosageUnitRepo.findAll();
		if(dus != null) {
			for(DosUom du : dus) {
				OptionDTO duo = dosUnitToOptionDTO(du);
				opt.getOptions().add(duo);
			}
		}
		return new FormFieldDTO<OptionDTO>(opt);
	}
	/**
	 * Dosage unit to OptionDTO
	 * @param dosUnit
	 * @return
	 */
	private OptionDTO dosUnitToOptionDTO(DosUom dosUnit) {
		OptionDTO ret = new OptionDTO();
		if(dosUnit != null) {
			ret.setCode(dosUnit.getUom());
			ret.setId(new Long(dosUnit.getId()));
		}
		return ret;
	}

	/**
	 * Convert Dosage Form to FormField
	 * @param dosForm
	 * @return
	 */
	private FormFieldDTO<OptionDTO> dosFormToDTO(DosageForm dosForm) {
		OptionDTO opt = dosFormToOptionDTO(dosForm);
		Iterable<DosageForm> dforms = dosageFormRepo.findAll();
		if(dforms != null) {
			for(DosageForm df : dforms) {
				OptionDTO oform = dosFormToOptionDTO(df);
				opt.getOptions().add(oform);
			}
		}
		return new FormFieldDTO<OptionDTO>(opt);
	}

	/**
	 * Convert AdminRoute Form to FormField
	 * @param dosForm
	 * @return
	 */
	private FormFieldDTO<OptionDTO> adminRouteToDTO(AdminRoute admRoute) {
		OptionDTO opt = adminRouteToOptionDTO(admRoute);
		Iterable<AdminRoute> adms = adminRouteRepo.findAll();
		if(adms != null) {
			for(AdminRoute ar : adms) {
				OptionDTO oform = adminRouteToOptionDTO(ar);
				opt.getOptions().add(oform);
			}
		}
		return new FormFieldDTO<OptionDTO>(opt);
	}

	/**
	 * Convert AdminRoute form to OptionDTO
	 * @param df
	 * @return
	 */
	private OptionDTO adminRouteToOptionDTO(AdminRoute ar) {
		OptionDTO ret = new OptionDTO();
		if(ar != null) {
			ret.setCode(ar.getName());
			ret.setDescription(ar.getCode());
			ret.setOriginalCode(ar.getName());
			ret.setOriginalDescription(ar.getCode());
			ret.setId(ar.getId());
		}
		return ret;
	}

	/**
	 * Convert dosage form to OptionDTO
	 * @param df
	 * @return
	 */
	private OptionDTO dosFormToOptionDTO(DosageForm df) {
		OptionDTO ret = new OptionDTO();
		if(df != null) {
			ret.setCode(df.getDosForm());
			ret.setId(df.getUid());
		}
		return ret;
	}
	/**
	 * Schedule is Hospital 
	 * @param list of all schediles
	 * @return
	 */
	private FormFieldDTO<OptionDTO> isHospitalCategory(List<UseCategory> list){
		FormFieldDTO<OptionDTO> ffdto = booleanToYesNo(false);
		if(list != null && list.size() > 0) {
			boolean value = list.contains(UseCategory.HOSPITAL);
			ffdto = booleanToYesNo(value);
		}
		return ffdto;
	}
	/**
	 * Schedule is narcotic 
	 * @param list of all schediles
	 * @return
	 */
	private FormFieldDTO<OptionDTO> isNarcCategory(List<UseCategory> list){
		FormFieldDTO<OptionDTO> ffdto = booleanToYesNo(false);
		if(list != null && list.size() > 0) {
			boolean value = list.contains(UseCategory.SCH_NARCOTIC);
			ffdto = booleanToYesNo(value);
		}
		return ffdto;
	}
	/**
	 * Schedule is Over The Counter 
	 * @param list of all schediles
	 * @return
	 */
	private FormFieldDTO<OptionDTO> isOtcCategory(List<UseCategory> list){
		FormFieldDTO<OptionDTO> ffdto = booleanToYesNo(false);
		if(list != null && list.size() > 0) {
			boolean value = list.contains(UseCategory.OTC);
			ffdto = booleanToYesNo(value);
		}
		return ffdto;
	}
	/**
	 * Schedule is Prescription only 
	 * @param list of all schediles
	 * @return
	 */
	private FormFieldDTO<OptionDTO> isPrescriptionCategory(List<UseCategory> list){
		FormFieldDTO<OptionDTO> ffdto = booleanToYesNo(false);
		if(list != null && list.size() > 0) {
			boolean value = list.contains(UseCategory.PRESCRIPTION);
			ffdto = booleanToYesNo(value);
		}
		return ffdto;
	}
	/**
	 * Represent boolean value as Yes or No enum and then pack to OptionDTO
	 * @param value
	 * @return
	 */
	private FormFieldDTO<OptionDTO> booleanToYesNo(boolean value) {
		YesNoType ynType = YesNoType.NO;
		if(value)
			ynType = YesNoType.YES;

		OptionDTO opt = enumYNToOptionDTO(ynType);
		for(YesNoType t:YesNoType.values()) {
			OptionDTO dto = enumYNToOptionDTO(t);
			opt.getOptions().add(dto);
		}
		return new FormFieldDTO<OptionDTO>(opt);
	}
	/**
	 * Yes/No enum to OptinDTO
	 * @param t
	 * @return
	 */
	private OptionDTO enumYNToOptionDTO(YesNoType t) {
		OptionDTO ret = new OptionDTO();
		if(t != null) {
			ret.setCode(messages.get(t.getKey()));
			ret.setOriginalCode(t.getKey());
			ret.setId(t.ordinal() + 1);
		}
		return ret;
	}
	/**
	 * List of all manufacturers DTO from a product entity
	 * @param product
	 * @return
	 */
	private List<ManufacturerDTO> productManuf(Product product) {
		List<ManufacturerDTO> list = new ArrayList<ManufacturerDTO>();
		List<ProdCompany> manufs = product.getProdCompanies();
		if(manufs != null) {
			for(ProdCompany pc:manufs) {
				if(pc.getCompany() != null && !pc.getCompanyType().name().equals(CompanyType.FIN_PROD_MANUF.name())) {
					ManufacturerDTO dto = new ManufacturerDTO();
					dto.setId(pc.getId());
					dto.setUniqueKey("man" + pc.getId());
					if(pc.getCompany() != null) {
						dto.getManuf_name().getValue().setCode(pc.getCompany().getCompanyName());
						dto.getManuf_name().getValue().setId(pc.getCompany().getId());
						
						dto.setApplicant_country(countryToDTO(pc.getCompany().getAddress()));
					}

					dto.setCompany_type(companyTypeToOptionDTO(pc.getCompanyType()));

					list.add(dto);
				}
			}
		}

		return list;
	}
	/**
	 * Who is finished product manufacturer? DTO from Entity
	 * @param product
	 * @return
	 */
	private ManufacturerDTO productManufFinish(Product product) {
		ManufacturerDTO finishManuf = new ManufacturerDTO();
		ProdCompany prodcompany = null;
		
		List<ProdCompany> manufs = product.getProdCompanies();
		if(manufs != null) {
			for(ProdCompany pc:manufs) {
				if(pc.getCompany() != null && pc.getCompanyType().name().equals(CompanyType.FIN_PROD_MANUF.name())) {
					prodcompany = pc;
					break;
				}
			}
		}
		
		if(prodcompany != null) {
			finishManuf.setId(prodcompany.getId());
			finishManuf.setUniqueKey("man" + prodcompany.getId());
			if(prodcompany.getCompany() != null) {
				finishManuf.getManuf_name().getValue().setCode(prodcompany.getCompany().getCompanyName());
				finishManuf.getManuf_name().getValue().setId(prodcompany.getCompany().getId());

				finishManuf.setApplicant_country(countryToDTO(prodcompany.getCompany().getAddress()));
			}
		}else {
			finishManuf.setId(0);
			finishManuf.setUniqueKey("finmanuf" + 0);
			if(product.getManufName() != null)
				finishManuf.getManuf_name().getValue().setCode(product.getManufName());
			else
				finishManuf.getManuf_name().getValue().setCode("");
			finishManuf.getManuf_name().getValue().setId(0);

			finishManuf.setApplicant_country(countryToDTO(null));
		}

		OptionDTO type = enumCompanyTypeToOptionDTO(CompanyType.FIN_PROD_MANUF);
		finishManuf.setCompany_type(new FormFieldDTO<OptionDTO>(type));

		return finishManuf;
	}
	/**
	 * Sometimes we will need Other... option to make a choice
	 * @return
	 */
	public OptionDTO createOtherOptionDTO() {
		OptionDTO dto = new OptionDTO();
		dto.setCode(messages.get("lbl_other"));
		dto.setId(-1);
		
		return dto;
	}
	/**
	 * Country from Address Entity 
	 * @param adr
	 * @return
	 */
	public FormFieldDTO<OptionDTO> countryToDTO(Address adr){
		OptionDTO option = new OptionDTO();
		if(adr != null) {
			if(adr.getCountry() != null) {
				option = countryToOptionDTO(adr.getCountry());
			}else {
				if(adr.getAddress2() != null) {
					option.setCode(adr.getAddress2());
				}
			}
		}
		Iterable<Country> iter = countryRepository.findAll();
		for(Country it:iter) {
			OptionDTO dto = countryToOptionDTO(it);
			option.getOptions().add(dto);
		}
		return new FormFieldDTO<OptionDTO>(option);
	}
	/**
	 * Create OptionDTO from a country to use in Oracle field
	 * @param c
	 * @return
	 */
	public OptionDTO countryToOptionDTO(Country c) {
		OptionDTO dto = new OptionDTO();
		if(c != null) {
			dto.setId(c.getId());
			dto.setCode(c.getCountryName());
		}
		return dto;
	}
	/**
	 * Manufacturer type choice list
	 * @param saved
	 * @return
	 */
	public FormFieldDTO<OptionDTO> companyTypeToOptionDTO(CompanyType saved) {
		OptionDTO opt = new OptionDTO();
		if(saved != null) {
			opt = enumCompanyTypeToOptionDTO(saved);
		}

		for(CompanyType t:CompanyType.values()) {
			if(!t.name().equals(CompanyType.FIN_PROD_MANUF.name())) {
				OptionDTO dto = enumCompanyTypeToOptionDTO(t);
				opt.getOptions().add(dto);
			}
		}
		return new FormFieldDTO<OptionDTO>(opt);
	}
	/**
	 * Possible types of manufacturer are from the enum
	 * @param t
	 * @return
	 */
	private OptionDTO enumCompanyTypeToOptionDTO(CompanyType t) {
		OptionDTO ret = new OptionDTO();
		if(t != null) {
			ret.setCode(messages.get(t.getKey()));
			ret.setOriginalCode(t.getKey());
			ret.setId(t.ordinal() + 1);
		}
		return ret;
	}
	/**
	 * List of active ingredients from a Product entity
	 * @param product
	 * @return
	 */
	public List<ProdInnDTO> productInns(Product product) {
		List<ProdInnDTO> list = new ArrayList<ProdInnDTO>();

		List<ProdInn> prodinns = product.getInns();
		for(ProdInn pi:prodinns) {
			ProdInnDTO dto = new ProdInnDTO();
			dto.setId(pi.getId());
			dto.setUniqueKey("inn" + pi.getId());
			if(pi.getInn() != null) {
				dto.getProduct_innname().getValue().setCode(pi.getInn().getName());
				dto.getProduct_innname().getValue().setId(pi.getInn().getId());
			}

			dto.getDos_strength().setValue(pi.getDosStrength());
			dto.setDos_unit(dosUnitToDTO(pi.getDosUnit()));

			list.add(dto);
		}

		return list;
	}

	/**
	 * List of Product Excipients DTO from a Product entity
	 * @param product
	 * @return
	 */
	public List<ProdExcipientDTO> productExcipients(Product product) {
		List<ProdExcipientDTO> list = new ArrayList<ProdExcipientDTO>();

		List<ProdExcipient> prodexcs = product.getExcipients();
		for(ProdExcipient pe:prodexcs) {
			ProdExcipientDTO dto = new ProdExcipientDTO();
			dto.setId(pe.getId());
			dto.setUniqueKey("exc" + pe.getId());
			if(pe.getExcipient() != null) {
				dto.getProduct_active().getValue().setCode(pe.getExcipient().getName());
				dto.getProduct_active().getValue().setId(pe.getExcipient().getId());
			}

			dto.getDos_strength().setValue(pe.getDosStrength());
			if(pe.getDosUnit() != null) {
				dto.setDos_unit(dosUnitToDTO(pe.getDosUnit()));
			}

			list.add(dto);
		}

		return list;
	}

	/**
	 * Product category to DTO
	 * @param prodCat
	 * @return
	 */
	private FormFieldDTO<OptionDTO> prodCatToDTO(ProdCategory prodCat) {
		OptionDTO opt = prodCatToOptionDTO(prodCat);
		for(ProdCategory pc:ProdCategory.values()) {
			OptionDTO oform = prodCatToOptionDTO(pc);
			opt.getOptions().add(oform);
		}

		return new FormFieldDTO<OptionDTO>(opt);
	}

	/**
	 * Convert ProdCategory form to OptionDTO
	 * @param df
	 * @return
	 */
	private OptionDTO prodCatToOptionDTO(ProdCategory prodCat) {
		OptionDTO ret = new OptionDTO();
		if(prodCat != null) {
			ret.setCode(messages.get(prodCat.getKey()));
			ret.setId(prodCat.ordinal() + 1);
		}
		return ret;
	}
	/**
	 * Age group for choice
	 * @param agegr
	 * @return
	 */
	private FormFieldDTO<OptionDTO> ageGroupToDTO(AgeGroup agegr) {
		OptionDTO opt = ageGroupToOptionDTO(agegr);
		for(AgeGroup ag:AgeGroup.values()) {
			OptionDTO oform = ageGroupToOptionDTO(ag);
			opt.getOptions().add(oform);
		}

		return new FormFieldDTO<OptionDTO>(opt);
	}

	/**
	 * Convert AgeGroup form to OptionDTO
	 * @param df
	 * @return
	 */
	private OptionDTO ageGroupToOptionDTO(AgeGroup agegr) {
		OptionDTO ret = new OptionDTO();
		if(agegr != null) {
			ret.setCode(messages.get(agegr.getKey()));
			ret.setOriginalCode(agegr.getKey());
			ret.setId(agegr.ordinal() + 1);
		}
		return ret;
	}



	/**
	 * Create cards DTO from sorted list of question instances
	 * Supposed, that all the question instances belongs to one state
	 * @param model 
	 * @param loadQuestionInstances
	 * @return
	 */
	public List<QuestionDTO> questionInstancesToDTO(Iterable<QuestionInstancePIP> checkList, Import_permit model, User currentUser) {
		List<QuestionDTO> ret = new ArrayList<QuestionDTO>();
		List<User> experts=expertsFromApplication(model, currentUser);
		for(QuestionInstancePIP qModel : checkList) {
			ret.add(questionInstanceToDto(qModel,experts));
		}
		return ret;
	}

	/**
	 * First (initial) way to create QuestionDTO from QuestionInstancePIP model 
	 * @param qModel
	 * @param experts 
	 * @return
	 */
	public QuestionDTO questionInstanceToDto(QuestionInstancePIP qModel, List<User> experts) {
		QuestionDTO ret = new QuestionDTO();
		ret.setAnswer(answerToDto(qModel.getAnswer(),experts));
		ret.setId(qModel.getId());
		ret.setOrder(qModel.getOrder());
		ret.setQuestionKey(qModel.getQuestion());
		ret.setQuestion(messages.get(qModel.getQuestion()));
		ret.setHead(qModel.getHeader());
		return ret;
	}

	/**
	 * First (initial) way to create QuestionDTO from QuestionTemplatePIP model 
	 * @param qModel
	 * @return
	 */
	public QuestionTemplateDTO questionTemplateToDto(QuestionTemplatePIP qModel) {
		QuestionTemplateDTO ret = new QuestionTemplateDTO();
		ret.setId(qModel.getId());
		ret.getOrder().setValue(qModel.getOrder());;
		ret.setQuestionKey(qModel.getQuestion());
		String str = messages.get(qModel.getQuestion());
		if(str.equals(QuestionTemplateDTO.QUESTION_KEY))
			ret.setQuestion("");
		else
			ret.setQuestion(messages.get(qModel.getQuestion()));

		ResourceMessage rm = boilerServ.findMessage(qModel.getQuestion(), true);
		if(rm != null)
			ret.getQuest().setValue(rm.getValue());
		
		rm = boilerServ.findMessage(qModel.getQuestion(), false);
		if(rm != null)
			ret.getQuestportu().setValue(rm.getValue());

		ret.getHead().setValue(qModel.getHeader());

		return ret;
	}


	/**
	 * Second way to create DTO from question instance
	 * @param question all fields from here
	 * @param questionDTO expert list form here
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public QuestionDTO questionInstanceToDto(QuestionInstancePIP question, QuestionDTO questionDTO) throws ObjectNotFoundException {
		List<User> experts = expertsFromAnswerDto(questionDTO.getAnswer());
		return questionInstanceToDto(question,experts);
	}

	/**
	 * Experts may be restored from a AnswerDTO created before
	 * @param answer
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	private List<User> expertsFromAnswerDto(AnswerDTO answer) throws ObjectNotFoundException {
		List<User> ret = new ArrayList<User>() ;
		if(answer!=null && answer.getExpert()!=null && answer.getExpert().getValue()!=null && answer.getExpert().getValue().getOptions()!=null) {
			List<OptionDTO> expertsOpt = answer.getExpert().getValue().getOptions();
			for(OptionDTO e : expertsOpt) {
				ret.add(boilerServ.userById(e.getId()));
			}
		}
		return ret;
	}

	/**
	 * Experts depends on the state of the application
	 * @param model
	 * @return
	 */
	private List<User> expertsFromApplication(Import_permit model,User currentUser) {
		Iterable<User> users = userRepo.findAll();
		List<User> ret = new ArrayList<User>();
		if(model.getPipStatus().getCode().equalsIgnoreCase("DRFT")) {
			for(User u :users) {
				if(boilerServ.hasRole(u,"secretary")) {
					ret.add(u);
				}
			}
		}else {
			for(User u :users) {
				if (u.getApplicant() != null) {
					if(u.getApplicant().getApplcntId() == model.getApplicant().getApplcntId()) {
						ret.add(u);
					}
				}
				if(boilerServ.hasRole(u,"expert") || boilerServ.hasRole(u,"moderator") || boilerServ.hasRole(u,"secretary") || boilerServ.hasRole(u, "responsible")   ) {
					if(u.getUserId()!=currentUser.getUserId()){
						ret.add(u);
					}
				}
			}
		}
		//TODO the rest of states
		return ret;
	}

	/**
	 * Create or modify Answer to a question model object form the DTO
	 * @param answerDto
	 * @param answer
	 * @param author
	 * @return
	 * @throws ObjectNotFoundException
	 */
	public AnswerQuestion answerDtoToAnswerQuestion(AnswerDTO answerDto, AnswerQuestion answer, User author) throws ObjectNotFoundException{
		if(answer == null) {
			answer = new AnswerQuestion();
		}
		answer.setYes(answerDto.isYes());
		answer.setNo(answerDto.isNo());
		answer.setNotApplicable(answerDto.isNa());
		answer.setAsk(answerDto.isAsk());
		answer.setAnswered(answerDto.isAnswered());
		long expertId = answerDto.getExpert().getValue().getId();
		if(answer.getAsk() && expertId>0) {
			User expert = boilerServ.userById(expertId);
			answer.setExpert(expert);
			answer.setAskedAt(new Date());
			answer.seteQuestion(answerDto.geteQuestion().getValue());
		}else {
			answer.setAskedAt(null);
			answer.setAsk(false);
			answer.seteQuestion("");
			answer.setExpert(null);
		}
		answer.setAuthor(author);
		answer.setNote(answerDto.getNotes().getValue());
		return answer;
	}


	/**
	 * Create AnswerDTO from AnswerQuestion model
	 * @param answer
	 * @param experts 
	 * @return
	 */
	public AnswerDTO answerToDto(AnswerQuestion answer, List<User> experts) {
		AnswerDTO ret = new AnswerDTO();
		if(answer != null) {
			ret.setId(answer.getId());
			ret.setNa(answer.getNotApplicable());
			ret.setNo(answer.getNo());
			ret.setYes(answer.getYes());
			ret.setAnswered(answer.getAnswered());
			ret.setAsk(answer.getAsk());
			ret.setMessage("");
			if(ret.isAsk()) {
				ret.setMessage(waitForAnswerMessage(answer));
				ret.setAnswerHeader(answerHeader(answer));
			}
			if(ret.isAnswered()) {
				ret.setMessage(answeredMessage(answer));
			}
			ret.getExpert().setValue(userToOptionDTO(answer.getExpert(), experts));
			if(answer.getNote() != null) {
				ret.getNotes().setValue(answer.getNote());
			}
			ret.geteQuestion().setValue(answer.geteQuestion());
		}
		return ret;
	}
	/**
	 * To display header in the QuestionAnswer component
	 * @param answer
	 * @return
	 */
	private String answerHeader(AnswerQuestion answer) {
		Date askedAt = answer.getAskedAt();
		List<String> retl=new ArrayList<String>();
		if(askedAt !=null) {
			TableCell cell = TableCell.instanceOf("1", new LocalDate(askedAt), LocaleContextHolder.getLocale());
			retl.add(cell.getValue());
		}
		if(answer.getAuthor()!=null) {
			retl.add(answer.getAuthor().getName());
		}
		if(answer.getExpert()!=null) {
			retl.add("->"+answer.getExpert().getName());
		}
		return String.join(" ", retl);
	}

	/**
	 * Expert has answered, however the checklist 
	 * @param answer
	 * @return
	 */
	private String answeredMessage(AnswerQuestion answer) {
		Date answeredAt = answer.getAnsweredAt();
		List<String> retl=new ArrayList<String>();
		if(answeredAt !=null) {
			TableCell cell = TableCell.instanceOf("1", new LocalDate(answeredAt), LocaleContextHolder.getLocale());
			retl.add(cell.getValue());
		}
		if(answer.getExpert()!=null) {
			retl.add(answer.getExpert().getName());
		}
		return String.join(" ", retl);
	}

	/**
	 * Wait for an answer - ask date and expert name
	 * @param answer
	 * @return
	 */
	private String waitForAnswerMessage(AnswerQuestion answer) {
		Date askedAt = answer.getAskedAt();
		List<String> retl=new ArrayList<String>();
		if(askedAt !=null) {
			TableCell cell = TableCell.instanceOf("1", new LocalDate(askedAt), LocaleContextHolder.getLocale());
			retl.add(cell.getValue());
		}
		if(answer.getExpert()!=null) {
			retl.add(answer.getExpert().getName());
		}
		return String.join(" ", retl);
	}

	/**
	 * Allows to select a user from the list of users
	 * @param user
	 * @param users
	 * @return
	 */
	public OptionDTO userToOptionDTO(User user, List<User> users) {
		OptionDTO ret = new OptionDTO();
		if(users != null) {
			List<OptionDTO> pickList = new ArrayList<OptionDTO>();
			for(User u : users) {
				pickList.add(userToOptionDTO(u,null));
			}
			ret.setOptions(pickList);
		}
		//create a user
		if(user != null) {
			ret.setId(user.getUserId());
			ret.setCode(user.getName());
			if(user.getApplicant() != null) {
				ret.setDescription(user.getApplicant().getAppName());
			}else {
				List<Role> roles = user.getRoles();
				if(roles != null) {
					List<String> rols = new ArrayList<String>();
					for(Role r : roles) {
						rols.add(messages.get(r.getRolename()));
					}
					ret.setDescription(String.join(",", rols));
				}
			}
		}

		return ret;
	}
	/**
	 * Create DTO from an Applicant entity
	 * @param model
	 * @param data
	 * @return
	 */
	@Transactional
	public ApplicantDTO applicantDTOFromModel(Import_permit model, ApplicantDTO data) {
		if(model.getApplicant()!=null) {
			data.getApplicant_addr1().setValue(addrToString(model.getApplicant().getAddress()));
			data.getApplicant_contactname().setValue(applicantResponsibleToString(model.getApplicant()));
			data.getApplicant_country().setValue(countryToString(model.getApplicant().getAddress().getCountry()));
			data.getApplicant_elink().setValue(applicantElinkToString(model.getApplicant()));
			data.getApplicant_name().setValue(model.getApplicant().getAppName());
		}
		return data;
	}

	/**
	 * Applicant's email, fax web, phone
	 * @param applicant
	 * @return
	 */
	public String applicantElinkToString(Applicant applicant) {
		List<String> list = new ArrayList<String>();
		if(applicant.getPhoneNo()!=null && applicant.getPhoneNo().length()>0) {
			list.add(applicant.getPhoneNo());
		}
		if(applicant.getFaxNo()!=null && applicant.getFaxNo().length()>0) {
			list.add(applicant.getFaxNo());
		}
		if(applicant.getEmail()!=null && applicant.getEmail().length()>0) {
			list.add(applicant.getEmail());
		}
		if(applicant.getWebsite()!=null && applicant.getWebsite().length()>0) {
			list.add(applicant.getWebsite());
		}
		return String.join(", ", list);
	}

	/**
	 * Country to string
	 * @param applicant
	 * @return
	 */
	private String countryToString(Country cou) {
		if(cou!=null) {
			return cou.getCountryName();
		}else {
			return "";
		}
	}

	/**
	 * Find applicant responsible and convert all data to a string
	 * @param applicant
	 * @return
	 */
	private String applicantResponsibleToString(Applicant applicant) {
		if(applicant.getUsers()!= null) {
			for(User user: applicant.getUsers()) {
				if(user.getUsername().equalsIgnoreCase(applicant.getContactName())) {
					return userToString(user);
				}
			}
			return "";
		}else {
			return "";
		}
	}
	/**
	 * All user data to string
	 * @param user
	 * @return
	 */
	private String userToString(User user) {
		List<String> list = new ArrayList<String>();
		if(user.getName() != null) {
			list.add(user.getName());
		}
		if(user.getAddress() != null) {
			list.add(addrToString(user.getAddress()));
		}
		if(user.getEmail()!= null) {
			list.add(user.getEmail());
		}
		if(user.getPhoneNo()!= null) {
			list.add(user.getPhoneNo());
		}
		if(user.getFaxNo()!= null) {
			list.add(user.getFaxNo());
		}
		return String.join(", ", list);
	}

	/**
	 * Convert an address to human readable string
	 * @param address
	 * @return
	 */
	public String addrToString(Address address) {
		List<String> ret= new ArrayList<String>();
		if(address.getAddress1() != null && address.getAddress1().length()>0) {
			ret.add(address.getAddress1());
		}
		if(address.getAddress2()!=null && address.getAddress2().length()>0) {
			ret.add(address.getAddress2());
		}
		if(address.getZipcode()!=null && address.getZipcode().length()>0) {
			ret.add(address.getZipcode());
		}
		if(address.getZipaddress()!=null && address.getZipaddress().length()>0) {
			ret.add(address.getZipaddress());
		}
		if(address.getCountry()!=null && address.getCountry().getCountryName().length()>0) {
			ret.add(address.getCountry().getCountryName());
		}
		return String.join(", ", ret);
	}


	/**
	 * User name, phone and eMail as string
	 * @param user
	 * @return
	 */
	public String userData(User user) {
		List<String> retl = new ArrayList<String>();
		retl.add(user.getName());
		if(user.getPhoneNo()!= null && user.getPhoneNo().length()>0) {
			retl.add(user.getPhoneNo());
		}
		if(user.getEmail()!= null && user.getEmail().length()>0) {
			retl.add(user.getEmail());
		}
		return String.join(" ,", retl);
	}
	/**
	 * Create product details from name, active ingredients and strengths
	 * @param product
	 * @return
	 */
	public String productToString(Product product) {
		List<String> names = new ArrayList<String>();
		List<String> actives = new ArrayList<String>();
		names.add(product.getProdName());
		names.add(product.getGenName());
		if(product.getInns()!=null) {
			List<String> active= new ArrayList<String>();
			for(ProdInn prodinn : product.getInns()) {
				active.add(prodinn.getInn().getName());
				if(prodinn.getDosStrength()!= null && prodinn.getDosStrength().length()>0) {
					active.add(prodinn.getDosStrength());
					if(prodinn.getDosUnit()!=null && prodinn.getDosUnit().getUom().length()>0) {
						active.add(prodinn.getDosUnit().getUom());
					}
				}
				actives.add(String.join(" ", active));
			}
		}
		return String.join("(", names)+")("+String.join("+", actives)+")";
	}
	/**
	 * Allows select an active ingredient using Oracle field
	 * @param inn
	 * @return
	 */
	public OptionDTO innToOptionDTO(Inn inn) {
		OptionDTO dto = new OptionDTO();
		if(inn != null) {
			dto.setCode(inn.getName());
			dto.setId(inn.getId());
		}
		return dto;
	}
	/**
	 * Allows select an excipient using ORacle field
	 * @param exc
	 * @return
	 */
	public OptionDTO excToOptionDTO(Excipient exc) {
		OptionDTO dto = new OptionDTO();
		if(exc != null) {
			dto.setCode(exc.getName());
			dto.setId(exc.getId());
		}
		return dto;
	}
	/**
	 * Allows select a Manufacturer using Oracle field
	 * @param c
	 * @return
	 */
	public OptionDTO companyToOptionDTO(Company c) {
		OptionDTO dto = new OptionDTO();
		if(c != null) {
			dto.setCode(c.getCompanyName());
			dto.setId(c.getId());
		}
		return dto;
	}
	/**
	 * Selection of a document type on Admin tab
	 * @param dt
	 * @return
	 */
	public DocTypesDTO docTypeToDTO(DocType dt) {
		DocTypesDTO dto = new DocTypesDTO();
		if(dt != null) {
			dto.setId(dt.getId());
			dto.getSra_code().setValue(dt.getCode());
			ResourceMessage rm = boilerServ.findMessage(dt.getDescription(), true);
			if(rm != null)
				dto.getName_us().setValue(rm.getValue());
			
			rm = boilerServ.findMessage(dt.getDescription(), false);
			if(rm != null)
				dto.getName_port().setValue(rm.getValue());
			
			dto.setActive(booleanToYesNo(dt.getActive()));
			dto.setProcess(booleanToYesNo(dt.getAttachToPIP()));
			dto.setApplicant(booleanToYesNo(dt.getAttachToApplicant()));
		}else {// create new
			dto.getSra_code().setValue("");
			dto.setActive(booleanToYesNo(true));
			dto.setProcess(booleanToYesNo(false));
			dto.setApplicant(booleanToYesNo(false));
		}

		return dto;
	}
	/**
	 * Convert Yes/No to boolean
	 * @param dto
	 * @return
	 */
	public boolean getYesNoValue(OptionDTO dto) {
		if(dto.getId() == (YesNoType.YES.ordinal() + 1))
			return true;
		
		return false;
	}
	/**
	 * Convert Import Permit Detail to DTO
	 * @param data
	 * @param model
	 * @return
	 */
	@Transactional
	public ApplicationDetailDTO ipDetailToDTO(ApplicationDetailDTO data, Import_permit_detail model) {
		data.setPacks(FormFieldDTO.of(model.getUnits(),2));
		data.setPrice(FormFieldDTO.of(model.getPrice(),2));
		BigDecimal amount = data.getPacks().getValue().multiply(data.getPrice().getValue());
		data.setAmount(FormFieldDTO.of(amount, 2));
		data.setProduct(productToDto(model.getProduct(), data.getProduct()));
		data.setId(model.getId());
		return data;
	}
}
