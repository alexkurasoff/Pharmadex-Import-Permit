package org.msh.pdex.ipermit.dto;

import java.math.BigDecimal;

import org.msh.pdex.dto.Validator;
import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.services.Magic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This class represents Import_permit_detail - a brige between Import_permit and Product
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ApplicationDetailDTO  extends  AllowValidation {
	long id=0l;					//Import_permit_detail ID
	long ipId=0l;					//Import_permit ID
	ProductDTO product=new ProductDTO();
	@Magic(name="units")
	@Validator(above=1, below=100000000)
	private FormFieldDTO<BigDecimal> packs = FormFieldDTO.of(BigDecimal.ZERO,2);
	@Magic(name="price")
	@Validator(above=1, below=100000)
	private FormFieldDTO<BigDecimal> price = FormFieldDTO.of(BigDecimal.ZERO,2);
	private FormFieldDTO<BigDecimal> amount = FormFieldDTO.of(BigDecimal.ZERO,2);

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public long getIpId() {
		return ipId;
	}
	public void setIpId(long ipId) {
		this.ipId = ipId;
	}

	public ProductDTO getProduct() {
		return product;
	}
	public void setProduct(ProductDTO product) {
		this.product = product;
	}
	public FormFieldDTO<BigDecimal> getPacks() {
		return packs;
	}
	public void setPacks(FormFieldDTO<BigDecimal> packs) {
		this.packs = packs;
	}
	public FormFieldDTO<BigDecimal> getPrice() {
		return price;
	}
	public void setPrice(FormFieldDTO<BigDecimal> price) {
		this.price = price;
	}
	public FormFieldDTO<BigDecimal> getAmount() {
		return amount;
	}
	public void setAmount(FormFieldDTO<BigDecimal> amount) {
		this.amount = amount;
	}
	
}
