package org.msh.pdex.ipermit.services;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.joda.time.LocalDate;
import org.msh.pdex.dto.Validator;
import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.Import_permit_detail;
import org.msh.pdex.model.pip.QuestionInstancePIP;
import org.msh.pdex.model.rsecond.AnswerQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * All validators are here. Low coupling
 * @author alexk
 *
 */
@Service
public class ValidationService {

	private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

	@Autowired
	Messages messages;
	@Autowired
	BoilerplateServices boilerServ;
	
	/**
	 * Deep Validate DTO and propagate result
	 * @param dto
	 * @param screenForm
	 * @param strict
	 * @return
	 * @throws ObjectNotFoundException
	 */
	public <T extends org.msh.pdex.dto.form.AllowValidation> T validateDTO(T dto, boolean screenForm, boolean strict) throws ObjectNotFoundException {
		dto = this.validateAllDTO(dto, screenForm, strict);
		dto.propagateValidation();
		return dto;
	}
	
	/**
	 * Recursive Validate DT0 with annotated by ValidateFormField FormFieldDTOs 
	 * @param <T>
	 * @param dto - any object
	 * @param strict - is this check final or preliminary only?
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@SuppressWarnings("rawtypes")
	private <T extends org.msh.pdex.dto.form.AllowValidation> T validateAllDTO(T dto, boolean screenForm, boolean strict) throws ObjectNotFoundException {
		//create validation rules
		dto.setValid(true);
		List<Field> all = FieldUtils.getAllFieldsList(dto.getClass());
		//paint all to green and validate all included
		for(Field fld : all) {
			try {
				Object obj = PropertyUtils.getProperty(dto, fld.getName());
				if(obj instanceof FormFieldDTO) {
					FormFieldDTO formFieldDTO = (FormFieldDTO) obj;
					formFieldDTO.setStrict(strict);
				}
				if(obj instanceof AllowValidation) {
					AllowValidation aobj = (AllowValidation)obj;
					validateAllDTO(aobj, screenForm, strict);
				}
				if(obj instanceof Iterable) {
					try {
						Iterable<T> iterable = (Iterable<T>)obj;
						for(T it:iterable) {
							validateAllDTO(it, screenForm, strict);
						}
					} catch (java.lang.ClassCastException e) {
						//nothing to do
					}
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new ObjectNotFoundException(e,logger);
			}
		}
		//validate all fields with the annotation
		List<Field> toValidate = FieldUtils.getFieldsListWithAnnotation(dto.getClass(), Validator.class);
		for(Field fld : toValidate) {
			Validator criteria = fld.getAnnotationsByType(Validator.class)[0];
			try {
				Object obj = PropertyUtils.getProperty(dto, fld.getName());
				if(obj instanceof FormFieldDTO) {
					FormFieldDTO formFieldDTO = (FormFieldDTO) obj;
					if((screenForm && !formFieldDTO.isJustloaded())|| !screenForm) {
						formFieldDTO = validateFormField(formFieldDTO,criteria, fld.getName());
						formFieldDTO.setStrict(strict);
						dto.setValid(dto.isValid() && (!formFieldDTO.isError()));
					}else {
						formFieldDTO.clearValidation();
					}
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new ObjectNotFoundException(e,logger);
			}
		}
		
		return dto;
	}
	/**
	 * Validate a field
	 * @param dt
	 * @param criteria
	 * @param name
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private FormFieldDTO validateFormField(FormFieldDTO formFieldDTO, Validator criteria, String name) {
		Object value = formFieldDTO.getValue();
		formFieldDTO.setSuggest("-");
		if(value instanceof OptionDTO) {
			formFieldDTO.setError(!validateString(((OptionDTO) value).getCode(), criteria));
			formFieldDTO.setSuggest(stringSuggest(criteria));
		}
		if(value instanceof String) {
			formFieldDTO.setError(!validateString((String) value, criteria));
			formFieldDTO.setSuggest(stringSuggest(criteria));
		}
		if(value instanceof LocalDate) {
			formFieldDTO.setError(!validateDate((LocalDate) value, criteria));
			formFieldDTO.setSuggest(dateSuggest(criteria));
		}
		if(value instanceof BigDecimal) {
			formFieldDTO.setError(!validateBigDecimal((BigDecimal)value, criteria));
			formFieldDTO.setSuggest(numericSuggest(criteria));
		}
		if(value instanceof Long) {
			BigDecimal bdVal = BigDecimal.valueOf((Long)value);
			formFieldDTO.setError(!validateBigDecimal(bdVal, criteria));
			formFieldDTO.setSuggest(numericSuggest(criteria));
		}
		if(value instanceof Integer) {
			BigDecimal bdVal = BigDecimal.valueOf((Integer)value);
			formFieldDTO.setError(!validateBigDecimal(bdVal, criteria));
			formFieldDTO.setSuggest(numericSuggest(criteria));
		}

		if(formFieldDTO.getSuggest().equals("-")) {
			String msg1=messages.get("valueisempty");
			if(value != null) {
				msg1=value.getClass().getSimpleName();
				logger.error("Validation service for "+ name +" " +messages.get("validationdataunrecognized")+": "+ msg1);
			}
			String msg = messages.get("validationdataunrecognized")+": "+ msg1;
			formFieldDTO.setError(true);
			formFieldDTO.setSuggest(msg);

		}
		return formFieldDTO;
	}

	/**
	 * Numeric suggest
	 * @param criteria
	 * @return
	 */
	private String numericSuggest(Validator criteria) {
		String ret="";
		if(criteria.above()>Integer.MIN_VALUE) {
			ret = messages.get("minnumber") + ": " + criteria.above()+". ";
		}
		if(criteria.below()<Integer.MAX_VALUE) {
			ret = ret+ messages.get("maxnumber") + ": " + criteria.below();
		}
		return ret;
	}
	/***
	 * Suggest for date
	 * @param criteria
	 * @return
	 */
	private String dateSuggest(Validator criteria) {
		String ret="";
		if(criteria.above()>Integer.MIN_VALUE) {
			LocalDate from = LocalDate.now().minusDays(criteria.above());
			ret=messages.get("fromdate") + ": " + from.toString("MMM-dd, yyyy", LocaleContextHolder.getLocale()) +". ";
		}
		if(criteria.below()<Integer.MAX_VALUE) {
			LocalDate to = LocalDate.now().plusDays(criteria.below());
			ret=ret+ messages.get("todate") + ": " + to.toString("MMM-dd, yyyy", LocaleContextHolder.getLocale()) + ".";
		}
		return ret;
	}
	/**
	 * Create a suggestion on the current language
	 * @param criteria
	 * @return
	 */
	private String stringSuggest(Validator criteria) {
		String format = "-";
		if(criteria.above()>0) {
			format = String.format(messages.get("atleastchars"),criteria.above());
		}
		if(criteria.below()!=Integer.MAX_VALUE) {
			format= format+String.format(messages.get("maxchars"),criteria.below());
		}
		return format;
	}
	/**
	 * Validate BigDecimal, really any numeric
	 * @param value
	 * @param validateFormField
	 * @return
	 */
	private boolean validateBigDecimal(BigDecimal value, Validator validateFormField) {
		if(value != null ) {
			BigDecimal min = BigDecimal.valueOf(validateFormField.above());
			BigDecimal max = BigDecimal.valueOf(validateFormField.below());
			return value.compareTo(min)>=0 && value.compareTo(max)<=0;
		}else {
			return false;
		}
	}
	/**
	 * Validate data
	 * @param value
	 * @param validateFormField
	 * @return
	 */
	private boolean validateDate(LocalDate value, Validator validateFormField) {
		if(value != null) {
			LocalDate min = LocalDate.now().minusDays(validateFormField.above());
			LocalDate max = LocalDate.now().plusDays(validateFormField.below());
			return (value.isAfter(min) && value.isBefore(max)) || value.isEqual(max) || value.isEqual(min);
		}else {
			return false;
		}
	}
	/**
	 * Validate a string
	 * @param code
	 * @param validateFormField
	 * @return
	 */
	private boolean validateString(String str, Validator validateFormField) {
		if(str != null) {
			return str.length()>=validateFormField.above() && str.length()<= validateFormField.below();
		}else {
			return false;
		}
	}

