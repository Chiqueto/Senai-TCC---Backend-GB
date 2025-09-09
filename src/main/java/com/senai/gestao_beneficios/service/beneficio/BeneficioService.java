package com.senai.gestao_beneficios.service.beneficio;

import com.senai.gestao_beneficios.DTO.beneficio.BeneficioRequestDTO;
import com.senai.gestao_beneficios.DTO.beneficio.BeneficioResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import com.senai.gestao_beneficios.infra.exceptions.AlreadyExistsException;
import com.senai.gestao_beneficios.repository.BeneficioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BeneficioService {
    final BeneficioRepository repository;

    public ApiResponse<BeneficioResponseDTO> createBeneficio (BeneficioRequestDTO request){
        Optional<Beneficio> beneficioExist = repository.findByNome(request.nome());
        if(beneficioExist.isPresent()){
            throw new AlreadyExistsException("Benefício com esse nome já existe");
        }

        Beneficio beneficio =

        return null;
    }

}
