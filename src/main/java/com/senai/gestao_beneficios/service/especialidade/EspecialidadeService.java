package com.senai.gestao_beneficios.service.especialidade;

import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.medico.Especialidade;
import com.senai.gestao_beneficios.infra.exceptions.BadRequest;
import com.senai.gestao_beneficios.repository.EspecialidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecialidadeService {
    final EspecialidadeRepository especialidadeRepository;

    public ApiResponse<EspecialidadeDTO> createEspecialidade (String nome) {
        if (nome.isEmpty()){
            throw new BadRequest("Nome não pode estar em branco");
        }
        Especialidade especialidade = new Especialidade();
        especialidade.setNome(nome);
        Especialidade savedEspecialidade = especialidadeRepository.save(especialidade);
        return new ApiResponse<>(true, new EspecialidadeDTO(savedEspecialidade.getId(), savedEspecialidade.getNome()), null, null, "Especialidade criado com sucesso!");
    }

    public ApiResponse<List<EspecialidadeDTO>> buscarEspecialidades (){
        List<Especialidade> especialidades = especialidadeRepository.findAll();

        List<EspecialidadeDTO> especialidadesDTO = especialidades.stream().map(
            (especialidade -> {
                return new EspecialidadeDTO(especialidade.getId(), especialidade.getNome());
                }
            )
        ).toList();

        return new ApiResponse<List<EspecialidadeDTO>>(true, especialidadesDTO, null, null, "Especialidades encontradas com sucesso!");

    }
}