	/**
	 * All questions for the current state should be answered
	 * @param model
	 * @param appDTO
	 * @return
	 */
	public ApplicationDTO validatePIPChecklist(Import_permit model, ApplicationDTO appDTO) {
		appDTO.setValid(true);
		appDTO.setAlertMessage("");
		for(QuestionInstancePIP question : model.getCheckLists() ) {
			if(question.getStatus().getId()==model.getPipStatus().getId() && !question.getHeader()) {
				AnswerQuestion answer = question.getAnswer();
				if(answer != null) {
					appDTO.setValid(
							answer.getYes() || answer.getNo() || answer.getNotApplicable()
							);
				}else {
					appDTO.setValid(false);
				}
				if(!appDTO.isValid()) {
					appDTO.setAlertMessage(messages.get("answerquestions"));
					break;
				}
			}
		}
		return appDTO;
	}

	/**
	 * Should be at least one detail
	 * Any detail should has non zero prices and units
	 * @param appDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public ApplicationDTO validatePipDetails(ApplicationDTO data) throws ObjectNotFoundException {
		Import_permit application = boilerServ.loadApplication(data.getId());
		boolean valid =true;
		if(application.getImport_permit_detail() != null && application.getImport_permit_detail().size()>0) {
			for(Import_permit_detail detail : application.getImport_permit_detail()) {
				valid = valid && (detail.getPrice().multiply(detail.getUnits()).compareTo(BigDecimal.valueOf(0.1))>0);
			}
		}else{
			valid=false;
		}
		if(!valid) {
			data.setValid(false);
			data.getDetails().setDetailsSuggest(messages.get("fillproductdata"));
		}else {
			data.getDetails().setDetailsSuggest("");
		}
		return data;
	}
}
