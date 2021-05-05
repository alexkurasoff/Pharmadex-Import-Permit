package org.msh.pdex.ipermit.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.msh.pdex.dto.AtcDTO;
import org.msh.pdex.dto.AttachmentDTO;
import org.msh.pdex.dto.DocTypesDTO;
import org.msh.pdex.dto.ManufacturerDTO;
import org.msh.pdex.dto.ProdExcipientDTO;
import org.msh.pdex.dto.ProdInnDTO;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.dto.ApplicationDetailDTO;
import org.msh.pdex.ipermit.dto.ProductDTO;
import org.msh.pdex.model.Address;
import org.msh.pdex.model.Atc;
import org.msh.pdex.model.Company;
import org.msh.pdex.model.Country;
import org.msh.pdex.model.DosUom;
import org.msh.pdex.model.Excipient;
import org.msh.pdex.model.Inn;
import org.msh.pdex.model.ProdCompany;
import org.msh.pdex.model.ProdExcipient;
import org.msh.pdex.model.ProdInn;
import org.msh.pdex.model.Product;
import org.msh.pdex.model.User;
import org.msh.pdex.model.enums.CompanyType;
import org.msh.pdex.model.enums.UseCategory;
import org.msh.pdex.model.enums.YesNoType;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.Import_permit_detail;
import org.msh.pdex.model.rsecond.DocType;
import org.msh.pdex.model.rsecond.Document;
import org.msh.pdex.repository.AtcRepo;
import org.msh.pdex.repository.CompanyRepo;
import org.msh.pdex.repository.DosageUnitRepo;
import org.msh.pdex.repository.ExcipientRepository;
import org.msh.pdex.repository.InnRepository;
import org.msh.pdex.repository.ProdCompanyRepo;
import org.msh.pdex.repository.ProdExcipientRepository;
import org.msh.pdex.repository.ProdInnRepository;
import org.msh.pdex.services.DictionaryService;
import org.msh.pdex.services.MagicEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Convert DTOs to Entities using the Magic 
 * @author alexk
 *
 */
@Service
public class DtoToEntityService {

	@Autowired
	MagicEntityService magicService;
	@Autowired
	DictionaryService dictionaryService;
	@Autowired
	AtcRepo atcRepo;
	@Autowired
	ProdInnRepository prodInnRepo;
	@Autowired
	DosageUnitRepo dosageUnitRepo;
	@Autowired
	InnRepository innRepo;
	@Autowired
	ProdExcipientRepository prodExcipientRepo;
	@Autowired
	ExcipientRepository excipientRepo;
	@Autowired
	ProdCompanyRepo prodCompanyRepo;
	@Autowired
	CompanyRepo companyRepo;
	@Autowired
	BoilerplateServices boilerServ;

