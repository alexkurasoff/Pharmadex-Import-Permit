package org.msh.pdex.ipermit.dto;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.msh.pdex.dto.AtcDTO;
import org.msh.pdex.dto.ManufacturerDTO;
import org.msh.pdex.dto.ProdExcipientDTO;
import org.msh.pdex.dto.ProdInnDTO;
import org.msh.pdex.dto.Validator;
import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.services.Magic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Display product data
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ProductDTO  extends  AllowValidation{
	
	long id = 0l;
	private LocalDateTime lastSaved=LocalDateTime.now();
	
	@Validator(above=2,below=5)
	private FormFieldDTO<OptionDTO> narc = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=2,below=5)
	private FormFieldDTO<OptionDTO> prescr = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=2,below=5)
	private FormFieldDTO<OptionDTO> hospital = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=2,below=5)
	private FormFieldDTO<OptionDTO> otc = new FormFieldDTO<OptionDTO>(new OptionDTO());
	
	@Validator(above=3,below=500)
	@Magic(name="prodName")
	private FormFieldDTO<String> prod_name = new FormFieldDTO<String>("");
	@Validator(above=3,below=500)
	@Magic(name="genName")
	private FormFieldDTO<String> prodgenname = new FormFieldDTO<String>("");
	
	private List<AtcDTO> atc = new ArrayList<AtcDTO>();
	@Validator(above=3,below=254)
	@Magic(name="dosForm")
	private FormFieldDTO<OptionDTO> dos_form = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=1,below=254)
	@Magic(name="dosUnit")
	private FormFieldDTO<OptionDTO> dos_unit = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=1,below=254)
	@Magic(name="dosStrength")
	private FormFieldDTO<String> dos_strength = new FormFieldDTO<String>("");
	
	@Validator(above=1,below=254)
	@Magic(name="adminRoute")
	private FormFieldDTO<OptionDTO> admin_route = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=1,below=254)
	@Magic(name="prodCategory")
	private FormFieldDTO<OptionDTO> prod_cat = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=1,below=254)
	@Magic(name="ageGroup")
	private FormFieldDTO<OptionDTO> age_group = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=4096)
	@Magic(name="prodDesc")
	private FormFieldDTO<String> prod_desc = new FormFieldDTO<String>("");

	private List<ProdInnDTO> inns = new ArrayList<ProdInnDTO>();
	private List<ProdExcipientDTO> excipients = new ArrayList<ProdExcipientDTO>();
	private ManufacturerDTO finProdManuf = new ManufacturerDTO();
	private List<ManufacturerDTO> manufacturers = new ArrayList<ManufacturerDTO>();
	
	@Validator(above=1,below=500)
	@Magic(name="shelfLife")
	private FormFieldDTO<String> product_shelflife = new FormFieldDTO<String>("");
	@Validator(above=0,below=500)
	@Magic(name="storageCndtn")
	private FormFieldDTO<String> product_storcndtn = new FormFieldDTO<String>("");
	@Validator(above=1,below=500)
	@Magic(name="packSize")
	private FormFieldDTO<String> product_packsize = new FormFieldDTO<String>("");
	@Validator(above=0,below=500)
	@Magic(name="contType")
	private FormFieldDTO<String> product_conttype = new FormFieldDTO<String>("");
	@Validator(above=3,below=4096)
	@Magic(name="indications")
	private FormFieldDTO<String> prod_indications = new FormFieldDTO<String>("");
	@Validator(above=3,below=4096)
	@Magic(name="posology")
	private FormFieldDTO<String> prod_posology = new FormFieldDTO<String>("");
	@Validator(above=3,below=4096)
	@Magic(name="medical_product")
	private FormFieldDTO<String> medical_product = new FormFieldDTO<String>("");
	@Magic(name="fnm")
	private FormFieldDTO<String> prod_fnm = new FormFieldDTO<String>("");
	
	/**
	 * Registration data
	 */
	private FormFieldDTO<String> license_holder = new FormFieldDTO<String>("");
	private FormFieldDTO<String> address = new FormFieldDTO<String>("");
	private FormFieldDTO<LocalDate> registration_date = new FormFieldDTO<LocalDate>(LocalDate.now());
	private FormFieldDTO<LocalDate> valid_to = new FormFieldDTO<LocalDate>(LocalDate.now());
	private FormFieldDTO<String> reg_number = new FormFieldDTO<String>("");
	

	private String validError = "";
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	
	public FormFieldDTO<OptionDTO> getNarc() {
		return narc;
	}
	public void setNarc(FormFieldDTO<OptionDTO> narc) {
		this.narc = narc;
	}
	public FormFieldDTO<OptionDTO> getPrescr() {
		return prescr;
	}
	public void setPrescr(FormFieldDTO<OptionDTO> prescr) {
		this.prescr = prescr;
	}
	public FormFieldDTO<OptionDTO> getHospital() {
		return hospital;
	}
	public void setHospital(FormFieldDTO<OptionDTO> hospital) {
		this.hospital = hospital;
	}
	public FormFieldDTO<OptionDTO> getOtc() {
		return otc;
	}
	public void setOtc(FormFieldDTO<OptionDTO> otc) {
		this.otc = otc;
	}
	public FormFieldDTO<String> getProd_name() {
		return prod_name;
	}
	public void setProd_name(FormFieldDTO<String> prod_name) {
		this.prod_name = prod_name;
	}

	public FormFieldDTO<String> getProdgenname() {
		return prodgenname;
	}
	public void setProdgenname(FormFieldDTO<String> prodgenname) {
		this.prodgenname = prodgenname;
	}
	public List<AtcDTO> getAtc() {
		return atc;
	}
	public void setAtc(List<AtcDTO> atc) {
		this.atc = atc;
	}
	public FormFieldDTO<OptionDTO> getDos_form() {
		return dos_form;
	}
	public void setDos_form(FormFieldDTO<OptionDTO> dos_form) {
		this.dos_form = dos_form;
	}
	public FormFieldDTO<OptionDTO> getDos_unit() {
		return dos_unit;
	}
	public void setDos_unit(FormFieldDTO<OptionDTO> dos_unit) {
		this.dos_unit = dos_unit;
	}

	public FormFieldDTO<OptionDTO> getAdmin_route() {
		return admin_route;
	}
	public void setAdmin_route(FormFieldDTO<OptionDTO> admin_route) {
		this.admin_route = admin_route;
	}
	
	public FormFieldDTO<OptionDTO> getProd_cat() {
		return prod_cat;
	}
	public void setProd_cat(FormFieldDTO<OptionDTO> prod_cat) {
		this.prod_cat = prod_cat;
	}
	public FormFieldDTO<OptionDTO> getAge_group() {
		return age_group;
	}
	public void setAge_group(FormFieldDTO<OptionDTO> age_group) {
		this.age_group = age_group;
	}
	public FormFieldDTO<String> getProd_desc() {
		return prod_desc;
	}
	public void setProd_desc(FormFieldDTO<String> prod_desc) {
		this.prod_desc = prod_desc;
	}

	public List<ProdInnDTO> getInns() {
		return inns;
	}
	public void setInns(List<ProdInnDTO> inns) {
		this.inns = inns;
	}
	public List<ProdExcipientDTO> getExcipients() {
		return excipients;
	}
	public void setExcipients(List<ProdExcipientDTO> excipients) {
		this.excipients = excipients;
	}

	public ManufacturerDTO getFinProdManuf() {
		return finProdManuf;
	}
	public void setFinProdManuf(ManufacturerDTO finProdManuf) {
		this.finProdManuf = finProdManuf;
	}
	public List<ManufacturerDTO> getManufacturers() {
		return manufacturers;
	}
	public void setManufacturers(List<ManufacturerDTO> manufacturers) {
		this.manufacturers = manufacturers;
	}
	public FormFieldDTO<String> getProduct_shelflife() {
		return product_shelflife;
	}
	public void setProduct_shelflife(FormFieldDTO<String> product_shelflife) {
		this.product_shelflife = product_shelflife;
	}
	public FormFieldDTO<String> getProduct_storcndtn() {
		return product_storcndtn;
	}
	public void setProduct_storcndtn(FormFieldDTO<String> product_storcndtn) {
		this.product_storcndtn = product_storcndtn;
	}

	public FormFieldDTO<String> getProduct_packsize() {
		return product_packsize;
	}
	public void setProduct_packsize(FormFieldDTO<String> product_packsize) {
		this.product_packsize = product_packsize;
	}
	public FormFieldDTO<String> getProduct_conttype() {
		return product_conttype;
	}
	public void setProduct_conttype(FormFieldDTO<String> product_conttype) {
		this.product_conttype = product_conttype;
	}
	public FormFieldDTO<String> getProd_indications() {
		return prod_indications;
	}
	public void setProd_indications(FormFieldDTO<String> prod_indications) {
		this.prod_indications = prod_indications;
	}
	public FormFieldDTO<String> getProd_posology() {
		return prod_posology;
	}
	public void setProd_posology(FormFieldDTO<String> prod_posology) {
		this.prod_posology = prod_posology;
	}

	public FormFieldDTO<String> getLicense_holder() {
		return license_holder;
	}
	public void setLicense_holder(FormFieldDTO<String> license_holder) {
		this.license_holder = license_holder;
	}
	public FormFieldDTO<LocalDate> getRegistration_date() {
		return registration_date;
	}
	public void setRegistration_date(FormFieldDTO<LocalDate> registration_date) {
		this.registration_date = registration_date;
	}
	public FormFieldDTO<String> getAddress() {
		return address;
	}
	public void setAddress(FormFieldDTO<String> address) {
		this.address = address;
	}
	public FormFieldDTO<LocalDate> getValid_to() {
		return valid_to;
	}
	public void setValid_to(FormFieldDTO<LocalDate> valid_to) {
		this.valid_to = valid_to;
	}

	public FormFieldDTO<String> getProd_fnm() {
		return prod_fnm;
	}
	public void setProd_fnm(FormFieldDTO<String> prod_fnm) {
		this.prod_fnm = prod_fnm;
	}
	public FormFieldDTO<String> getReg_number() {
		return reg_number;
	}
	public void setReg_number(FormFieldDTO<String> reg_number) {
		this.reg_number = reg_number;
	}
	public LocalDateTime getLastSaved() {
		return lastSaved;
	}
	public void setLastSaved(LocalDateTime lastSaved) {
		this.lastSaved = lastSaved;
	}
	public FormFieldDTO<String> getDos_strength() {
		return dos_strength;
	}
	public void setDos_strength(FormFieldDTO<String> dos_strength) {
		this.dos_strength = dos_strength;
	}
	public String getValidError() {
		return validError;
	}
	public void setValidError(String validError) {
		this.validError = validError;
	}
	public FormFieldDTO<String> getMedical_product() {
		return medical_product;
	}
	public void setMedical_product(FormFieldDTO<String> medical_product) {
		this.medical_product = medical_product;
	}

}
