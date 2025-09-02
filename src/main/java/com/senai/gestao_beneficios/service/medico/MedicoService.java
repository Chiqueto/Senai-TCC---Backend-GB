package com.senai.gestao_beneficios.service.medico;

import com.senai.gestao_beneficios.DTO.medico.MedicoMapper;
import com.senai.gestao_beneficios.DTO.medico.MedicoRequestDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.medico.Disponibilidade;
import com.senai.gestao_beneficios.domain.medico.Especialidade;
import com.senai.gestao_beneficios.domain.medico.Medico;
import com.senai.gestao_beneficios.infra.exceptions.BadRequest;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.infra.exceptions.ServerException;
import com.senai.gestao_beneficios.repository.DisponibilidadeRepository;
import com.senai.gestao_beneficios.repository.EspecialidadeRepository;
import com.senai.gestao_beneficios.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicoService {
    final MedicoRepository medicoRepository;
    final MedicoMapper medicoMapper;
    final EspecialidadeRepository especialidadeRepository;
    final DisponibilidadeRepository disponibilidadeRepository;

    public ApiResponse<MedicoResponseDTO> createMedico (MedicoRequestDTO request){
        Optional<Medico> medicoExist = medicoRepository.findByEmail(request.email());

        if (medicoExist.isPresent()){
            return new ApiResponse<>(false, null, "Médico já existe com esse e-mail", null, null);
        }

        boolean existeValorInvalido = request.disponibilidade().stream()
                .anyMatch(dia -> dia < 0 || dia > 6);

        if (existeValorInvalido) {
            throw new BadRequest("Valor de disponibilidade inválido. Os valores devem estar entre 0 e 6.");
        }

        Especialidade especialidade = especialidadeRepository.findById(request.id_especialidade()).orElseThrow(
                () -> new NotFoundException("especialidade", "Especialidade não encontrada"));

        Medico medico = new Medico();
        medico.setNome(request.nome());
        medico.setEmail(request.email());
        medico.setEspecialidade(especialidade);
        medico.setHoraEntrada(request.horaEntrada());
        medico.setHoraPausa(request.horaPausa());
        medico.setHoraVolta(request.horaVolta());
        medico.setHoraSaida(request.horaSaida());

        Medico medicoCriado = medicoRepository.save(medico);

        request.disponibilidade().forEach(dia -> {
            Disponibilidade novaDisponibilidade = new Disponibilidade();
            novaDisponibilidade.setMedico(medico);
            novaDisponibilidade.setDiaSemana(dia);
            disponibilidadeRepository.save(novaDisponibilidade);
        });

        MedicoResponseDTO medicoResponseDTO = medicoMapper.toDTO(medicoCriado);
        return new ApiResponse<MedicoResponseDTO>(true, medicoResponseDTO, null, null, "Médico criado com sucesso!");
    }
}