	/**
	 * Fill application model from application DTO
	 * @param appModel
	 * @param appDTO
	 * @return
	 */
	public Import_permit applicationFromDTO(Import_permit appModel, ApplicationDTO appDTO) {
		try {
			magicService.magic(appDTO, appModel);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		return appModel;
	}

	public Product productFromDTO(Product prod, ProductDTO prodDTO) {
		Object[] objects = new Object[1];
		objects[0] = (Object)prod;

		try {
			magicService.magic(prodDTO, objects);
			prod = (Product)objects[0];

			prod = buildUseCategories(prod, prodDTO);
			prod = builATC(prod, prodDTO);
			prod = builProdInnList(prod, prodDTO);
			prod = builProdExcipientList(prod, prodDTO);
			prod = builProdCompanyList(prod, prodDTO);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		return prod;
	}

	private Product buildUseCategories(Product prod, ProductDTO prodDTO) {
		List<UseCategory> cats = new ArrayList<UseCategory>();
		if(prodDTO.getNarc().getValue().getId() == (YesNoType.YES.ordinal() + 1))
			cats.add(UseCategory.SCH_NARCOTIC);
		if(prodDTO.getPrescr().getValue().getId() == (YesNoType.YES.ordinal() + 1))
			cats.add(UseCategory.PRESCRIPTION);
		if(prodDTO.getHospital().getValue().getId() == (YesNoType.YES.ordinal() + 1))
			cats.add(UseCategory.HOSPITAL);
		if(prodDTO.getOtc().getValue().getId() == (YesNoType.YES.ordinal() + 1))
			cats.add(UseCategory.OTC);

		prod.setUseCategories(cats);
		return prod;
	}
	/**
	 * Add single ATC code to a product. List of ATC codes doesn't work yet
	 * @param prod
	 * @param prodDTO
	 * @return
	 */
	private Product builATC(Product prod, ProductDTO prodDTO) {
		if(prodDTO.getAtc().size()>1) {
			if(prod.getAtcs() == null) {
				prod.setAtcs(new ArrayList<Atc>()); 
			}
			prod.getAtcs().clear();
			List<Atc> codes = new ArrayList<Atc>();
			Atc atc = atcDTOtoAtc(prodDTO.getAtc().get(0));
			codes.add(atc);
			prod.getAtcs().addAll(codes);
		}else {
			prod.setAtcs(new ArrayList<Atc>());
		}
		return prod;
	}
	/**
	 * AtcDTO to ATc entity, not magic
	 * @param dto
	 * @return
	 */
	public Atc atcDTOtoAtc(AtcDTO dto) {
		Atc atc = new Atc();
		if(dto.getAtc_5().getValue().getId() > 0) {
			atc = atcRepo.findByAtcCodeLike(dto.getAtc_5().getValue().getCode());
		}else if(dto.getAtc_4().getValue().getId() > 0) {
			atc = atcRepo.findByAtcCodeLike(dto.getAtc_4().getValue().getCode());
		}else if(dto.getAtc_3().getValue().getId() > 0) {
			atc = atcRepo.findByAtcCodeLike(dto.getAtc_3().getValue().getCode());
		}else if(dto.getAtc_2().getValue().getId() > 0) {
			atc = atcRepo.findByAtcCodeLike(dto.getAtc_2().getValue().getCode());
		}else if(dto.getAtc_1().getValue().getId() > 0) {
			atc = atcRepo.findByAtcCodeLike(dto.getAtc_1().getValue().getCode());
		}
		return atc;
	}

	private Product builProdInnList(Product prod, ProductDTO prodDTO) {
		if(prod.getInns() == null) {
			prod.setInns(new ArrayList<ProdInn>());
		}
		prod.getInns().clear();
		List<ProdInn> prodInns = new ArrayList<ProdInn>();
		for(ProdInnDTO dto:prodDTO.getInns()) {
			ProdInn pinn = prodInnDTOtoProdInn(dto);
			if(pinn != null) {
				pinn.setProduct(prod);
				prodInns.add(pinn);
			}
		}
		prod.getInns().addAll(prodInns);
		return prod;
	}
	/**
	 * ProdInn entity from ProdInnDTO
	 * @param dto
	 * @return
	 */
	public ProdInn prodInnDTOtoProdInn(ProdInnDTO dto) {
		ProdInn pi = new ProdInn();
		if(dto.getId() > 0) {
			Optional<ProdInn> opt = prodInnRepo.findById(dto.getId());
			if(opt.isPresent()) 
				pi = opt.get();
		}
		pi.setDosStrength(dto.getDos_strength().getValue());

		if(dto.getDos_unit().getValue().getIntId() > 0) {
			DosUom du = dosageUnitRepo.findById(dto.getDos_unit().getValue().getIntId()).get();
			pi.setDosUnit(du);
		}

		if(dto.getProduct_innname().getValue().getId() > 0) {
			Inn inn = innRepo.findById(dto.getProduct_innname().getValue().getId()).get();
			pi.setInn(inn);
		}else
			return null;

		return pi;
	}

	/**
	 * Create Excipients list in the product
	 * @param prod
	 * @param prodDTO
	 * @return
	 */
	private Product builProdExcipientList(Product prod, ProductDTO prodDTO) {
		if(prod.getExcipients() == null) {
			prod.setExcipients(new ArrayList<ProdExcipient>());
		}
		prod.getExcipients().clear();
		List<ProdExcipient> prodExcs = new ArrayList<ProdExcipient>();
		for(ProdExcipientDTO dto:prodDTO.getExcipients()) {
			ProdExcipient pexc = prodExcipientDTOtoProdExcipient(dto);
			if(pexc != null) {
				pexc.setProduct(prod);
				prodExcs.add(pexc);
			}
		}
		prod.getExcipients().addAll(prodExcs);
		return prod;
	}
	/**
	 * Excipient DTO to Excipient entity
	 * @param dto
	 * @return
	 */
	public ProdExcipient prodExcipientDTOtoProdExcipient(ProdExcipientDTO dto) {
		ProdExcipient pe = new ProdExcipient();
		if(dto.getId() > 0) {
			Optional<ProdExcipient> opt = prodExcipientRepo.findById(dto.getId());
			if(opt.isPresent()) 
				pe = opt.get();
		}
		pe.setDosStrength(dto.getDos_strength().getValue());

		if(dto.getDos_unit().getValue().getIntId() > 0) {
			DosUom du = dosageUnitRepo.findById(dto.getDos_unit().getValue().getIntId()).get();
			pe.setDosUnit(du);
		}

		if(dto.getProduct_active().getValue().getId() > 0) {
			Excipient exc = excipientRepo.findById(dto.getProduct_active().getValue().getId()).get();
			pe.setExcipient(exc);
		}else
			return null;

		return pe;
	}
	/**
	 * ProdCompany from DTO
	 * @param dto
	 * @return
	 */
	public ProdCompany prodCompanyDTOtoProdCompany(ManufacturerDTO dto) {
		ProdCompany pc = new ProdCompany();
		if(dto.getId() > 0) {
			Optional<ProdCompany> opt = prodCompanyRepo.findById(dto.getId());
			if(opt.isPresent()) 
				pc = opt.get();
		}
		if(dto.getManuf_name().getValue().getId() > 0) {
			Company c = companyRepo.findById(dto.getManuf_name().getValue().getId()).get();
			pc.setCompany(c);
		}else
			return null;

		CompanyType type = null;
		int typeId = dto.getCompany_type().getValue().getIntId() - 1;
		if(typeId >= 0)
			type = CompanyType.findByOrdinal(typeId);
		pc.setCompanyType(type);

		return pc;
	}

	/**
	 * Create a list of manufacturers (prodCompany)
	 * @param prod
	 * @param prodDTO
	 * @return
	 */
	private Product builProdCompanyList(Product prod, ProductDTO prodDTO) {
		if(prod.getProdCompanies() == null) {
			prod.setProdCompanies(new ArrayList<ProdCompany>());
		}
		prod.getProdCompanies().clear();
		List<ProdCompany> prodComps = new ArrayList<ProdCompany>();
		for(ManufacturerDTO dto:prodDTO.getManufacturers()) {
			ProdCompany manuf = prodCompanyDTOtoProdCompany(dto);
			if(manuf != null) {
				manuf.setProduct(prod);
				prodComps.add(manuf);
			}
		}

		// FinishProdManuf
		String finProdName = "";
		ManufacturerDTO finManuf = prodDTO.getFinProdManuf();
		if(finManuf != null) {
			if(finManuf.getManuf_name().getValue().getId() > 0) {
				ProdCompany manuf = prodCompanyDTOtoProdCompany(finManuf);
				if(manuf != null) {
					manuf.setProduct(prod);
					prodComps.add(manuf);
				}
				finProdName = finManuf.getManuf_name().getValue().getCode();
			}else if(finManuf.getManuf_name().getValue().getId() == -1) {
				Company c = new Company();
				c.setCompanyName(finManuf.getOthermanufname().getValue());

				Country cntr = dictionaryService.loadCountry(finManuf.getApplicant_country().getValue().getId());
				c.setAddress(new Address());
				c.getAddress().setCountry(cntr);

				ProdCompany pc = new ProdCompany();
				pc.setCompany(c);
				pc.setProduct(prod);
				pc.setCompanyType(CompanyType.FIN_PROD_MANUF);

				prodComps.add(pc);

				finProdName = finManuf.getOthermanufname().getValue();
			}
		}

		prod.getProdCompanies().addAll(prodComps);
		prod.setManufName(finProdName);
		return prod;
	}
	/**
	 * DocType entity from DTO
	 * @param dtype
	 * @param docsDTO
	 * @return
	 */
	public DocType docTypeFromDTO(DocType dtype, DocTypesDTO docsDTO) {
		Object[] objects = new Object[1];
		objects[0] = (Object)dtype;

		try {
			magicService.magic(docsDTO, objects);
			dtype = (DocType)objects[0];

		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		return dtype;
	}

	/**
	 * Set common doc fields before save it
	 * @param user 
	 * @param fileBytes
	 * @param doc
	 * @throws ObjectNotFoundException 
	 */
	public Document commonDocFields(AttachmentDTO att, User user, byte[] fileBytes, Document doc) throws ObjectNotFoundException {
		DocType docType = dictionaryService.loadDocType(att.getDocType().getValue());
		doc.setDocType(docType);
		doc.setFileName(att.getFileName().getValue());
		doc.setFileSize(att.getFileSize().getValue());
		doc.setContentEncoding(att.getFileContentType().getValue());
		if(fileBytes.length>0) {
			doc.setAttachment(fileBytes);
		}
		doc.setAnnotation(att.getAnnotation().getValue());
		doc.setSignDate(boilerServ.toDate(att.getSignDate().getValue()));
		doc.setSignNo(att.getSignNo().getValue());
		doc.setSingPerson(att.getSignPerson().getValue());
		doc.setAuthor(user);
		doc.setModified(new Date());
		return doc;
	}

	/**
	 * Change "marking" data in the model
	 * @param model
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public Import_permit modelFromMarking(Import_permit model, ApplicationDTO data) {
		model.setValidation_date(boilerServ.toDate(data.getValidation_date().getValue()));
		model.setAuth_date(boilerServ.toDate(data.getAuth_date().getValue()));
		model.setExpiry_date(boilerServ.toDate(data.getExpiry_date().getValue()));
		model.setCustom(dictionaryService.loadCustom(data.getCustom().getValue()));
		return model;
	}

	/**
	 * Change "finalize" data in the model
	 * @param model
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public Import_permit modelFromFinalize(Import_permit model, ApplicationDTO data) {
		model.setApprovalDate(boilerServ.toDate(data.getApprovalDate().getValue()));
		model.setExpiry_date(boilerServ.toDate(data.getExpiry_date().getValue()));
		model.setApprover(dictionaryService.loadUser(data.getApprover().getValue()));
		return model;
	}
	/**
	 * Import_permit_detail from the DTO
	 * Assumed that the product is OK the database
	 * @param data
	 * @param import_permit_detail
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public Import_permit_detail detailFromDTO(ApplicationDetailDTO data, Import_permit_detail ipd) throws ObjectNotFoundException {
		magicService.magic(data, ipd);
		return ipd;
	}

}
