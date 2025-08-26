package com.senai.gestao_beneficios.controller.medico;

import com.senai.gestao_beneficios.DTO.medico.MedicoRequestDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/medico")
public class Medico {

    public ResponseEntity<ApiResponse<MedicoResponseDTO>> createMedico (MedicoRequestDTO medicoRequestDTO){
        return null;
    }
}
