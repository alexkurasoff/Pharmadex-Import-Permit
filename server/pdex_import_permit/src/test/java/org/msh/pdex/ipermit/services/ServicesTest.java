package org.msh.pdex.ipermit.services;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.ipermit.PdexImportPermitApplicationTest;
import org.msh.pdex.ipermit.WebSecurity;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.model.User;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.model.rsecond.Currency;
import org.msh.pdex.repository.pip.Import_permitRepo;
import org.msh.pdex.services.ContextServices;
import org.msh.pdex.services.DictionaryService;
import org.msh.pdex.services.MagicEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@SpringBootTest(classes=PdexImportPermitApplicationTest.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ServicesTest {
	
	@Autowired
	UserService userService;
	
	@Autowired
	BoilerplateServices boilerServ;
	
	@Autowired
	ValidationService validator;
	
	@Autowired
	Import_permitRepo ipRepo;
	
	@Autowired
	CommonService commonServ;
	@Autowired
	ApplicantService applicantServ;
	
	@Autowired
	EntityToDtoService entityToDTOServ;
	
	@Autowired
	ContextServices contextServ;
	
	@Autowired
	DictionaryService dict;
	
	@Autowired
	MagicEntityService enttService ;
	
	@Autowired
	Import_permitRepo import_permitRepo;
	
	@Test
	public void shouldSupplier() throws ObjectNotFoundException {
		OptionDTO opt = new OptionDTO();
		opt.setCode("My");
		opt = commonServ.suppliers(opt);
		System.out.println(opt.getOptions());
	}
	@Test
	public void shouldListToStringAndNULL() {
		List<String> list = new ArrayList<String>();
		list.add("swin");
		list.add(null);
		list.add("kot");
		String res = String.join(",", list);
		System.out.println(res.replace("null,", ""));
	}
	
	@Test
	@Transactional
	public void createApplDTO() throws ObjectNotFoundException, IOException {
		Optional<String> contextId = Optional.empty();
		Context context = contextServ.loadContext(contextId);
		String loginName = "applicant";
		String group = "applicant";
		User user = boilerServ.userByUserLogin(loginName);
		
		Import_permit model = import_permitRepo.findById(new Long(31)).get();
		ApplicationDTO appDTO = new ApplicationDTO();
		appDTO.setId(model.getId());
		appDTO = entityToDTOServ.ApplicationToDto(context, model, loginName, group, appDTO);
		
		appDTO.getPipNumber().setValue("DRFT/31-1");
		appDTO.getCurrency().setValue(null);
		appDTO.getExpiry_date().setValue(new LocalDate(2020, 10, 10));
		

		Object[] objects = new Object[1];
		objects[0] = (Object)model;
		
		enttService.magic(appDTO, objects);
		
		model = (Import_permit)objects[0];

		assertTrue(model.getPipNumber().equals("DRFT/31-1"));
	}
}
