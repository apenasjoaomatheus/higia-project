package start.project.higia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import start.project.higia.models.Doctor;
import start.project.higia.services.DoctorServices;

@Controller
public class DoctorController {
	
    //Lendo classe de serviço do doutor  
	@Autowired
	private DoctorServices services;
	
	//Rota para tela de cadastro 
	@GetMapping("doctor_registration")
	public String registration() {
		return "index";
	}
	
	//Rota para salvar o doutor no banco de dados
	@PostMapping("doctor")
	public String create(Doctor doctor) {
		this.services.create(doctor);
		return "index";
	}
}
