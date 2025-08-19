package com.senai.gestao_beneficios.service;

import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ColaboradorService {
    private final ColaboradorRepository colaboradorRepository;


    public ApiResponse<ColaboradorDTO> getUserById(String id){
        Colaborador colaborador = colaboradorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id", "Colaborador não encontrado com o ID: " + id));

        return

    }
}
