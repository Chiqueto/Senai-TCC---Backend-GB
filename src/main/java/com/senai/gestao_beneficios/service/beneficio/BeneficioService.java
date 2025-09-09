package com.senai.gestao_beneficios.service.beneficio;

import com.senai.gestao_beneficios.DTO.beneficio.BeneficioMapper;
import com.senai.gestao_beneficios.DTO.beneficio.BeneficioRequestDTO;
import com.senai.gestao_beneficios.DTO.beneficio.BeneficioResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import com.senai.gestao_beneficios.infra.exceptions.AlreadyExistsException;
import com.senai.gestao_beneficios.repository.BeneficioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BeneficioService {
    final BeneficioRepository repository;
    final BeneficioMapper mapper;

    public ApiResponse<BeneficioResponseDTO> createBeneficio (BeneficioRequestDTO request){
        Optional<Beneficio> beneficioExist = repository.findByNome(request.nome());
        if(beneficioExist.isPresent()){
            throw new AlreadyExistsException("Benefício com esse nome já existe");
        }

        Beneficio beneficio = new Beneficio();
        beneficio.setNome(request.nome());
        beneficio.setDescricao(request.descricao());
        beneficio.setCreated_at(Instant.now());

        Beneficio createdBeneficio = repository.save(beneficio);

        BeneficioResponseDTO response = mapper.toDTO(createdBeneficio);

        return new ApiResponse<>(true, response, null, null, "Benefício criado com sucesso!");
    }

    public ApiResponse<List<BeneficioResponseDTO>> buscarBeneficios (){
        List<Beneficio> beneficios = repository.findAll();
        List<BeneficioResponseDTO> beneficiosDTO = mapper.toDTOList(beneficios);

        return new ApiResponse<>(true, beneficiosDTO, null, null, "Benefícios encontrados com sucesso!");
    }

}
